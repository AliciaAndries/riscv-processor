package core

import chisel3._
import chisel3.util._

class RegFileIO extends Bundle {
    val raddr1 = Input(UInt(5.W)) //addr is 5 wide
    val raddr2 = Input(UInt(5.W))
    val waddr = Input(UInt(32.W))
    val wdata = Input(UInt(32.W))
    val wen = Input(Bool())
    val rs1 = Output(UInt(32.W))
    val rs2 = Output(UInt(32.W))
}

class RegFile extends Module {
    val io = IO(new RegFileIO)

    val reg = Reg(Vec(32, UInt(32.W))) //32 registers -> only 5 bit addr so cant have any bigger

    reg(0) := 0.U //reg 0 always has to be 0 cause it handy to have a 0 around

    when(io.wen && io.waddr.orR){
        reg(io.waddr) := io.wdata
    }

    io.rs1 := reg(io.raddr1)
    io.rs2 := reg(io.raddr2)
}

//what if waddr == rs1 or rs2? --> do you fix this with wen, do you need to fix it --> draw it mate