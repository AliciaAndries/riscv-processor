package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import TestInst._

class Control_tester extends BasicTester {
    val dut = Module(new Control)

    val (cntr, done) = Counter(true.B, insts.size)
    
    val line = VecInit(compares)(cntr)
    val aluc    = line(14,11)
    val aluin   = line(10)
    val imm     = line(9,7)
    val mpc     = line(6)
    val stt     = line(5,4)
    val ldt     = line(3,1)
    val wben    = line(0)

    dut.io.inst := VecInit(insts)(cntr)

    printf("cntr = %d, aludut: %d =?= %d, aluIndut: %d =?= %d, immGendut: %d =?= %d, PCSrc: %d =?= %d, sttype: %d =?= %d, ldtype: %d =?= %d, wben %d =?= %d\n",
                cntr, aluc, dut.io.aluCtrl, aluin, dut.io.aluInCtrl, imm, dut.io.immGenCtrl, mpc, dut.io.PCSrc, stt, dut.io.sttype, ldt, dut.io.ldtype, wben, dut.io.wben)

    assert(dut.io.aluCtrl === aluc)
    assert(dut.io.aluInCtrl === aluin)
    assert(dut.io.immGenCtrl === imm)
    assert(dut.io.PCSrc === mpc)
    assert(dut.io.sttype === stt)
    assert(dut.io.ldtype === ldt)
    assert(dut.io.wben === wben)

    when(done) { stop(); stop() } 
}

class ControlTests extends FlatSpec with Matchers {
  "Control" should "pass" in {
    assert(TesterDriver execute (() => new Control_tester))
  }
}