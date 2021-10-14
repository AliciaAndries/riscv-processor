package core

import chisel3._
import chisel3.util._
import scala.collection.immutable
import chisel3.util.experimental.loadMemoryFromFile

class InstructionRegIO extends Bundle {
    val inst_addr = Input(UInt(32.W))
    val inst = Output(UInt(32.W))
}

class InstructionReg(file: String) extends Module {
    val INSTREG_SIZE = 256 //amount of instructions = 64

    val io = IO(new InstructionRegIO) //should it be decoupled?

    val mem = Mem(INSTREG_SIZE, UInt(8.W)) //maybe should use Vector instead as that is the official way to do rom
    loadMemoryFromFile(mem, file) //not sure if this should be used for non simulation, also a it a bit ugly

    io.inst := Cat(mem(io.inst_addr), mem(io.inst_addr+1.U(32.W)), mem(io.inst_addr+2.U(32.W)), mem(io.inst_addr+3.U(32.W)))
}