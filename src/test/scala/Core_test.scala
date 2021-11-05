package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core(new IMemoryVec))
    
    val (cntr, done) = Counter(true.B, 53)

    printf("counter = %d, pc = %d, addi = %d, same = %d, sum = %d\n", cntr, dut.io.pc, dut.io.addi, dut.io.same, dut.io.sum)
    assert(dut.io.sum === 1.U)
    assert(dut.io.same === 1.U)
    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}