package core

import chisel3._
import chisel3.util._

object ForwardingUnit {
    val CUR = 0.U(3.W)
    val EX_MEM_ALU = 1.U(3.W)
    val MEM_WB_ALU = 2.U(3.W)
    val WB_OUT_ALU = 3.U(3.W)
    val EX_MEM_MEM = 4.U(3.W)
    val MEM_WB_MEM = 5.U(3.W)
    val WB_OUT_MEM = 6.U(3.W)
}

class ForwardingUnitIO extends Bundle {
    val rs1_cur = Input(UInt(5.W))
    val rs2_cur = Input(UInt(5.W))
    val rd_ex_mem = Input(UInt(5.W))
    val rd_mem_wb = Input(UInt(5.W))
    val rd_wb_out = Input(UInt(5.W))
    val wbsrc_ex_mem = Input(UInt(2.W))
    val wbsrc_mem_wb = Input(UInt(2.W))
    val wbsrc_wb_out = Input(UInt(2.W))
    val reg1 = Output(UInt(2.W))
    val reg2 = Output(UInt(2.W))
}

class ForwardingUnit extends Module {
    val io = IO(new ForwardingUnitIO)
    val temp1 = Wire(UInt(3.W))
    temp1 := Mux(rs1_cur === wbsrc_wb_out, WB_OUT_ALU,
                Mux(rs1_cur === rd_mem_wb, MEM_WB_ALU,
                Mux(rs1_cur === rd_ex_mem, EX_MEM_ALU, CUR))
    when(temp1.orR){
        temp1 := MuxLookup()
    }
    reg2 := Mux(rs2_cur === rd_out_of_pipeline, PREV_PREV_PREV,
                Mux(rs2_cur === rd_wb_stage, PREV_PREV,
                Mux(rs2_cur === rd_mem_stage, PREV, CUR))
}