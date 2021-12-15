package core

import chisel3._
import chisel3.util._
import ForwardingUnit._

class FpgaTestIO() extends Bundle{
    val halt_in = Input(Bool())
    val pc = Output(UInt(32.W))
    val reg_addr = Input(UInt(5.W))
    val reg_data = Output(UInt(32.W))
    val rs1data = Output(UInt(32.W))
    val rs2data = Output(UInt(32.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val wdata = Input(UInt(32.W))
    val waddr = Input(UInt(32.W))
    val wmask = Input(UInt(4.W))
    val pc_reset = Input(Bool())

    val pc_ex = Output(UInt(32.W))
    val wb = Output(UInt(32.W))
    val halt = Output(Bool())
    val reg_input1 = Output(UInt(3.W))
    val reg_input2 = Output(UInt(3.W))
    val id_ex_rd = Output(UInt(5.W))
    val decode_pc = Output(UInt(32.W))
    val decode_inst = Output(UInt(32.W))
    val aluresult = Output(UInt(32.W))
}

trait DataflowTrait {
    val io = new DataflowTestIO
}

class DataflowIO() extends Bundle {
    //val instrRegIO = Flipped(new InstructionRegIO)
    val dMemIO = Flipped(new MemoryIO)
    val iMemIO = Flipped(new MemoryIO)
    val io_out_of_bounds = Input(Bool()) 
}

class DataflowTestIO() extends DataflowIO {
    val fpgatest = new FpgaTestIO

}

object PC_CONSTS {
    val pc_init = 0x0.U(32.W)
    val pc_expt = 0xDEAD.U(32.W)
}

object BRANCH_CONTST {
    val offset_for_nops = 8.U
}

class Dataflow(test : Boolean = false) extends Module with DataflowTrait{
    override val io = IO(new DataflowTestIO)

    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALUWithBranch)
    val branchLogic = Module(new BranchLogic)
    val forwardingUnit = Module(new ForwardingUnit)
    val hazardDetection = Module(new HazardDetection)

    val nop = "b00000000000000000000000000110011".U(32.W)
    //halt
    val halt                = WireDefault(false.B)
    // branch/jal wires/forwarding
    val taken               = WireDefault(false.B)
    val jump                = WireDefault(false.B)
    val tBranchaddr         = WireDefault(0.U(32.W))
    val aluresult           = WireDefault(0.U(32.W))
    val wbdata              = WireDefault(0.U(32.W))
    // registers
    val pc                  = RegInit(PC_CONSTS.pc_init) //32 bit so< 4 byte instructions TODO: should it be init to 0?
    val start               = RegInit(true.B)
    val start_decode        = RegInit(true.B)
    val inst                = RegInit(nop)
    val wb_prev_inst        = RegInit(0.U(32.W))
    val rd_prev_inst        = RegInit(0.U(5.W))
    val wbsrc_prev_inst     = RegInit(0.U(2.W))
    // if/id
    val if_id_pc            = RegInit(0.U(32.W))
    // id/ex
    val id_ex_is_jump       = RegInit(false.B)
    val id_ex_rs1_addr      = RegInit(0.U(5.W))
    val id_ex_rs2_addr      = RegInit(0.U(5.W))

    val id_ex_pc            = RegInit(0.U(32.W))
    val id_ex_rs1           = RegInit(0.U(32.W))
    val id_ex_rs2           = RegInit(0.U(32.W))
    val id_ex_immgen        = RegInit(0.U(32.W))
    val id_ex_rd            = RegInit(0.U(5.W))
    val id_ex_aluCtrl       = RegInit(0.U(4.W))
    val id_ex_op2Ctrl       = RegInit(false.B)
    val id_ex_op1Ctrl       = RegInit(false.B)
    val id_ex_sttype        = RegInit(0.U(2.W))
    val id_ex_ldtype        = RegInit(0.U(3.W))
    val id_ex_wbsrc         = RegInit(0.U(2.W))
    val id_ex_bt            = RegInit(0.U(3.W))
    // ex/mem
    val ex_mem_pc            = RegInit(0.U(32.W))
    val ex_mem_aluresult     = RegInit(0.U(32.W))
    val ex_mem_rd            = RegInit(0.U(5.W))
    val ex_mem_rs2           = RegInit(0.U(32.W))
    val ex_mem_sttype        = RegInit(0.U(2.W))
    val ex_mem_ldtype        = RegInit(0.U(3.W))
    val ex_mem_wbsrc         = RegInit(0.U(2.W))
    // mem/wb
    val mem_wb_resp_valid    = RegInit(false.B)
    val mem_wb_pc            = RegInit(0.U(32.W))
    val mem_wb_aluresult     = RegInit(0.U(32.W))
    val mem_wb_rd            = RegInit(0.U(5.W))
    val mem_wb_wbsrc         = RegInit(0.U(2.W))
    val mem_wb_ldtype        = RegInit(0.U(3.W))
    val mem_wb_doffset       = RegInit(0.U(5.W))



    ////////////////////////////////////////fetch instruction//////////////////////////////////////// 
    val test_halt = WireDefault(false.B)
    if(test){
        test_halt := io.fpgatest.halt_in
    }
    val pc_current = Mux(halt || test_halt || start, pc,
                        Mux(taken, tBranchaddr, 
                        //Mux(id_ex_is_jump, tBranchaddr - BRANCH_CONTST.offset_for_nops, //TODO: this is wrong, you need the control.PCSrc from execution phase, however might change the calc to decode
                        Mux(control.io.PCSrc === Control.EXC, PC_CONSTS.pc_expt, pc + 4.U )))//)
    
    io.iMemIO.req.bits.addr := pc_current
    io.iMemIO.req.valid := true.B
    io.iMemIO.req.bits.mask := 0.U
    io.iMemIO.req.bits.data := DontCare

    pc := pc_current
    if_id_pc := Mux(halt || taken || test_halt || id_ex_is_jump, if_id_pc, pc)

    //printf("mpc = %d, pc = %d, if_id_pc = %d, inst = %x\n\n", pc_current, pc, if_id_pc, inst)

    start := false.B
    ////////////////////////////////////////decode instruction////////////////////////////////////////

    //hazard detection
    inst := Mux(taken || id_ex_is_jump || start, nop, 
                Mux(halt, inst, io.iMemIO.resp.bits.data))   //first clockcycle say next clockcycle it also needs to be nop


    val raddr1 = inst(19,15)
    val raddr2 = inst(24,20)

    hazardDetection.io.rs1 := raddr1
    hazardDetection.io.rs2 := raddr2
    hazardDetection.io.rd_prev := id_ex_rd
    hazardDetection.io.prev_is_load := id_ex_ldtype.orR
    
    halt := hazardDetection.io.nop

    val inst_halt = Mux(halt || test_halt, nop, inst)      //need to read mem on next clockcycle otherwise you get prev result  
    control.io.inst := inst_halt

    //immgen extend 12 bits
    immGen.io.inst := inst_halt
    immGen.io.immGenCtrl := control.io.immGenCtrl
    val extended = immGen.io.out

    //get registers
    regFile.io.raddr1 := raddr1
    regFile.io.raddr2 := raddr2
    val rs1 = regFile.io.rs1            //always exists
    val rs2 = regFile.io.rs2            //only R&S-Type

    
    id_ex_is_jump       := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.PCSrc === Control.Jump)
    id_ex_rs1_addr      := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, raddr1)
    id_ex_rs2_addr      := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, raddr2)
    id_ex_pc            := if_id_pc
    id_ex_rs1           := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, rs1)
    id_ex_rs2           := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, rs2)
    id_ex_immgen        := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, extended)
    id_ex_rd            := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, inst(11,7))
    id_ex_aluCtrl       := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.aluCtrl)
    id_ex_op2Ctrl       := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.op2Ctrl)
    id_ex_op1Ctrl       := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.op1Ctrl)
    id_ex_sttype        := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.sttype)
    id_ex_ldtype        := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.ldtype)
    id_ex_wbsrc         := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.wbsrc)
    id_ex_bt            := Mux(halt || taken || id_ex_is_jump || control.io.PCSrc === Control.EXC, 0.U, control.io.bt)

    ////////////////////////////////////////execute////////////////////////////////////////

    alu.io.operation := id_ex_aluCtrl

    forwardingUnit.io.rs1_cur := id_ex_rs1_addr
    forwardingUnit.io.rs2_cur := id_ex_rs2_addr
    forwardingUnit.io.cur_is_load := id_ex_ldtype.orR
    forwardingUnit.io.rd_ex_mem := Mux(ex_mem_wbsrc === Control.WB_F, 0.U, ex_mem_rd)
    forwardingUnit.io.rd_mem_wb := Mux(mem_wb_wbsrc === Control.WB_F, 0.U, mem_wb_rd)
    forwardingUnit.io.rd_wb_out := Mux(wbsrc_prev_inst === Control.WB_F, 0.U, rd_prev_inst)
    
    // if its a load its PREV_PREV cause prev is 
    val reg_input1 = Mux(forwardingUnit.io.reg1 === WB_OUT, wb_prev_inst, 
                            Mux(forwardingUnit.io.reg1 === MEM_WB, /* mem_wb_aluresult, */wbdata, 
                            Mux(forwardingUnit.io.reg1 === EX_MEM_ALU, ex_mem_aluresult, id_ex_rs1)))
    val reg_input2 = Mux(forwardingUnit.io.reg2 === WB_OUT, wb_prev_inst,
                            Mux(forwardingUnit.io.reg2 === MEM_WB, /* mem_wb_aluresult, */wbdata,
                            Mux(forwardingUnit.io.reg2 === EX_MEM_ALU, ex_mem_aluresult, id_ex_rs2)))

    val aluop1 = Mux(id_ex_op1Ctrl === Control.op1Reg, reg_input1, id_ex_pc)
    val aluop2 = Mux(id_ex_op2Ctrl === Control.op2Imm, id_ex_immgen, reg_input2)
    alu.io.op1 := aluop1
    alu.io.op2 := aluop2
    
    aluresult := alu.io.result
    branchLogic.io.comp := alu.io.comp
    branchLogic.io.bt := id_ex_bt
    // if taken === 1 you need to nop the last instruction and set pc
    taken := branchLogic.io.taken || id_ex_is_jump
    //Branch should this be a reg?
    
    //TODO: can also be input from rs1
    tBranchaddr := id_ex_immgen + Mux(id_ex_is_jump && id_ex_op1Ctrl === Control.op1Reg, reg_input1, id_ex_pc)

    ex_mem_pc           := id_ex_pc
    ex_mem_aluresult    := aluresult
    ex_mem_rd           := id_ex_rd
    ex_mem_rs2          := reg_input2
    ex_mem_sttype       := id_ex_sttype
    ex_mem_ldtype       := id_ex_ldtype
    ex_mem_wbsrc        := id_ex_wbsrc

    
    ////////////////////////////////////////Memory Access////////////////////////////////////////

    //The SW, SH, and SB instructions store 32-bit, 16-bit, and 8-bit values from the low bits of register rs2 to memory.
    io.dMemIO.req.bits.addr := ex_mem_aluresult
    //io.dMemIO.req.bits.data := ex_mem_rs2

    //W -> moffset = 0, H -> moffset = 0 or 2, B -> mmoffset =  0-3
    val moffset = Mux(ex_mem_sttype === Control.ST_SW || ex_mem_ldtype === Control.LD_LW, 0.U,
                    Mux(ex_mem_sttype === Control.ST_SH || ex_mem_ldtype === Control.LD_LH || ex_mem_ldtype === Control.LD_LHU,  
                        ex_mem_aluresult(1,0)&"b10".U, ex_mem_aluresult(1,0)))
    val doffset = moffset << 3
    
    io.dMemIO.req.bits.data := ex_mem_rs2 << doffset
    
    val mask = MuxLookup(ex_mem_sttype, "b0000".U, 
        Array(
            Control.ST_SW -> ("b1111".U),
            Control.ST_SH -> ("b11".U << moffset),      //what if someone puts in addr(1,0) = "01" or "11", no longer aligned so maybe throw a fit?
            Control.ST_SB -> ("b1".U << moffset)
        ))
    io.dMemIO.req.bits.mask := mask
    io.dMemIO.req.valid := ex_mem_sttype.orR || ex_mem_ldtype.orR 
    
    mem_wb_resp_valid   := io.dMemIO.resp.valid
    mem_wb_pc           := ex_mem_pc
    mem_wb_aluresult    := ex_mem_aluresult
    mem_wb_rd           := ex_mem_rd
    mem_wb_wbsrc        := ex_mem_wbsrc
    mem_wb_ldtype       := ex_mem_ldtype
    mem_wb_doffset      := doffset


    ////////////////////////////////////////write back////////////////////////////////////////

    val memrdata = Mux(mem_wb_resp_valid, io.dMemIO.resp.bits.data, 0.U) >> mem_wb_doffset  //offset cause if you want to read at alu(1,0) = "10" [LB] you need to move result to right to then use the mask below
    val rdata = MuxLookup(mem_wb_ldtype, memrdata.asSInt,
        Array(
            Control.LD_LH  -> (memrdata(15,0).asSInt),   //SInt sign extends the value
            Control.LD_LHU -> ((memrdata(15,0).zext).asSInt),    //zext is UInt feature
            Control.LD_LB  -> (memrdata(7,0).asSInt),
            Control.LD_LBU -> ((memrdata(7,0).zext).asSInt)
        ))


    regFile.io.waddr := mem_wb_rd
    regFile.io.wen := mem_wb_wbsrc.orR
    wbdata := Mux(mem_wb_wbsrc === Control.WB_MEM, rdata.asUInt, 
                            Mux(mem_wb_wbsrc === Control.WB_PC, mem_wb_pc + 4.U, mem_wb_aluresult))
    regFile.io.wdata := wbdata

    wb_prev_inst := wbdata
    rd_prev_inst := mem_wb_rd
    wbsrc_prev_inst := mem_wb_wbsrc

    ////////////////////////////////////////test stuff////////////////////////////////////////
        if(test){
            when(io.fpgatest.halt_in){
                regFile.io.raddr1 := io.fpgatest.reg_addr
                io.fpgatest.reg_data := regFile.io.rs1

            }.otherwise{
                io.fpgatest.reg_data := 0.U
            }
            /* regFile.io.raddr1 := io.fpgatest.reg_addr
            io.fpgatest.reg_data := regFile.io.rs1 */
            io.fpgatest.rs1 := id_ex_rs1_addr
            io.fpgatest.rs2 := id_ex_rs2_addr
            io.fpgatest.rs1data := aluop1
            io.fpgatest.rs2data := aluop2
            io.fpgatest.aluresult := aluresult
            io.fpgatest.id_ex_rd := mem_wb_rd

            io.fpgatest.wb := wbdata
            io.fpgatest.pc := mem_wb_pc

            io.fpgatest.reg_input1 := forwardingUnit.io.reg1
            io.fpgatest.reg_input2 := forwardingUnit.io.reg2
            io.fpgatest.pc_ex := id_ex_pc
            io.fpgatest.halt := taken || halt
            io.fpgatest.decode_inst := inst_halt
            io.fpgatest.decode_pc := if_id_pc >> 2.U
    } else {
        io.fpgatest <> DontCare
    }
}

object Dataflowdriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Dataflow, args)
}