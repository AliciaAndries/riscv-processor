package core

import chisel3._
import chisel3.util._
import Instructions._

class CoreIO extends Bundle {

}

class Core extends Module {
    val io = IO(new CoreIO)

    val dataflow = Module(new Dataflow)
    val dMem = Module(new Memory)
    val iMem = Module(new Memory)

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
}

object CoreNoIOdriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core, args)
}