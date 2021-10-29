package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import TestInst._


/* 
  only 7 instructions types tested  
 */

class ImmGenTester extends BasicTester {
    val dut = Module(new ImmGen)

    val (cntr, done) = Counter(true.B, (insts.size << 1))

    val compare = VecInit(compares)(cntr)

    val inst = VecInit(insts)(cntr)
    dut.io.inst := inst
    val immGenCtrl = compare(14,12)
    dut.io.immGenCtrl := immGenCtrl

    val out = dut.io.out

    val inst31 = inst(31)
    val inst30_25 = inst(30,25)
    val inst24_21 = inst(24,21)
    val inst20 = inst(20)
    val inst19_12 = inst(19,12)
    val inst11_8 = inst(11,8)
    val inst7 = inst(7)
    

    val immgeni = Cat(Cat(Seq.fill(21){inst31}), inst30_25, inst24_21, inst20) 
    val immgens = Cat(Cat(Seq.fill(21){inst31}), inst30_25, inst11_8, inst7)
    val immgenb = Cat(Cat(Seq.fill(20){inst31}), inst7, inst30_25, inst11_8, 0.U)
    val immgenu = Cat(inst31, inst30_25, inst24_21, inst20, inst19_12, Cat(Seq.fill(12)(0.U)))
    val immgenj = Cat(Cat(Seq.fill(12)(inst31)), inst19_12, inst20, inst30_25, inst24_21, 0.U)
    /*
    ...
    */
    val immgenx = 0.U

    val out_check = MuxLookup(immGenCtrl, immgenx, 
      Seq(
        ImmGen.I -> immgeni,
        ImmGen.S -> immgens,
        ImmGen.B -> immgenb,
        ImmGen.U -> immgenu,
        ImmGen.J -> immgenj
      )
    )
    // printf("b31 = %d, b30to20 = %d, b19to12 = %d, b11 = %d, b10to5 = %d, b4to1 = %d, b0 = %d\n"
    //       , dut.io.test.b31, dut.io.test.b30to20, dut.io.test.b19to12, dut.io.test.b11, dut.io.test.b10to5, dut.io.test.b4to1, dut.io.test.b0)
    printf("inst31 = %d, inst30_25 = %d, inst24_21 = %d, inst20 = %d, inst19_12 = %d, inst11_8 = %d, inst7 = %d\n"
          , inst31, inst30_25, inst24_21, inst20, inst19_12, inst11_8, inst7)
    printf("cntr = %d, cntrl = %d, extended = %d, check = %d, 12 bits = %d\n", cntr, immGenCtrl, out, out_check, inst(31,20).asSInt)
    when(immGenCtrl.orR){
      assert(out === out_check)
    }
    when(done) { stop(); stop() } 
}


class ImmGenTests extends FlatSpec with Matchers {
  "ImmGen" should "pass" in {
    assert(TesterDriver execute (() => new ImmGenTester))
  }
}