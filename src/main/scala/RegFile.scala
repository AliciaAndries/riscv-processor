package core

import chisel3._
import chisel3.util._

/* 
    you need to be able to access RegFile 2 times in 1 clock-cycle when pipelined cause of the writeback and decoding part && could be same addr
    would it be benificial for Dmemory? No because there is only max 1 Dmem access in 1 clockcycle
    but important if you use Mem do you read the old or new version?
*/

class RegFileIO extends Bundle {
    val raddr1 = Input(UInt(5.W)) //addr is 5 wide
    val raddr2 = Input(UInt(5.W))
    val waddr = Input(UInt(5.W))
    val wdata = Input(UInt(32.W))
    val wen = Input(Bool())
    val rs1 = Output(UInt(32.W))
    val rs2 = Output(UInt(32.W))
}

class RegFile extends Module {
    val io = IO(new RegFileIO)

    val reg = Mem(32, UInt(32.W))  //32 registers -> only 5 bit addr so cant have any bigger

    when(io.wen && io.waddr.orR){
        reg(io.waddr) := io.wdata
    }

    io.rs1 := Mux(io.raddr1.orR, reg(io.raddr1), 0.U)
    io.rs2 := Mux(io.raddr2.orR, reg(io.raddr2), 0.U)
}
