package core

import chisel3._
import chisel3.util._
import chisel3.experimental.BaseModule
import Instructions._
import FPGAInstructions._

class CoreIO extends Bundle {
    val fpgatest = new FpgaTestIO
}

class Core[T <: BaseModule with IMem](imemory: => T) extends Module {

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

    io.fpgatest := dataflow.io.fpgatest
}

object CoreFPGAOutHardCodedInsts extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemoryVec), args)
}

object CoreFPGAOutInitMem extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemory("test.mem")), args)
}