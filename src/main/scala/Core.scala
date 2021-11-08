package core

import chisel3._
import chisel3.util._
import chisel3.experimental.BaseModule
import Instructions._
import FPGAInstructions._

class CoreIO extends Bundle {
    val fpgatest = new FpgaTestIO
    val ledio = Output(UInt(1.W))
}

class Core[T <: BaseModule with IMem](imemory: => T) extends Module {

    val io = IO(new CoreIO)

    val dataflow = Module(new Dataflow)
    val dMem = Module(new Memory)
    val iMem = Module(imemory)
    val addressArbiter = Module(new AddressArbiter)

    iMem.io.req.bits.addr := dataflow.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dataflow.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dataflow.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dataflow.io.iMemIO.req.valid

    dataflow.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dataflow.io.iMemIO.resp.valid := iMem.io.resp.valid


    addressArbiter.io.addr := dataflow.io.dMemIO.req.bits.addr
    addressArbiter.io.req := dataflow.io.dMemIO.req.valid

    dMem.io.req.bits.addr := dataflow.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dataflow.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dataflow.io.dMemIO.req.bits.mask
    dMem.io.req.valid := addressArbiter.io.memReqValid

    dataflow.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dataflow.io.dMemIO.resp.valid := dMem.io.resp.valid

    io.fpgatest := dataflow.io.fpgatest
    io.ledio := Mux(addressArbiter.io.ioReqValid && !addressArbiter.io.memReqValid, dataflow.io.dMemIO.req.bits.data(1), 0.U)
    dataflow.io.io_out_of_bounds := !addressArbiter.io.ioReqValid
}

object CoreFPGAOutHardCodedInsts extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemoryVec), args)
}

object CoreFPGAOutInitMem extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemory("test.mem")), args)
}