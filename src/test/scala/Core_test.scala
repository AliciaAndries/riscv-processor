package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core)
    
    val (cntr, done) = Counter(true.B, 32)

    when(cntr === 2.U){
      assert(dut.io.same === true.B)
      assert(dut.io.sum === true.B)
    }

    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}