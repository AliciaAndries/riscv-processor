package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import scala.math.BigInt
import ALU._
import Control._
import TestInst._

class ALU_tester extends BasicTester {
    val dut = Module(new ALU)
    val ctrl = Module(new Control)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)
    
    val (cntr, done) = Counter(true.B, insts.size)
      val rs1  = Seq.fill(insts.size)(rnd.nextInt()) map toBigInt
      val rs2  = Seq.fill(insts.size)(rnd.nextInt()) map toBigInt

      val line = VecInit(compares)(cntr)
      val aluc = line(21,18)
      

      ctrl.io.inst := VecInit(insts)(cntr)

      dut.io.operation := ctrl.io.aluCtrl
      dut.io.op1 := VecInit(rs1 map (_.U))(cntr)
      dut.io.op2 := VecInit(rs2 map (_.U))(cntr)

      val sum =   VecInit((rs1 zip rs2) map { case (a, b) => toBigInt(a.toInt + b.toInt).U(32.W) })
      val sub =   VecInit((rs1 zip rs2) map { case (a, b) => toBigInt(a.toInt - b.toInt).U(32.W) })
      val and =   VecInit((rs1 zip rs2) map { case (a, b) => (a & b).U(32.W) })
      val or =    VecInit((rs1 zip rs2) map { case (a, b) => (a|b).U(32.W) })
      val xor =   VecInit((rs1 zip rs2) map { case (a,b) => (a^b).U(32.W) })
      val slt =   VecInit((rs1 zip rs2) map { case (a,b) => Mux((a.toInt < b.toInt).asBool, 1.U, 0.U) })
      val sltu =  VecInit((rs1 zip rs2) map { case (a,b) => Mux(a.U(32.W) < b.U(32.W), 1.U, 0.U) })
      val sgt =   VecInit((rs1 zip rs2) map { case (a,b) => Mux((a.toInt >= b.toInt).asBool, 1.U, 0.U) })
      val sgtu =  VecInit((rs1 zip rs2) map { case (a,b) => Mux(a.U(32.W) >= b.U(32.W), 1.U, 0.U) })
      val sll =   VecInit((rs1 zip rs2) map { case (a, b) => (a.U(32.W) << (b.U(32.W))(4,0))(31,0) })
      val srl =   VecInit((rs1 zip rs2) map { case (a, b) => (a.U(32.W) >> (b.U(32.W))(4,0)) })
      val sra =   VecInit((rs1 zip rs2) map { case (a, b) => toBigInt(a.toInt >> (b.toInt&0x1f)).U(32.W)})

      val out = (MuxLookup(aluc, dut.io.op2,
                    Seq(
                      ADD_ALU -> sum(cntr),
                      SUB_ALU -> sub(cntr),
                      AND_ALU -> and(cntr), 
                      OR_ALU -> or(cntr),
                      XOR_ALU -> xor(cntr),
                      SLT_ALU -> slt(cntr),
                      SLTU_ALU -> sltu(cntr),
                      SLL_ALU -> sll(cntr),
                      SRL_ALU -> srl(cntr),
                      SRA_ALU -> sra(cntr)
                  )),
                  MuxLookup(aluc, !sub(cntr).orR, 
                    Seq(
                      SLT_ALU -> slt(cntr)(0),
                      SLTU_ALU -> sltu(cntr)(0),
                      SGT_ALU -> sgt(cntr)(0),
                      SGTU_ALU -> sgtu(cntr)(0)
                    )))
      
      printf("Counter: %d, OP: %d =?= %d, A: %d, B: %d, OUT: %d =?= %d, ZERO: %d =?= %d\n",
          cntr, dut.io.operation, aluc, dut.io.op1, dut.io.op2, dut.io.result, out._1, dut.io.comp, out._2) 
      assert(ctrl.io.aluCtrl === aluc)
      assert(dut.io.result === out._1)
      assert(dut.io.comp === out._2)

      when(done) { stop(); stop() } 
      
      
}

class ALUTests extends FlatSpec with Matchers {
  "ALU" should "pass" in {
    assert(TesterDriver execute (() => new ALU_tester))
  }
}