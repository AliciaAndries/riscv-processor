package core

import chisel3._
import chisel3.util._

class TestIO() extends Bundle {
    val wb_data = Output(UInt(32.W))
    val aluzero = Output(Bool())
    val aluresult = Output(UInt(32.W))
    val op1 = Output(UInt(32.W))
    val op2 = Output(UInt(32.W))
    val rs2 = Output(UInt(32.W))
    val raddr1 = Output(UInt(5.W))
    val raddr2 = Output(UInt(5.W))
    val rwdata = Output(UInt(5.W))
}

class FpgaTestIO() extends Bundle{
    //val led = Output(Bool())
    val pc = Output(UInt(32.W))
    val wb = Output(UInt(32.W))
    val zero = Output(Bool())
}

class DataflowIO() extends Bundle {
    //val instrRegIO = Flipped(new InstructionRegIO)
    val dMemIO = Flipped(new MemoryIO)
    val iMemIO = Flipped(new MemoryIO)
    val test = new TestIO
    val fpgatest = new FpgaTestIO
} 

object PC_CONSTS {
    val pc_init = 0x4.U(32.W)
    val pc_expt = 0x0.U(32.W)
}

class Dataflow(test : Boolean = false) extends Module {
    val io = IO(new DataflowIO)

    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALU)
    val branchLogic = Module(new BranchLogic)
    val forwardingUnit = Module(new ForwardingUnit)

    // branch/jal wires
    val taken               = Wire(false.B)
    val tBranchaddr         = Wire(0.U(W.32))
    val aluresult           = Wire(0.U(W.32))
    // registers
    val pc                  = RegInit(PC_CONSTS.pc_init) //32 bit so< 4 byte instructions TODO: should it be init to 0?
    val start               = RegInit(true.B)
    val aluwb_prev_inst     = RegInit(0.U(32.W))
    // if/id
    val if_id_pc            = RegInit(0.U(W.32))
    // id/ex
    val id_ex_rs1_addr      = RegInit(0.U(W.5))
    val id_ex_rs2_addr      = RegInit(0.U(W.5))

    val id_ex_pc            = RegInit(0.U(W.32))
    val id_ex_rs1           = RegInit(0.U(W.32))
    val id_ex_rs2           = RegInit(0.U(W.32))
    val id_ex_immgen        = RegInit(0.U(W.32))
    val id_ex_rd            = RegInit(0.U(W.5))
    val id_ex_aluCtrl       = RegInit(0.U(W.4))
    val id_ex_op2Ctrl       = RegInit(Bool())
    val id_ex_op1Ctrl       = RegInit(Bool()))
    val id_ex_sttype        = RegInit(0.U(W.2))
    val id_ex_ldtype        = RegInit(0.U(W.3))
    val id_ex_wbsrc         = RegInit(0.U(W.2))
    val id_ex_bt            = RegInit(0.U(W.3))
    // ex/mem
    val ex_mem_pc            = RegInit(0.U(W.32))
    val ex_mem_aluresult     = RegInit(0.U(W.32))
    val ex_mem_rd            = RegInit(0.U(W.5))
    val ex_mem_rs2           = RegInit(0.U(W.32))
    val ex_mem_sttype        = RegInit(0.U(W.2))
    val ex_mem_ldtype        = RegInit(0.U(W.3))
    val ex_mem_wbsrc         = RegInit(0.U(W.2))
    // mem/wb
    val mem_wb_pc            = RegInit(0.U(W.32))
    val mem_wb_aluresult     = RegInit(0.U(W.32))
    val mem_wb_rd            = RegInit(0.U(W.5))
    val mem_wb_wbsrc         = RegInit(0.U(W.2))



    ////////////////////////////////////////fetch instruction//////////////////////////////////////// 
    
    val pc_current := Mux(start, pc,
                        Mux(taken, tBranchaddr, 
                        Mux(control.io.PCSrc === Control.Jump, aluresult, //TODO: this is wrong, you need the control.PCSrc from execution phase, however might change the calc to decode
                        Mux(control.io.PCSrc === Control.EXC, PC_CONSTS.pc_expt, pc + 4.U)))))

    io.iMemIO.req.bits.addr := pc_current
    io.iMemIO.req.valid := true.B
    io.iMemIO.req.bits.mask := 0.U
    io.iMemIO.req.bits.data := DontCare
    
    io.fpgatest.pc := pc_current
    
    if_id_pc            := pc_current

    pc := pc_current
    start := false.B


    ////////////////////////////////////////decode instruction////////////////////////////////////////

    val inst = io.iMemIO.resp.bits.data     //need to read mem on next clockcycle otherwise you get prev result  
    control.io.inst := inst
    //immgen extend 12 bits
    immGen.io.inst := inst
    immGen.io.immGenCtrl := control.io.immGenCtrl
    val extended = immGen.io.out

    //get registers
    regFile.io.raddr1 := inst(19,15)
    regFile.io.raddr2 := inst(24,20) 
    val rs1 = regFile.io.rs1            //always exists
    val rs2 = regFile.io.rs2            //only R&S-Type

    id_ex_rs1_addr      := inst(19,15)
    id_ex_rs2_addr      := inst(24,20)
    id_ex_pc            := if_id_pc
    id_ex_rs1           := rs1
    id_ex_rs2           := rs2
    id_ex_immgen        := extended
    id_ex_rd            := if_id_pc(11,7)
    id_ex_aluCtrl       := control.io.aluCtrl
    id_ex_op2Ctrl       := control.io.op2Ctrl
    id_ex_op1Ctrl       := control.io.op1Ctrl
    id_ex_sttype        := control.io.sttype
    id_ex_ldtype        := control.io.ldtype
    id_ex_wbsrc         := control.io.wbsrc
    id_ex_bt            := control.io.bt


    ////////////////////////////////////////execute////////////////////////////////////////

    alu.io.operation := id_ex_aluCtrl

    forwardingUnit.io.rs1_cur := id_ex_rs1_addr
    forwardingUnit.io.rs2_cur := id_ex_rs2_addr
    forwardingUnit.io.rd_mem_stage := ex_mem_rd
    forwardingUnit.io.rd_wb_stage := mem_wb_rd

    // if its a load its PREV_PREV cause prev is 
    val reg_input1 := Mux(forwardingUnit.io.reg1 === PREV_PREV_PREV, aluwb_prev_inst, 
                            Mux(forwardingUnit.io.reg1 === PREV_PREV, ex_mem_aluresult, 
                            Mux(forwardingUnit.io.reg1 === PREV, mem_wb_aluresult, id_ex_rs1)
    val reg_input2 := Mux(forwardingUnit.io.reg2 === PREV_PREV_PREV, aluwb_prev_inst,
                            Mux(forwardingUnit.io.reg2 === PREV_PREV, ex_mem_aluresult,
                            Mux(forwardingUnit.io.reg2 === PREV, mem_wb_aluresult, id_ex_rs2)
    val aluop1 = Mux(id_ex_op1Ctrl === Control.op1Reg, reg_input1, id_ex_pc)
    val aluop2 = Mux(id_ex_op2Ctrl === Control.op2Imm, id_ex_immgen, reg_input2)
    alu.io.op1 := aluop1
    alu.io.op2 := aluop2
    
    aluresult := alu.io.result
    branchLogic.io.comp := alu.io.comp
    branchLogic.io.bt := id_ex_bt
    // if taken === 1 you need to nop the last instruction and set pc
    taken := branchLogic.io.taken
    io.fpgatest.zero := taken
    //Branch should this be a reg?
    tBranchaddr = id_ex_immgen + pc

    ex_mem_pc           := id_ex_pc
    ex_mem_aluresult    := aluresult
    ex_mem_rd           := id_ex_rd
    ex_mem_rs2          := id_ex_rs2
    ex_mem_sttype       := id_ex_sttype
    ex_mem_ldtype       := id_ex_ldtype
    ex_mem_wbsrc        := id_ex_wbsrc

    
    ////////////////////////////////////////Memory Access////////////////////////////////////////

    //The SW, SH, and SB instructions store 32-bit, 16-bit, and 8-bit values from the low bits of register rs2 to memory.
    io.dMemIO.req.bits.addr := ex_mem_aluresult
    io.dMemIO.req.bits.data := ex_mem_rs2

    //W -> moffset = 0, H -> moffset = 0 or 2, B -> mmoffset =  0-3
    val moffset = Mux(ex_mem_sttype === Control.ST_SW || ex_mem_sttype === Control.LD_LW, 0.U,
                    Mux(ex_mem_sttype === Control.ST_SH || ex_mem_sttype === Control.LD_LH || ex_mem_sttype === Control.LD_LHU,  
                        ex_mem_aluresult(1,0)&"b10".U, ex_mem_aluresult(1,0)))
    val doffset = moffset << 3
    
    io.dMemIO.req.bits.data := rs2 << doffset
    io.dMemIO.req.bits.mask := MuxLookup(ex_mem_sttype, "b0000".U, 
        Array(
            Control.ST_SW -> ("b1111".U),
            Control.ST_SH -> ("b11".U << moffset),      //what if someone puts in addr(1,0) = "01" or "11", no longer aligned so maybe throw a fit?
            Control.ST_SB -> ("b1".U << moffset)
        ))
    
    io.dMemIO.req.valid := ex_mem_sttype.orR || ex_mem_ldtype.orR 

    mem_wb_pc           := ex_mem_pc
    mem_wb_aluresult    := ex_mem_aluresult
    mem_wb_rd           := ex_mem_rd
    mem_wb_wbsrc        := ex_mem_wbsrc


    ////////////////////////////////////////write back////////////////////////////////////////

    val memrdata = Mux(io.dMemIO.resp.valid, io.dMemIO.resp.bits.data, 0.U) >> doffset  //offset cause if you want to read at alu(1,0) = "10" [LB] you need to move result to right to then use the mask below
    val rdata = MuxLookup(ex_mem_ldtype, memrdata.asSInt,
        Array(
            Control.LD_LH  -> (memrdata(15,0).asSInt),   //SInt sign extends the value
            Control.LD_LHU -> ((memrdata(15,0).zext).asSInt),    //zext is UInt feature
            Control.LD_LB  -> (memrdata(7,0).asSInt),
            Control.LD_LBU -> ((memrdata(7,0).zext).asSInt)
        ))


    regFile.io.waddr := mem_wb_rd
    regFile.io.wen := mem_wb_wbsrc.orR
    val wbdata = Mux(mem_wb_wbsrc === Control.WB_MEM, rdata.asUInt, 
                            Mux(mem_wb_wbsrc === Control.WB_PC, mem_wb_pc + 4.U, mem_wb_aluresult))
    regFile.io.wdata := wbdata
    io.fpgatest.wb := wbdata

    aluwb_prev_inst := mem_wb_aluresult


    
    
    if(test){
        io.test.raddr1 := inst(19,15)
        io.test.raddr2 := inst(24,20) 
        io.test.rs2 := rs2
        io.test.op1 := aluop1
        io.test.op2 := aluop2
        io.test.aluzero := alu.io.comp
        io.test.aluresult := alu.io.result
        io.test.rwdata := inst(11,7)
        io.test.wb_data := Mux(control.io.ldtype.orR, rdata.asUInt, aluresult)
    } else{
        io.test <> DontCare
    }


}

object Dataflowdriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Dataflow, args)
}