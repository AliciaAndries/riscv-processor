
package core

import chisel3._
import chisel3.util._

class DataflowNotPipelined(test : Boolean = false) extends Module {
    val io = IO(new DataflowTestIO)

    //val instructionReg = Module(new InstructionReg("/filepath")) maybe use this for testing put it in the core
    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALU)
    val branchLogic = Module(new BranchLogic)

    val nop = "b00000000000000000000000000010011".U

    val mpc = WireDefault(PC_CONSTS.pc_init)
    val pc = RegInit(PC_CONSTS.pc_init - 4.U) //32 bit so< 4 byte instructions TODO: should it be init to 0?
    val start = RegInit(true.B)
    
    val test_halt = WireDefault(false.B)
    if(test){
        test_halt := io.fpgatest.halt_in
    }

    //make initialisation phase? 

    //fetch instruction
    io.iMemIO.req.bits.addr := mpc
    io.iMemIO.req.valid := true.B
    io.iMemIO.req.bits.mask := 0.U
    io.iMemIO.req.bits.data := DontCare
    val inst = Mux(start || test_halt, nop, io.iMemIO.resp.bits.data)

    //decode instruction
    control.io.inst := inst
    
    start := false.B
    
    //immgen extend 12 bits

    immGen.io.inst := inst
    immGen.io.immGenCtrl := control.io.immGenCtrl
    val extended = immGen.io.out

    //get registers
    val raddr1 = inst(19,15)
    val raddr2 = inst(24,20) 
    regFile.io.raddr1 := raddr1
    regFile.io.raddr2 := raddr2
    val rs1 = regFile.io.rs1            //always exists
    val rs2 = regFile.io.rs2            //only R&S-Type

    //ALU
    alu.io.operation := control.io.aluCtrl
    val aluop1 = Mux(control.io.op1Ctrl === Control.op1Reg, rs1, pc)
    val aluop2 = Mux(control.io.op2Ctrl === Control.op2Imm, extended, rs2)
    alu.io.op1 := aluop1
    alu.io.op2 := aluop2
    
    val aluresult = alu.io.result
    branchLogic.io.comp := alu.io.comp
    branchLogic.io.bt := control.io.bt
    val taken = branchLogic.io.taken
    

    //fpga test output
    //io.fpgatest.led := taken
    
    //Branch
    val tBranchaddr = extended + Mux(control.io.PCSrc === Control.Jump && control.io.op1Ctrl === Control.op1Reg, rs1, pc)

    //pc multiplexer
    val ld_another_clk = RegInit(true.B)
    
    mpc := Mux((control.io.PCSrc === Control.Br && taken) || control.io.PCSrc === Control.Jump, tBranchaddr, 
                Mux(control.io.PCSrc === Control.EXC, PC_CONSTS.pc_expt,
                Mux(((control.io.PCSrc === Control.Pl0) && ld_another_clk) || test_halt, pc, pc + 4.U)))
    
    when(control.io.ldtype.orR){
        ld_another_clk := !ld_another_clk
    }.otherwise{
        ld_another_clk := true.B
    }
    
    //Memory
    //The SW, SH, and SB instructions store 32-bit, 16-bit, and 8-bit values from the low bits of register rs2 to memory. --> is this correct?
    io.dMemIO.req.bits.addr := aluresult
    io.dMemIO.req.bits.data := rs2

    //W -> moffset = 0, H -> moffset = 0 or 2, B -> mmoffset =  0-3
    val moffset = Mux(control.io.sttype === Control.ST_SW || control.io.ldtype === Control.LD_LW, 0.U,
                    Mux(control.io.sttype === Control.ST_SH || control.io.ldtype === Control.LD_LH || control.io.ldtype === Control.LD_LHU,  
                        alu.io.result(1,0)&"b10".U, alu.io.result(1,0)))
    val doffset = moffset << 3
    
    io.dMemIO.req.bits.data := rs2 << doffset
    io.dMemIO.req.bits.mask := MuxLookup(control.io.sttype, "b0000".U, 
        Array(
            Control.ST_SW -> ("b1111".U),
            Control.ST_SH -> ("b11".U << moffset),      //what if someone puts in addr(1,0) = "01" or "11", no longer aligned so maybe throw a fit?
            Control.ST_SB -> ("b1".U << moffset)
        ))
    io.dMemIO.req.valid := control.io.sttype.orR || control.io.ldtype.orR 

    val memrdata = Mux(io.dMemIO.resp.valid, io.dMemIO.resp.bits.data, 0.U) >> doffset  //offset cause if you want to read at alu(1,0) = "10" [LB] you need to move result to right to then use the mask below
    val rdata = MuxLookup(control.io.ldtype, memrdata.asSInt,
        Array(
            Control.LD_LH  -> (memrdata(15,0).asSInt),   //SInt sign extends the value
            Control.LD_LHU -> ((memrdata(15,0).zext).asSInt),    //zext is UInt feature
            Control.LD_LB  -> (memrdata(7,0).asSInt),
            Control.LD_LBU -> ((memrdata(7,0).zext).asSInt)
        ))

    //write back
    val rd = inst(11,7)
    regFile.io.waddr := rd      //rd part of instruction
    regFile.io.wen := control.io.wbsrc.orR
    val wbdata = Mux(control.io.wbsrc === Control.WB_MEM, rdata.asUInt, 
                            Mux(control.io.wbsrc === Control.WB_PC, pc + 4.U, aluresult))
    regFile.io.wdata := wbdata
    

    //update pc
    pc := mpc


    if(test){
        when(io.fpgatest.halt_in){
            regFile.io.raddr1 := io.fpgatest.reg_addr
            io.fpgatest.reg_data := regFile.io.rs1

        }.otherwise{
            io.fpgatest.reg_data := 0.U
        }
        /* regFile.io.raddr1 := io.fpgatest.reg_addr
        io.fpgatest.reg_data := regFile.io.rs1 */
        io.fpgatest.rs1 := raddr1
        io.fpgatest.rs2 := raddr2
        io.fpgatest.rs1data := aluop1
        io.fpgatest.rs2data := aluop2
        io.fpgatest.aluresult := aluresult
        io.fpgatest.id_ex_rd := rd

        io.fpgatest.wb := wbdata
        io.fpgatest.pc := pc

        io.fpgatest.reg_input1 := 0.U
        io.fpgatest.reg_input2 := 0.U
        io.fpgatest.pc_ex := pc
        io.fpgatest.halt := !ld_another_clk
        io.fpgatest.decode_inst := inst
        io.fpgatest.decode_pc := pc
    } else {
        io.fpgatest <> DontCare
    }
}