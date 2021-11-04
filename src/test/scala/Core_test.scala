package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core)
    
    val (cntr, done) = Counter(true.B, 31)

    printf("counter = %d, pc = %d, addi = %d, same = %d, sum = %d\n", cntr, dut.io.pc, dut.io.pc_whole, dut.io.addi, dut.io.same, dut.io.sum)

    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}