package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import FPGAInstructions._

class Pipelined_Tester extends BasicTester {
    val dut = Module(new Dataflow)//Module(new Core(new Data))
    val iMem = Module(new IMemoryVec)
    val dMem = Module(new Memory)
    val (cntr, done) = Counter(true.B, 53)

    iMem.io.req.bits.addr := dut.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dut.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dut.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dut.io.iMemIO.req.valid

    dut.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dut.io.iMemIO.resp.valid := iMem.io.resp.valid

    dMem.io.req.bits.addr := dut.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dut.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dut.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dut.io.dMemIO.req.valid

    dut.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dut.io.dMemIO.resp.valid := dMem.io.resp.valid

    printf("cntr = %d, pc = %d, mpc = %d\n", cntr, dut.io.test.pc, dut.io.test.mpc)
    when(done) { stop(); stop() } 
}

class PipelinedTests extends FlatSpec with Matchers {
  "Pipeline" should "pass" in {
    assert(TesterDriver execute (() => new Pipelined_Tester))
  }
}