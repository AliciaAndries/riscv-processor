package core

import chisel3._
import chisel3.util._

import ALU._

object ALU { //in riscv mini this is for some reason an object but like why?
    val AND_ALU = 0.U(4.W)
    val OR_ALU = 1.U(4.W)
    val XOR_ALU = 2.U(4.W)
    val ADD_ALU = 3.U(4.W)
    val SUB_ALU = 4.U(4.W)
    val SLT_ALU = 5.U(4.W)
    val SLTU_ALU = 6.U(4.W)
    val SLL_ALU = 7.U(4.W)
    val SRL_ALU = 8.U(4.W)
    val SRA_ALU = 9.U(4.W)
    val SGT_ALU = 10.U(4.W)     //TODO: greater than may be needed for BGE
    val SGTU_ALU = 11.U(4.W)
    val XXX_ALU = 15.U(4.W)
}

class ALUBasicIO extends Bundle{
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val operation = Input(UInt(4.W))
    val result = Output(UInt(32.W))
}

class ALUWithBranchIO extends ALUBasicIO{
    val comp = Output(Bool())
}

class ALUIO extends Bundle {
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val operation = Input(UInt(4.W))
    val comp = Output(Bool())
    val result = Output(UInt(32.W))
}

class ALU extends Module {
    val io = IO(new ALUIO)

    val and_res = io.op1 & io.op2
    val or_res = io.op1 | io.op2
    val xor_res = io.op1 ^ io.op2
    val add_res = io.op1 + io.op2
    val sub_res = io.op1 - io.op2
    val slt_res = Mux(io.op1.asSInt < io.op2.asSInt, 1.U, 0.U)
    val sltu_res = Mux(io.op1 < io.op2, 1.U, 0.U)         //pseudo instruction SNEZ, if op2 === 0 return 0
    val sgt_res = Mux(io.op1.asSInt >= io.op2.asSInt, 1.U, 0.U)
    val sgtu_res = Mux(io.op1 >= io.op2, 1.U, 0.U)
    val sll_res = io.op1 << io.op2(4,0)
    val srl_res = io.op1 >> io.op2(4,0)
    val sra_res = io.op1.asSInt >> io.op2(4,0)

    io.result := MuxLookup(io.operation, io.op2,
                    Seq(
                        AND_ALU -> and_res,
                        OR_ALU -> or_res,
                        XOR_ALU -> xor_res,
                        ADD_ALU -> add_res,
                        SUB_ALU -> sub_res,
                        SLT_ALU -> slt_res,
                        SLTU_ALU -> sltu_res,
                        SLL_ALU -> sll_res,
                        SRL_ALU -> srl_res,
                        SRA_ALU -> sra_res.asUInt,
                    ))
    
    io.comp := MuxLookup(io.operation, !sub_res.orR,
                    Seq(
                        SLT_ALU -> slt_res(0),
                        SLTU_ALU -> sltu_res(0),
                    ))
    }

class ALUBasic extends Module{
    val io = IO(new ALUBasicIO)

    val and_res = io.op1 & io.op2
    val or_res = io.op1 | io.op2
    val xor_res = io.op1 ^ io.op2
    val add_res = io.op1 + io.op2
    val sub_res = alu_sub(io.op1, io.op2)
    val slt_res =  alu_slt(io.op1, io.op2)
    val sltu_res = alu_sltu(io.op1, io.op2)         //pseudo instruction SNEZ, if op2 === 0 return 0
    val sll_res = io.op1 << io.op2(4,0)
    val srl_res = io.op1 >> io.op2(4,0)
    val sra_res = io.op1.asSInt >> io.op2(4,0)

    io.result := MuxLookup(io.operation, io.op2,
                    Seq(
                        AND_ALU -> and_res,
                        OR_ALU -> or_res,
                        XOR_ALU -> xor_res,
                        ADD_ALU -> add_res,
                        SUB_ALU -> sub_res,
                        SLT_ALU -> slt_res,
                        SLTU_ALU -> sltu_res,
                        SLL_ALU -> sll_res,
                        SRL_ALU -> srl_res,
                        SRA_ALU -> sra_res.asUInt,
                    ))
} 

object ALUBasic{
    def apply(op1: UInt, op2: UInt, operation: UInt) = {
    val alub = Module(new ALUBasic)
    alub.io.op1 := op1
    alub.io.op2 := op2
    alub.io.operation := operation
    alub.io.result
  }
}

object alu_sub{
    def apply(op1: UInt, op2:UInt):UInt = op1 - op2
}

object alu_slt{
    def apply(op1: UInt, op2:UInt):UInt = Mux(op1.asSInt < op2.asSInt, 1.U, 0.U)
}

object alu_sltu{
    def apply(op1: UInt, op2:UInt):UInt = Mux(op1 < op2, 1.U, 0.U) 
}

class ALUWithBranch extends Module{
val io = IO(new ALUWithBranchIO)

    val sub_res = alu_sub(io.op1, io.op2)
    val slt_res = alu_slt(io.op1, io.op2)
    val sltu_res = alu_sltu(io.op1, io.op2)

    val result = ALUBasic(io.op1, io.op2, io.operation)
    io.result := result

    io.comp := MuxLookup(io.operation, !sub_res.orR,
                    Seq(
                        SLT_ALU -> slt_res(0),
                        SLTU_ALU -> sltu_res(0)
                    ))
}