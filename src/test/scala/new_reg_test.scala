package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class NewRegFileTester extends BasicTester {
    val dut = Module(new RegFile)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)

    val raddr1s = VecInit(1.U, 2.U, 3.U, 4.U, 5.U)
    val raddr2s = VecInit(1.U, 1.U, 1.U, 1.U, 1.U)
    val waddr = VecInit(1.U, 2.U, 3.U, 4.U, 5.U)
    val wtotal = 32


    val (cntr, done) = Counter(true.B, 6)

    dut.io.raddr1 := raddr1s(cntr)
    dut.io.raddr2 := raddr2s(cntr)
    dut.io.waddr := raddr1s(cntr)
    dut.io.wdata := 10.U + cntr
    dut.io.wen := true.B
    

    printf("count = %d, wdata = %d, waddr = %d, rd1 = %d, rd2 = %d\n", cntr, 10.U + cntr, raddr1s(cntr), dut.io.rs1, dut.io.rs2)

    when(done) { stop(); stop() } 
}

class NewRegFileTests extends FlatSpec with Matchers {
  "RegFile" should "pass" in {
    assert(TesterDriver execute (() => new NewRegFileTester))
  }
}