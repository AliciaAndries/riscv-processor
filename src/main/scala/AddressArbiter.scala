package core

import chisel3._
import chisel3.util._
import MemorySize._

class AddressArbiterIO extends Bundle {
    val addr = Input(UInt(32.W))
    val req = Input(Bool())
    val memReqValid = Output(Bool())
    val ioReqValid = Output(Bool())
}

class AddressArbiter extends Module {
    val io = IO(new AddressArbiterIO)

    val ad = Wire(Bool())
    ad := false.B

    when(io.addr < BMemBytes.U*4.U){
        ad := true.B
    }
    io.ioReqValid := io.addr === BMemBytes.U*4.U
    io.memReqValid := ad && io.req
}