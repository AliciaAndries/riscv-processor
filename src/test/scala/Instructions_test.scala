package core

import chisel3._
import chisel3.util._
import ALU._
import Control._
import Branch._

object TestInst {
    val rnd = new scala.util.Random
    def rand_fn7 = (rnd.nextInt(1 << 7)).U(7.W)
    def rand_rs2 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_rs1 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_fn3 = (rnd.nextInt(1 << 3)).U(3.W) 
    def rand_rd  = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)


    //(20,17)(16)(15)(14,12)(11,10)(9,8)(7,5)(4,3)(2,0)
    val compares = Array(
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LB, WB_MEM, XX),       //4,1,1,3,2,2,3,2,3
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LBU, WB_MEM, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LH, WB_MEM, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LHU, WB_MEM, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LW, WB_MEM, XX),

        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SB, LD_XX, WB_F, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SH, LD_XX, WB_F, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SW, LD_XX, WB_F, XX),

        Cat(ADD_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SUB_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLL_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SRL_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SRA_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(OR_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(XOR_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(AND_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLT_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLTU_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, XX),

        Cat(XXX_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, EQ),
        Cat(XXX_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, NE),
        Cat(SLT_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, LT),
        Cat(SLTU_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, LTU),
        Cat(SGT_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, GE),
        Cat(SGTU_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, GEU),

        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLL_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SRL_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SRA_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(OR_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(XOR_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(AND_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLT_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(SLTU_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, XX),

        Cat(XXX_ALU, op2Imm, op1Reg, ImmGen.U, Pl4, ST_XX, LD_XX, WB_ALU, XX),
        Cat(ADD_ALU, op2Imm, op1PC, ImmGen.U, Pl4, ST_XX, LD_XX, WB_ALU, XX),

        Cat(ADD_ALU, op2Imm, op1PC, ImmGen.J, Jump, ST_XX, LD_XX, WB_PC, XX),
        Cat(ADD_ALU, op2Imm, op1Reg, ImmGen.I, Jump, ST_XX, LD_XX, WB_PC, XX),
        )

    val insts : Seq[UInt]  = Seq(
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LB, rand_rd, Opcode.LOAD),             //LB
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LBU, rand_rd, Opcode.LOAD),            //LBU
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LH, rand_rd, Opcode.LOAD),             //LH
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LHU, rand_rd, Opcode.LOAD),            //LHU
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LW, rand_rd, Opcode.LOAD),             //LW

        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SB, rand_rd, Opcode.STORE),            //SB
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SH, rand_rd, Opcode.STORE),            //SH
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SW, rand_rd, Opcode.STORE),            //SW

        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),           //ADD
        Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),           //SUB
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLL, rand_rd, Opcode.RTYPE),           //SLL
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SRL, rand_rd, Opcode.RTYPE),           //SRL
        Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.SRL, rand_rd, Opcode.RTYPE),           //SRA
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.RTYPE),            //OR
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.XOR, rand_rd, Opcode.RTYPE),           //XOR
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.RTYPE),           //AND
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLT, rand_rd, Opcode.RTYPE),           //SLT
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLTU, rand_rd, Opcode.RTYPE),           //SLTU

        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BEQ, rand_rd, Opcode.BRANCH),          //BEQ
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BNE, rand_rd, Opcode.BRANCH),          //BNE
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BLT, rand_rd, Opcode.BRANCH),          //BLT
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BLTU, rand_rd, Opcode.BRANCH),         //BLTU
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BGE, rand_rd, Opcode.BRANCH),          //BGE
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BGEU, rand_rd, Opcode.BRANCH),         //BGEU

        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.ITYPE),           //ADDI
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SLL, rand_rd, Opcode.ITYPE),           //SLLI
        Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.SRL, rand_rd, Opcode.ITYPE),           //SRL
        Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.SRL, rand_rd, Opcode.ITYPE),           //SRA
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.ITYPE),            //ORI
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.XOR, rand_rd, Opcode.ITYPE),           //XORI
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.ITYPE),           //ANDI
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SLT, rand_rd, Opcode.ITYPE),           //SLTI
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SLTU, rand_rd, Opcode.ITYPE),           //SLTIU

        Cat(rand_fn7, rand_rs2, rand_rs1, rand_fn3, rand_rd, Opcode.LUI),               //LUI
        Cat(rand_fn7, rand_rs2, rand_rs1, rand_fn3, rand_rd, Opcode.AUIPC),             //AUIPC

        Cat(rand_fn7, rand_rs2, rand_rs1, rand_fn3, rand_rd, Opcode.JAL),               //JAL
        Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.JALR),            //JALR
        )

}