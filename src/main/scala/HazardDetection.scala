package core

import chisel3._
import chisel3.util._

class HazardDetectionIO extends Bundle {
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd_prev = Input(UInt(5.W))
    val prev_is_load = Input(Bool())
    val nop = Output(Bool())
}

class HazardDetection extends Module {
    val io = IO(new HazardDetectionIO)

    io.nop := (io.rs1 === io.rd_prev || io.rs2 === io.rd_prev) && io.prev_is_load
}