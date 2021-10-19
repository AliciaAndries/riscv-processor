package core

import chisel3._
import chisel3.util._
import ALU._
import Control._

object TestInst {
    val rnd = new scala.util.Random
    def rand_fn7 = (rnd.nextInt(1 << 7)).U(7.W)
    def rand_rs2 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_rs1 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_fn3 = (rnd.nextInt(1 << 3)).U(3.W) 
    def rand_rd  = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)


    //(13,10)(9)(8,6)(5)(4,3)(2,0)
    val compares = Array(
            Cat(ADD_ALU, op2Imm,ImmGen.I, Pl4, ST_XX, LD_LB),       //4,1,3,1,2,3
            Cat(ADD_ALU, op2Imm, ImmGen.S, Pl4, ST_SB, LD_XX),
            Cat(ADD_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            Cat(SUB_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            Cat(OR_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            Cat(AND_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            Cat(ADD_ALU, op2Reg,ImmGen.B, Br, ST_XX, LD_XX)
            )

    val insts : Seq[UInt]  = Seq(
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LB, rand_rd, Opcode.LOAD),
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SB, rand_rd, Opcode.STORE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
            Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.RTYPE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.RTYPE),
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BEQ, rand_rd, Opcode.BRANCH)
            )
}