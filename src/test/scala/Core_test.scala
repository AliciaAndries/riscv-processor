package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core(new IMemoryVec))
    val prev_led = RegInit(false.B)
    val (cntr, done) = Counter(true.B, 53)

    printf("ioled = %d\n", dut.io.ledio)
    assert(dut.io.ledio === !prev_led)
    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}