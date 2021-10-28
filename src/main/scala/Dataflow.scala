package core

import chisel3._
import chisel3.util._

class TestIO() extends Bundle {
    val wb_data = Output(UInt(32.W))
    val aluzero = Output(Bool())
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

class Dataflow(test : Boolean = false) extends Module {
    val io = IO(new DataflowIO)

    //val instructionReg = Module(new InstructionReg("/filepath")) maybe use this for testing put it in the core
    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALU)
    val branchLogic = Module(new BranchLogic)

    val pc = RegInit(0.U(32.W)) //32 bit so< 4 byte instructions TODO: should it be init to 0?
    io.fpgatest.pc := pc

    //make initialisation phase? 

    //fetch instruction
    io.iMemIO.req.bits.addr := pc
    io.iMemIO.req.valid := true.B
    io.iMemIO.req.bits.mask := 0.U
    io.iMemIO.req.bits.data := DontCare
    val inst = io.iMemIO.resp.bits.data

    //decode instruction
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

    //ALU
    alu.io.operation := control.io.aluCtrl
    alu.io.op1 := Mux(control.io.op2Ctrl === Control.op1Reg, rs1, pc)
    alu.io.op2 := Mux(control.io.op2Ctrl === Control.op2Imm, extended, rs2)
    
    val aluresult = alu.io.result
    branchLogic.io.comp := alu.io.comp
    branchLogic.io.bt := control.io.bt
    val taken = branchLogic.io.taken
    io.fpgatest.zero := taken

    //fpga test output
    //io.fpgatest.led := taken
    //io.fpgatest.aluresult := aluresult

    //Branch
    val tBranchaddr = extended + pc

    //pc multiplexer
    val mpc = Mux(control.io.PCSrc === Control.Br && taken, tBranchaddr, 
                Mux(control.io.PCSrc === Control.Jump, aluresult, 
                Mux(control.io.PCSrc === Control.Pl0, pc, pc + 4.U)))   //TODO will this not just load forever

    //Memory
    //The SW, SH, and SB instructions store 32-bit, 16-bit, and 8-bit values from the low bits of register rs2 to memory. --> is this correct?
    io.dMemIO.req.bits.addr := aluresult
    io.dMemIO.req.bits.data := rs2
    val moffset = alu.io.result(1,0)
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
    regFile.io.waddr := inst(11,7)       //rd part of instruction
    regFile.io.wen := control.io.wbsrc.orR
    regFile.io.wdata := Mux(control.io.wbsrc === Control.WB_MEM, rdata.asUInt, 
                            Mux(control.io.wbsrc === Control.WB_ALU, aluresult, pc + 4.U))
    io.fpgatest.wb := Mux(control.io.ldtype.orR, rdata.asUInt, aluresult)

    //update pc
    pc := mpc
    
    if(test){
        io.test.raddr1 := inst(19,15)
        io.test.raddr2 := inst(24,20) 
        io.test.rs2 := rs2
        io.test.op1 := rs1
        io.test.op2 := Mux(control.io.op2Ctrl === Control.op2Imm, extended, rs2)
        io.test.aluzero := alu.io.comp
        io.test.rwdata := inst(11,7)
        io.test.wb_data := Mux(control.io.ldtype.orR, rdata.asUInt, aluresult)
    } else{
        io.test <> DontCare
    }


}

object Dataflowdriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Dataflow, args)
}