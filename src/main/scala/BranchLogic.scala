package core

import chisel3._
import chisel3.util._

object Branch {
    val XX = 0.U(3.W)
    val EQ = 1.U(3.W)
    val NE = 2.U(3.W)
    val LT = 3.U(3.W)
    val LTU = 4.U(3.W)
    val GE = 5.U(3.W)
    val GEU = 6.U(3.W)
}
//TODO: maybe move logic in ALU to here
class BranchLogicIO extends Bundle {
    val comp = Input(Bool())
    val bt = Input(UInt(3.W))
    val taken = Output(Bool())
}

class BranchLogic extends Module {
    val io = IO(new BranchLogicIO)

    io.taken := Mux(io.bt === Branch.NE /* || io.bt === Branch.GE || io.bt === Branch.GEU */, !io.comp, Mux(io.bt === Branch.XX, false.B, io.comp))
}