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
      val aluc = line(13,10)
      

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
      
      printf("Counter: %d, OP: 0x%x, A: 0x%x, B: 0x%x, OUT: 0x%x ?= 0x%x, ZERO: 0x%x ?= 0x%x\n",
          cntr, dut.io.operation, dut.io.op1, dut.io.op2, dut.io.result, out._1, dut.io.zero, out._2) 

      when(done) { stop(); stop() } 
      
      assert(ctrl.io.aluCtrl === aluc)
      assert(dut.io.result === out._1)
      assert(dut.io.zero === out._2)
}

class ALUTests extends FlatSpec with Matchers {
  "ALU" should "pass" in {
    assert(TesterDriver execute (() => new ALU_tester))
  }
}