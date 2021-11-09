package core

import chisel3._
import chisel3.util._

object ForwardingUnit {
    val CUR = 0.U(3.W)
    val EX_MEM_ALU = 1.U(3.W)
    val MEM_WB = 2.U(3.W)
    val WB_OUT = 3.U(3.W)
}

class ForwardingUnitIO extends Bundle {
    val rs1_cur = Input(UInt(5.W))
    val rs2_cur = Input(UInt(5.W))
    val rd_ex_mem = Input(UInt(5.W))
    val rd_mem_wb = Input(UInt(5.W))
    val rd_wb_out = Input(UInt(5.W))
    val reg1 = Output(UInt(3.W))
    val reg2 = Output(UInt(3.W))
}

class ForwardingUnit extends Module {
    val io = IO(new ForwardingUnitIO)
    io.reg1 :=  Mux(io.rs1_cur === 0.U, ForwardingUnit.CUR,
                Mux(io.rs1_cur === io.rd_ex_mem, ForwardingUnit.EX_MEM_ALU,
                Mux(io.rs1_cur === io.rd_mem_wb, ForwardingUnit.MEM_WB,
                Mux(io.rs1_cur === io.rd_wb_out, ForwardingUnit.WB_OUT, ForwardingUnit.CUR))))

    io.reg2 := Mux(io.rs2_cur === 0.U, ForwardingUnit.CUR,
                Mux(io.rs2_cur === io.rd_ex_mem, ForwardingUnit.EX_MEM_ALU,
                Mux(io.rs2_cur === io.rd_mem_wb, ForwardingUnit.MEM_WB,
                Mux(io.rs2_cur === io.rd_wb_out, ForwardingUnit.WB_OUT, ForwardingUnit.CUR))))
}