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
            Cat(ADD_ALU, op2Imm,ImmGen.I, Pl4, ST_XX, LD_LB, WB_MEM),       //4,1,3,1,2,3,1
            Cat(ADD_ALU, op2Imm, ImmGen.S, Pl4, ST_SB, LD_XX, WB_F),
            Cat(ADD_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU),
            Cat(SUB_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU),
            Cat(OR_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU),
            Cat(AND_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU),
            Cat(XXX_ALU, op2Reg,ImmGen.B, Br, ST_XX, LD_XX, WB_F),
            Cat(ADD_ALU, op2Imm, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU)
            )

    val insts : Seq[UInt]  = Seq(
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LB, rand_rd, Opcode.LOAD),         //LB
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LBU, rand_rd, Opcode.LOAD),        //LBU
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LH, rand_rd, Opcode.LOAD),         //LH
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LHU, rand_rd, Opcode.LOAD),        //LHU
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LW, rand_rd, Opcode.LOAD),         //LW
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SB, rand_rd, Opcode.STORE),        //SB
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SH, rand_rd, Opcode.STORE),        //SH
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SW, rand_rd, Opcode.STORE),        //SW
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),       //ADD
            Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),       //SUB
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.RTYPE),        //OR
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.XOR, rand_rd, Opcode.RTYPE),       //XOR
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.RTYPE),       //AND
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLT, rand_rd, Opcode.RTYPE),       //SLT
            Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLT, rand_rd, Opcode.RTYPE),       //SLTU

            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BEQ, rand_rd, Opcode.BRANCH),      //BEQ
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BNE, rand_rd, Opcode.BRANCH),      //BNE
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BLT, rand_rd, Opcode.BRANCH),      //BLT
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BLTU, rand_rd, Opcode.BRANCH),     //BLTU
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BGE, rand_rd, Opcode.BRANCH),      //BGE
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BGEU, rand_rd, Opcode.BRANCH),     //BGEU
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.ITYPE)        //ADDI
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.ITYPE),        //ORI
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.XOR, rand_rd, Opcode.ITYPE),       //XORI
            Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.ITYPE),       //ANDI
            

            )
}