package core

import chisel3._
import chisel3.util._
import ForwardingUnit._

class DataflowALUSplit(test : Boolean = false) extends Module with DataflowTrait{
    override val io = IO(new DataflowTestIO)

    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALUBasic)
    val branchLogic = Module(new FullBranchLogic)
    val forwardingUnit = Module(new ForwardingUnit)
    val hazardDetection = Module(new HazardDetectionALUSplit)

    val nop = "b00000000000000000000000000110011".U(32.W)
    //halt
    val halt                = WireDefault(false.B)
    // branch/jal wires/forwarding
    val taken_nop           = RegInit(false.B)
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
    taken_nop := taken
    if_id_pc := Mux(halt || taken || test_halt || control.io.PCSrc === Control.Jump, if_id_pc, pc)


    start := false.B
    ////////////////////////////////////////decode instruction////////////////////////////////////////

    //hazard detection
    inst := Mux(halt, inst,
            Mux(taken || control.io.PCSrc === Control.Jump || start, nop, io.iMemIO.resp.bits.data))   //first clockcycle say next clockcycle it also needs to be nop

    control.io.inst := inst
    
    val raddr1 = inst(19,15)
    val raddr2 = inst(24,20)

    hazardDetection.io.s.rs1 := raddr1
    hazardDetection.io.s.rs2 := raddr2
    hazardDetection.io.s.rd_prev := id_ex_rd
    hazardDetection.io.a.rd_prev_prev := ex_mem_rd
    hazardDetection.io.s.prev_is_load := id_ex_ldtype.orR
    /* hazardDetection.io.rs1 := raddr1
    hazardDetection.io.rs2 := raddr2
    hazardDetection.io.rd_prev := id_ex_rd
    hazardDetection.io.rd_prev_prev := ex_mem_rd
    hazardDetection.io.prev_is_load := id_ex_ldtype.orR */

    val is_arith = !id_ex_ldtype.orR && !id_ex_sttype.orR && !(id_ex_is_jump) && !(id_ex_bt.orR)
    hazardDetection.io.a.prev_is_arith := is_arith
    hazardDetection.io.a.is_branch := control.io.bt.orR || (control.io.PCSrc === Control.Jump)
    /* hazardDetection.io.prev_is_arith := is_arith
    hazardDetection.io.is_branch := control.io.bt.orR || (control.io.PCSrc === Control.Jump) */

    halt := hazardDetection.io.s.nop

    /* when(inst =/=0.U){    
    printf("inst = %x, rs1 = %d, rs2 = %d, rd = %d, rd_prev = %d, rd_prev_prev = %d, prev_load = %d, prev_arith = %d, is_branch = %d\n", 
            inst, raddr1, raddr2, inst(11,7), id_ex_rd, ex_mem_rd, id_ex_ldtype.orR, is_arith, control.io.bt.orR)
    } */

    val inst_halt = Mux(halt || test_halt, nop, inst)      //need to read mem on next clockcycle otherwise you get prev result  
    

    //immgen extend 12 bits
    immGen.io.inst := inst_halt
    immGen.io.immGenCtrl := control.io.immGenCtrl
    val extended = immGen.io.out

    //get registers
    regFile.io.raddr1 := inst_halt(19,15)
    regFile.io.raddr2 := inst_halt(24,20)
    val rs1 = regFile.io.rs1            //always exists
    val rs2 = regFile.io.rs2            //only R&S-Type

    val forwarding_decode = Module(new ForwardingUnit)

    forwarding_decode.io.rs1_cur := inst_halt(19,15)
    forwarding_decode.io.rs2_cur := inst_halt(24,20)
    forwarding_decode.io.cur_is_load := false.B
    forwarding_decode.io.rd_ex_mem := Mux(id_ex_wbsrc === Control.WB_F, 0.U, id_ex_rd)
    forwarding_decode.io.rd_mem_wb := Mux(ex_mem_wbsrc === Control.WB_F, 0.U, ex_mem_rd)
    forwarding_decode.io.rd_wb_out := Mux(mem_wb_wbsrc === Control.WB_F, 0.U, mem_wb_rd)
    
    val rs1_forwarded = MuxLookup(forwarding_decode.io.reg1, rs1,
                                    Seq(
                                        CUR -> rs1,
                                        EX_MEM_ALU -> rs1,    //this is a nop
                                        MEM_WB -> ex_mem_aluresult,
                                        WB_OUT -> wbdata
                                    ))
    val rs2_forwarded = MuxLookup(forwarding_decode.io.reg2, rs2,
                                    Seq(
                                        CUR -> rs2,
                                        EX_MEM_ALU -> rs2,    //this is a nop
                                        MEM_WB -> ex_mem_aluresult,
                                        WB_OUT -> wbdata
                                    ))
    branchLogic.io.rs1 := rs1_forwarded
    branchLogic.io.rs2 := rs2_forwarded
    branchLogic.io.bt := control.io.bt
    // if taken === 1 you need to nop the last instruction and set pc
    taken := branchLogic.io.taken || control.io.PCSrc === Control.Jump /* || (control.io.PCSrc === Control.EXC) */

    //printf("pc = %d, inst = %x, rs1 = %d, rs1_source = %d, rs2 = %d, rs2_source = %d, taken = %d, branchT = %d \n",
     //if_id_pc>>2.U, inst_halt, rs1_forwarded, forwarding_decode.io.reg1, rs2_forwarded, forwarding_decode.io.reg1, taken, control.io.bt)

    tBranchaddr := extended + Mux(control.io.PCSrc === Control.Jump && control.io.op1Ctrl === Control.op1Reg, rs1_forwarded, if_id_pc)

    
    id_ex_is_jump       := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.PCSrc === Control.Jump)
    id_ex_rs1_addr      := Mux(control.io.PCSrc === Control.EXC, 0.U, raddr1)
    id_ex_rs2_addr      := Mux(control.io.PCSrc === Control.EXC, 0.U, raddr2)
    id_ex_pc            := if_id_pc
    id_ex_rs1           := Mux(control.io.PCSrc === Control.EXC, 0.U, rs1)
    id_ex_rs2           := Mux(control.io.PCSrc === Control.EXC, 0.U, rs2)
    id_ex_immgen        := Mux(control.io.PCSrc === Control.EXC, 0.U, extended)
    id_ex_rd            := Mux(control.io.PCSrc === Control.EXC, 0.U, inst_halt(11,7))
    id_ex_aluCtrl       := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.aluCtrl)
    id_ex_op2Ctrl       := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.op2Ctrl)
    id_ex_op1Ctrl       := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.op1Ctrl)
    id_ex_sttype        := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.sttype)
    id_ex_ldtype        := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.ldtype)
    id_ex_wbsrc         := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.wbsrc)
    id_ex_bt            := Mux(control.io.PCSrc === Control.EXC, 0.U, control.io.bt)

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
    
    //Branch should this be a reg?    

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
            io.fpgatest.pc_ex := if_id_pc
            io.fpgatest.halt := taken //|| halt
            io.fpgatest.decode_inst := inst_halt
            io.fpgatest.decode_pc := if_id_pc >> 2.U
    } else {
        io.fpgatest <> DontCare
    }
}