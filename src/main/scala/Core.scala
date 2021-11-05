package core

import chisel3._
import chisel3.util._
import chisel3.experimental.BaseModule
import Instructions._
import FPGAInstructions._

class CoreIO extends Bundle {
    val same = Output(Bool())
    val pc = Output(Bool())
    val sum = Output(Bool())
    val addi = Output(Bool())
}

class Core[T <: BaseModule with IMem](imemory: => T) extends Module {

    val correct_wb = VecInit(
        1.U(32.W),
        225.U(32.W),
        10.U(32.W),
        235.U(32.W),
        225.U(32.W),
        117.U(32.W),
        235.U(32.W),
        224.U(32.W),
        BigInt(3758096384L).U(32.W),
        225.U(32.W),                   
        BigInt(3774873600L).U(32.W),
        52.U(32.W),                     //pc + 4
        BigInt(3774873600L).U(32.W),
        0.U(32.W),                    
        0.U(32.W),
        0.U(32.W),
        1.U(32.W),
        225.U(32.W),
        225.U(32.W),
        76.U(32.W),
        225.U(32.W),
        225.U(32.W),
        226.U(32.W),
        1.U(32.W),
        225.U(32.W),
        71.U(32.W),
        0.U(32.W),
        BigInt(14745600L).U(32.W),
        255.U(32.W),
        225.U(32.W),
        57344.U(32.W),
        BigInt(4294959104L).U(32.W),    //already correct cause reads the same as prev ld just interprets it differently
        BigInt(4294959104L).U(32.W),
        57344.U(32.W),
        BigInt(3758039040L).U(32.W),
        BigInt(4294967288L).U(32.W),
        BigInt(3758096400L).U(32.W),
        BigInt(3758096384L).U(32.W),
        0.U(32.U),
        BigInt(4294963200L).U(32.W),
        224.U(32.W),
        224.U(32.W),
        271.U(32.W),
        BigInt(4294967264L).U(32.W),
        BigInt(4294967266L).U(32.W),
        0.U,
        1.U,
        1.U,
        0.U,
        102400.U(32.W),
        4264.U(32.W),
        176.U(32.W),
        0.U(32.W)
        )

    val io = IO(new CoreIO)

    val dataflow = Module(new Dataflow)
    val dMem = Module(new Memory)
    val iMem = Module(imemory)

    iMem.io.req.bits.addr := dataflow.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dataflow.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dataflow.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dataflow.io.iMemIO.req.valid

    dataflow.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dataflow.io.iMemIO.resp.valid := iMem.io.resp.valid

    dMem.io.req.bits.addr := dataflow.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dataflow.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dataflow.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dataflow.io.dMemIO.req.valid

    dataflow.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dataflow.io.dMemIO.resp.valid := dMem.io.resp.valid

    val (cntr, done) = Counter(true.B, 53)

    io.addi := (dataflow.io.fpgatest.wb === 10.U)
    io.pc := Mux(dataflow.io.fpgatest.pc>>2.U < 32.U, dataflow.io.fpgatest.pc(2), false.B)
    val pc_prev = RegInit(0.U)

    val wb = dataflow.io.fpgatest.wb
    io.sum := (correct_wb(cntr) === wb)
    io.same := MuxLookup(cntr, dataflow.io.fpgatest.pc === pc_prev + 4.U, Seq(
                12.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                13.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                16.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                17.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                20.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                21.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                24.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                25.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                27.U -> (dataflow.io.fpgatest.pc === pc_prev),
                30.U -> (dataflow.io.fpgatest.pc === pc_prev),
                32.U -> (dataflow.io.fpgatest.pc === pc_prev),
                41.U -> (dataflow.io.fpgatest.pc === pc_prev),
                44.U -> (dataflow.io.fpgatest.pc === pc_prev),
                52.U -> (dataflow.io.fpgatest.pc === 4264.U + 16.U)
                ))

    pc_prev := dataflow.io.fpgatest.pc
}

object CoreFPGAOutHardCodedInsts extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemoryVec), args)
}

object CoreFPGAOutInitMem extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemory("test.mem")), args)
}