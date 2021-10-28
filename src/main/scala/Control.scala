package core

import chisel3._
import chisel3.util._
import Instructions._
/*p130 has all upcode mappings*/


//only immgen and alu control for now

object Control {
    //ALU and immgen values here or there 
    //TODO: what to do with XImmGen?

    /*ALU_op2*/
    val op2Imm = 0.U
    val op2Reg = 1.U

    /*ALU_op1*/
    val op1Reg = 0.U
    val op1PC = 1.U

    /*PCSrc*/
    val Pl4 = 0.U
    val Br = 1.U
    val Jump = 2.U
    val Pl0 = 3.U

    /*write back*/
    val WB_F = 0.U(2.W)
    val WB_ALU = 1.U(2.W)
    val WB_MEM = 2.U(2.W)
    val WB_PC = 3.U(2.W)

    /*store type*/
    val ST_XX = 0.U(2.W)
    val ST_SW = 1.U(2.W)
    val ST_SH = 2.U(2.W)
    val ST_SB = 3.U(2.W)

    /*load type*/
    val LD_XX = 0.U(3.W)
    val LD_LW = 1.U(3.W)
    val LD_LH = 2.U(3.W)
    val LD_LHU = 3.U(3.W)
    val LD_LB = 4.U(3.W)
    val LD_LBU = 5.U(3.W)
//                     ALU op
//                      |       ALU_op2
    val default = List(ALU.XXX_ALU, op2Imm, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_F, Branch.XX)
    val mappings = Array(
            //Loads
            LB -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LB, WB_MEM, Branch.XX),
            LBU -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LBU, WB_MEM, Branch.XX),
            LH -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LH, WB_MEM, Branch.XX),
            LHU -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LHU, WB_MEM, Branch.XX),
            LW -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl0, ST_XX, LD_LW, WB_MEM, Branch.XX),
            //Stores
            SB -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SB, LD_XX, WB_F, Branch.XX),
            SH -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SH, LD_XX, WB_F, Branch.XX),
            SW -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.S, Pl4, ST_SW, LD_XX, WB_F, Branch.XX),
            //Arithmetic
            ADD -> List(ALU.ADD_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SUB -> List(ALU.SUB_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLL -> List(ALU.SLL_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SRL -> List(ALU.SRL_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SRA -> List(ALU.SRA_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            //Logical
            AND -> List(ALU.AND_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            OR -> List(ALU.OR_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            XOR -> List(ALU.XOR_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLT -> List(ALU.SLT_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLTU -> List(ALU.SLTU_ALU, op2Reg, op1Reg, ImmGen.X, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            //Interger register-immediate
            ADDI -> List(ALU.ADD_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLLI -> List(ALU.SLL_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SRLI -> List(ALU.SRL_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SRAI -> List(ALU.SRA_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            //
            ANDI -> List(ALU.AND_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            ORI -> List(ALU.OR_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            XORI -> List(ALU.XOR_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLTI -> List(ALU.SLT_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            SLTIU -> List(ALU.SLTU_ALU, op2Imm, op1Reg, ImmGen.I, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            //UTYPE
            LUI -> List(ALU.XXX_ALU, op2Imm, op1Reg, ImmGen.U, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),    //ALU_res is written to wb so XXX_ALU makes op2 pass through and op2 == immGen
            AUIPC -> List(ALU.ADD_ALU, op2Imm, op1PC, ImmGen.U, Pl4, ST_XX, LD_XX, WB_ALU, Branch.XX),
            //Branches
            BEQ -> List(ALU.XXX_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.EQ),      //ALU doesnt really matter, zero is always calculated, also extra thing for branch calc
            BNE -> List(ALU.XXX_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.NE),
            BLT -> List(ALU.SLT_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.LT),
            BLTU -> List(ALU.SLTU_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.LTU),
            BGE -> List(ALU.SGT_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.GE),
            BGEU -> List(ALU.SGTU_ALU, op2Reg, op1Reg, ImmGen.B, Br, ST_XX, LD_XX, WB_F, Branch.GEU),
            //Unconditional jump
            JAL -> List(ALU.ADD_ALU, op2Imm, op1PC, ImmGen.J, Jump, ST_XX, LD_XX, WB_PC, Branch.XX),
            JAL -> List(ALU.ADD_ALU, op2Imm, op1PC, ImmGen.I, Jump, ST_XX, LD_XX, WB_PC, Branch.XX)
            
    )
}

class ControlIO extends Bundle {
    val inst = Input(UInt(32.W))
    val immGenCtrl = Output(UInt(3.W))
    val aluCtrl = Output(UInt(4.W))
    val op2Ctrl = Output(Bool())
    val op1Ctrl = Output(Bool())
    val PCSrc = Output(UInt(3.W))
    val sttype = Output(UInt(2.W))
    val ldtype = Output(UInt(3.W))
    val wbsrc = Output(UInt(2.W))
    val bt = Output(UInt(3.W))
}

class Control extends Module {
    val io = IO(new ControlIO)

    val controls = ListLookup(io.inst, Control.default, Control.mappings) //Set default

    io.aluCtrl      := controls(0)
    io.op2Ctrl      := controls(1)
    io.op1Ctrl      := controls(2)
    io.immGenCtrl   := controls(3)
    io.PCSrc        := controls(4)
    io.sttype       := controls(5)
    io.ldtype       := controls(6)
    io.wbsrc        := controls(7)
    io.bt           := controls(8)
}