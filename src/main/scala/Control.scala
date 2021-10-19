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

    /*PCSrc*/
    val Pl4 = 0.U
    val Br = 1.U

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
//                      |    ALU_op2
    val default = List(ALU.XXX_ALU, op2Imm, ImmGen.X, Pl4, ST_XX, LD_XX)
    val mappings = Array(
            //Loads
            LB -> List(ALU.ADD_ALU, op2Imm,ImmGen.I, Pl4, ST_XX, LD_LB),
            //Stores
            SB -> List(ALU.ADD_ALU, op2Imm, ImmGen.S, Pl4, ST_SB, LD_XX),
            //Arithmetic
            ADD -> List(ALU.ADD_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            SUB -> List(ALU.SUB_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            //Logical
            OR -> List(ALU.OR_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            AND -> List(ALU.AND_ALU, op2Reg, ImmGen.X, Pl4, ST_XX, LD_XX),
            //Branches
            BEQ -> List(ALU.ADD_ALU, op2Reg,ImmGen.B, Br, ST_XX, LD_XX)       //ALU doesnt really matter, zero is always calculated, also extra thing for branch calc

    )
}

class ControlIO extends Bundle {
    val inst = Input(UInt(32.W))
    val immGenCtrl = Output(UInt(3.W))
    val aluCtrl = Output(UInt(4.W))
    val aluInCtrl = Output(Bool())
    val PCSrc = Output(Bool())
    val sttype = Output(UInt(2.W))
    val ldtype = Output(UInt(3.W))
}

class Control extends Module {
    val io = IO(new ControlIO)

    val controls = ListLookup(io.inst, Control.default, Control.mappings) //Set default

    io.aluCtrl      := controls(0)
    io.aluInCtrl    := controls(1)
    io.immGenCtrl   := controls(2)
    io.PCSrc        := controls(3)
    io.sttype       := controls(4)
    io.ldtype       := controls(5)
}