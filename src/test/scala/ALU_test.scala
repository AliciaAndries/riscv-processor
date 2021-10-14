package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import scala.math.BigInt
import ALU._
import Control._

class ALU_tester extends BasicTester {
    val dut = Module(new ALU)
    val ctrl = Module(new Control)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)


    val rnd = new scala.util.Random
    def rand_fn7 = (rnd.nextInt(1 << 7)).U(7.W)
    def rand_rs2 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_rs1 = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)
    def rand_fn3 = (rnd.nextInt(1 << 3)).U(3.W) 
    def rand_rd  = (rnd.nextInt((1 << 5) - 1) + 1).U(5.W)

    val compares = Array(Cat(ADD_ALU, op2Imm,ImmGen.I, Pl4),    //4,1,3,1
                          Cat(ADD_ALU, op2Imm, ImmGen.S, Pl4),
                          Cat(ADD_ALU, op2Reg, ImmGen.X, Pl4),
                          Cat(SUB_ALU, op2Reg, ImmGen.X, Pl4),
                          Cat(OR_ALU, op2Reg, ImmGen.X, Pl4),
                          Cat(AND_ALU, op2Reg, ImmGen.X, Pl4),
                          Cat(ADD_ALU, op2Reg,ImmGen.B, Br))

    val insts : Seq[UInt]  = Seq(Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.LB, rand_rd, Opcode.LOAD),
                                  //BigInt("01111111111111111000111110000011", 2).U, same as above
                                  Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.SB, rand_rd, Opcode.STORE),
                                  Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
                                  Cat(Funct7.S, rand_rs2, rand_rs1, Funct3.ADD, rand_rd, Opcode.RTYPE),
                                  Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.OR, rand_rd, Opcode.RTYPE),
                                  Cat(Funct7.U, rand_rs2, rand_rs1, Funct3.AND, rand_rd, Opcode.RTYPE),
                                  Cat(rand_fn7, rand_rs2, rand_rs1, Funct3.BEQ, rand_rd, Opcode.BRANCH)
                                  )
    
    val (cntr, done) = Counter(true.B, insts.size)
      val rs1  = Seq.fill(insts.size)(rnd.nextInt()) map toBigInt
      val rs2  = Seq.fill(insts.size)(rnd.nextInt()) map toBigInt
      val count = RegInit(0.U(8.W))
      count := count + 1.U
      val line = VecInit(compares)(count)
      val aluc = line(7,5)
      val aluin = line (4)
      val imm = line(3,1)
      val mpc = line(0)

      ctrl.io.inst := VecInit(insts)(cntr)

      

      dut.io.operation := ctrl.io.aluCtrl
      dut.io.op1 := VecInit(rs1 map (_.U))(cntr)
      dut.io.op2 := VecInit(rs2 map (_.U))(cntr)

      val sum = VecInit((rs1 zip rs2) map { case (a, b) => toBigInt(a.toInt + b.toInt).U(32.W) })
      val sub = VecInit((rs1 zip rs2) map { case (a, b) => toBigInt(a.toInt - b.toInt).U(32.W) })
      val and = VecInit((rs1 zip rs2) map { case (a, b) => (a & b).U(32.W) })
      val or = VecInit((rs1 zip rs2) map { case (a, b) => (a | b).U(32.W) })
    
      
      val out = (Mux(aluc === ADD_ALU, sum(cntr),
                  Mux(aluc === SUB_ALU, sub(cntr),
                  Mux(aluc === AND_ALU, and(cntr), or(cntr)))),
                  Mux(sub(cntr).orR, 0.B, 1.B))
      printf("control, aluCtrl: %d ?= %d, aluInCtrl: %d ?= %d, immGenCtrl: %d ?= %d, PCSrc: %d ?= %d\n",
                aluc, ctrl.io.aluCtrl, aluin, ctrl.io.aluInCtrl, imm, ctrl.io.immGenCtrl, mpc, ctrl.io.PCSrc)
      printf("Counter: %d, OP: 0x%x, A: 0x%x, B: 0x%x, OUT: 0x%x ?= 0x%x, ZERO: 0x%x ?= 0x%x\n",
          cntr, dut.io.operation, dut.io.op1, dut.io.op2, dut.io.result, out._1, dut.io.zero, out._2) 

      when(done) { stop(); stop() } 
      assert(ctrl.io.aluCtrl === aluc)
      assert(ctrl.io.aluInCtrl === aluin)
      assert(ctrl.io.immGenCtrl === imm)
      assert(ctrl.io.PCSrc === mpc)
      assert(dut.io.result === out._1)
      assert(dut.io.zero === out._2)
}

class ALUTests extends FlatSpec with Matchers {
  "ALU" should "pass" in {
    assert(TesterDriver execute (() => new ALU_tester))
  }
}