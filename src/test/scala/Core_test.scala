package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core(new IMemoryVec))
    val prev_led = RegInit(false.B)
    dut.io.uartSerialPort.rx := 0.U
    val (cntr, done) = Counter(true.B, 20)

    printf("ioled = %d, uart = %d\n", dut.io.ledio, dut.io.uartSerialPort.tx)
    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}