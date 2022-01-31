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

object hazard_prev_obj {
    def apply(rs1: UInt, rs2: UInt, rd_prev: UInt): Bool =  Mux(rs1.orR, rd_prev === rs1, false.B) || Mux(rs2.orR, rd_prev === rs2, false.B)
}

class HazardDetection extends Module {
    val io = IO(new HazardDetectionIO)

    io.nop := hazard_prev_obj(io.rs1, io.rs2, io.rd_prev) && io.prev_is_load
}

object HazardDetection {
    def apply(rs1: UInt, rs2: UInt, rd_prev: UInt, prev_is_load: Bool) = {
    val h = Module(new HazardDetection)
    h.io.rs1 := rs1
    h.io.rs2 := rs2
    h.io.rd_prev := rd_prev
    h.io.prev_is_load := prev_is_load 
    h.io.nop
    }
}

class HazardDetectionExtraHaltIO extends Bundle {
    val prev_prev_is_load = Input(Bool())
    val rd_prev_prev = Input(UInt(5.W))
}

class HazardDetectionExtraHalt extends Module {
    val io = IO(new Bundle{
        val s = new HazardDetectionIO
        val e = new HazardDetectionExtraHaltIO
    })

    io.s.nop := HazardDetection(io.s.rs1, io.s.rs2, io.s.rd_prev, io.s.prev_is_load) || HazardDetection(io.s.rs1, io.s.rs2, io.e.rd_prev_prev, io.e.prev_prev_is_load)
}

object HazardDetectionExtraHalt {
    def apply(rs1: UInt, rs2: UInt, rd_prev: UInt, rd_prev_prev: UInt, prev_is_load: Bool, prev_prev_is_load: Bool) = {
        val extrahalt = Module(new HazardDetectionExtraHalt)
        extrahalt.io.s.rs1 := rs1
        extrahalt.io.s.rs2 := rs2
        extrahalt.io.s.rd_prev := rd_prev
        extrahalt.io.e.rd_prev_prev := rd_prev_prev
        extrahalt.io.s.prev_is_load := prev_is_load
        extrahalt.io.e.prev_prev_is_load := prev_prev_is_load
        extrahalt.io.s.nop
    }
}

class HazardDetectionALUSplitIO extends Bundle {
    val rd_prev_prev = Input(UInt(5.W))
    val prev_is_arith = Input(Bool())
    val is_branch = Input(Bool())
}

class HazardDetectionALUSplit extends Module {
    val io = IO(new Bundle{
        val s = new HazardDetectionIO
        val a = new HazardDetectionALUSplitIO})

    val hazard_prev = hazard_prev_obj(io.s.rs1, io.s.rs2, io.s.rd_prev)
    val hazard_prev_prev = hazard_prev_obj(io.s.rs1, io.s.rs2, io.a.rd_prev_prev)

    val branch_prev_nop = hazard_prev && (io.s.prev_is_load || io.a.prev_is_arith)
    val branch_prev_prev_nop = hazard_prev_prev && io.s.prev_is_load
    
    val branch_nop = (branch_prev_nop || branch_prev_prev_nop) && io.a.is_branch
    val other_nop = HazardDetection(io.s.rs1, io.s.rs2, io.s.rd_prev, io.s.prev_is_load)

    io.s.nop := branch_nop || other_nop
}

/* class HazardDetectionALUSplit extends Module {
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
} */

object HazardDetectionALUSplit {
    def apply(rs1: UInt, rs2: UInt, rd_prev: UInt, rd_prev_prev: UInt, prev_is_arith: Bool, prev_is_load: Bool, is_branch: Bool) = {
        val alusplit = Module(new HazardDetectionALUSplit)
        alusplit.io.s.rs1 := rs1
        alusplit.io.s.rs2 := rs2
        alusplit.io.s.rd_prev := rd_prev
        alusplit.io.a.rd_prev_prev := rd_prev_prev
        alusplit.io.a.prev_is_arith := prev_is_arith
        alusplit.io.s.prev_is_load := prev_is_load
        alusplit.io.a.is_branch := is_branch
        alusplit.io.s.nop
    }
}

class HazardDetectionCombined extends Module {
    val io = IO(new Bundle{
        val s = new HazardDetectionIO
        val e = new HazardDetectionExtraHaltIO
        val a = new HazardDetectionALUSplitIO
        val rd_prev_prev_prev = Input(UInt(5.W))
        val prev_prev_prev_is_load = Input(Bool())
    })
    val hazard_prev_prev_prev = hazard_prev_obj(io.s.rs1, io.s.rs2, io.rd_prev_prev_prev) && io.prev_prev_prev_is_load && io.a.is_branch

    io.s.nop := HazardDetectionALUSplit(io.s.rs1, io.s.rs2, io.s.rd_prev, io.a.rd_prev_prev, io.a.prev_is_arith, io.s.prev_is_load, io.a.is_branch) || 
                HazardDetectionExtraHalt(io.s.rs1, io.s.rs2, io.s.rd_prev, io.e.rd_prev_prev, io.s.prev_is_load, io.e.prev_prev_is_load) ||
                hazard_prev_prev_prev
}