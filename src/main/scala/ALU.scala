package core

import chisel3._
import chisel3.util._

object ALU { //in riscv mini this is for some reason an object but like why?
    val AND_ALU = 0.U(4.W)
    val OR_ALU = 1.U(4.W)
    val ADD_ALU = 2.U(4.W)
    val SUB_ALU = 6.U(4.W)
    val XXX_ALU = 15.U(4.W)
}

class ALUIO extends Bundle {
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val operation = Input(UInt(4.W))
    val zero = Output(Bool())
    val result = Output(UInt(32.W))
}

class ALU extends Module {
    val io = IO(new ALUIO)

    val and_res = io.op1 & io.op2
    val or_res = io.op1 | io.op2
    val add_res = io.op1 + io.op2
    val sub_res = io.op1 - io.op2

    io.result := Mux(io.operation === ALU.AND_ALU, and_res,
                    Mux(io.operation === ALU.OR_ALU, or_res,
                    Mux(io.operation === ALU.ADD_ALU, add_res, sub_res)))
    
    io.zero := Mux(sub_res.orR, 0.B, 1.B)
    }