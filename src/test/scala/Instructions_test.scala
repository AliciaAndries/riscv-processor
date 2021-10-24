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


    //(14,11)(10)(9,7)(6)(5,4)(3,1)(0)
    val compares = Array(
            Cat(ADD_ALU, op2Imm,ImmGen.I, Pl4, ST_XX, LD_LB, WB_T),       //4,1,3,1,2,3,1
            Cat(ADD_ALU, op2Imm, ImmGen.S, Pl4, ST_SB, LD_XX, WB_F),
            Cat(ADD_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_T),
            Cat(SUB_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_T),
            Cat(OR_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_T),
            Cat(AND_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_T),
            Cat(ADD_ALU, op2Reg,ImmGen.B, Br, ST_XX, LD_XX, WB_F),
            Cat(ADD_ALU, op2Imm, ImmGen.I, Pl4, ST_XX, LD_XX, WB_T)
            )

    val insts : Seq[UInt]  = Seq(
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LB, rand_rd, Opcode.LOAD),
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SB, rand_rd, Opcode.STORE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
            Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.RTYPE),
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.RTYPE),
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BEQ, rand_rd, Opcode.BRANCH),
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.ITYPE)
            )
}