package core

import chisel3._
import chisel3.util._

class DataflowIO() extends Bundle {
    val instrRegIO = Flipped(new InstructionRegIO)
    val dMemIO = Flipped(new DMemoryIO)
} 

class Dataflow() extends Module {
    val io = IO(new DataflowIO)

    val instructionReg = Module(new InstructionReg("/filepath"))
    val immGen = Module(new ImmGen)
    val control = Module(new Control)
    val regFile = Module(new RegFile)
    val alu = Module(new ALU)

    val pc = Reg(UInt(32.W)) //32 bit so< 4 byte instructions TODO: should it be init to 0?

    

    //fetch instruction
    io.instrRegIO.inst_addr := pc
    val inst = instructionReg.io.inst

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
    alu.io.op1 := rs1
    alu.io.op2 := Mux(control.io.aluInCtrl === Control.op2Imm, extended, rs2)
    val aluresult = alu.io.result
    val taken = alu.io.zero

    //Branch
    val tBranchaddr = extended + pc

    //pc multiplexer
    val mpc = Mux(control.io.PCSrc === Control.Br && taken === 1.B, tBranchaddr, pc + 4.U)

    //update pc
    pc := mpc

    //Memory
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
    val rdata = MuxLookup(control.io.ldtype, memrdata,
        Array(
            Control.LD_LH  -> (memrdata(15,0).asSInt),   //SInt sign extends the value
            Control.LD_LHU -> (memrdata(15,0).zext),    //zext is UInt feature
            Control.LD_LB  -> (memrdata(7,0).asSInt),
            Control.LD_LBU -> (memrdata(7,0).zext)
        ))
    //depending on LD you need to sign/0 extend

    //write back
    val regFile.io.waddr = inst(11,7)
    /* regFile.io.wen = control something */
    /* val regFile.io.wdata = dRData or  */
    
}