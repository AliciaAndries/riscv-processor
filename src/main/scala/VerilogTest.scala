package core

import chisel3._
import chisel3.util._
import Instructions._

/* class VerilogTest extends Module {
    val io = IO(new CoreIO)
    val pc = RegInit(0.U(32.W))

    val extended = 10.U
    //Branch
    val tBranchaddr = extended + pc

    //pc multiplexer
    val mpc = Mux(io.dontrun, tBranchaddr, pc + 4.U)

    //update pc
    pc := mpc

    io.pc := pc

    //io.led := false.B

}

object VerilogTest extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new VerilogTest, args)
} */