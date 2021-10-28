package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import TestInst._

class Control_tester extends BasicTester {
    val dut = Module(new Control)

    val (cntr, done) = Counter(true.B, insts.size)
    
    val line    = VecInit(compares)(cntr)
    val aluc    = line(20,17)
    val aluop2  = line(16)
    val aluop1  = line(15)
    val imm     = line(14,12)
    val mpc     = line(11,10)
    val stt     = line(9,8)
    val ldt     = line(7,5)
    val wbsrc   = line(4,3)
    val bt      = line(2,0)

    dut.io.inst := VecInit(insts)(cntr)

    printf("cntr = %d, aludut: %d =?= %d, aluIndut: %d =?= %d, immGendut: %d =?= %d, PCSrc: %d =?= %d, sttype: %d =?= %d, ldtype: %d =?= %d, wbsrc %d =?= %d\n",
                cntr, aluc, dut.io.aluCtrl, aluop2, dut.io.op2Ctrl, imm, dut.io.immGenCtrl, mpc, dut.io.PCSrc, stt, dut.io.sttype, ldt, dut.io.ldtype, wbsrc, dut.io.wbsrc)

    assert(dut.io.aluCtrl === aluc)
    assert(dut.io.op2Ctrl === aluop2)
    assert(dut.io.op1Ctrl === aluop1)
    assert(dut.io.immGenCtrl === imm)
    assert(dut.io.PCSrc === mpc)
    assert(dut.io.sttype === stt)
    assert(dut.io.ldtype === ldt)
    assert(dut.io.wbsrc === wbsrc)
    assert(dut.io.bt === bt)

    when(done) { stop(); stop() } 
}

class ControlTests extends FlatSpec with Matchers {
  "Control" should "pass" in {
    assert(TesterDriver execute (() => new Control_tester))
  }
}