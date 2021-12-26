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

class HazardDetectionALUSplitIO extends HazardDetectionIO {
    val rd_prev_prev = Input(UInt(5.W))
    val prev_is_arith = Input(Bool())
    val is_branch = Input(Bool())
}

class HazardDetectionALUSplit extends Module {
    val io = IO(new HazardDetectionALUSplitIO)

    val hazard_prev = Mux(io.rs1.orR, io.rd_prev === io.rs1, false.B) || Mux(io.rs2.orR, io.rd_prev === io.rs2, false.B)
    val hazard_prev_prev = Mux(io.rs1.orR, io.rd_prev_prev === io.rs1, false.B) || Mux(io.rs2.orR, io.rd_prev_prev === io.rs2, false.B)

    //val hazard_prev = io.rd_prev === io.rs1 || io.rd_prev === io.rs2
    //val hazard_prev_prev = io.rd_prev_prev === io.rs1 || io.rd_prev_prev === io.rs2

    val branch_prev_nop = hazard_prev && (io.prev_is_load || io.prev_is_arith)
    val branch_prev_prev_nop = hazard_prev_prev && io.prev_is_load
    
    val branch_nop = (branch_prev_nop || branch_prev_prev_nop) && io.is_branch
    val other_nop = (hazard_prev) && io.prev_is_load

    io.nop := branch_nop || other_nop
}