package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class RegFileTester extends BasicTester {
    val dut = Module(new RegFile)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)

    val wtotal = 32

    val rnd = new scala.util.Random
    val wdatas = Seq.fill(wtotal)((toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W))
    val (cntr, done) = Counter(true.B, wtotal*2)

    val raddr1 = WireDefault(0.U(5.W))
    val raddr2 = WireDefault(0.U(5.W))
    when(cntr >= wtotal.U){
        raddr1 := wtotal.U -1.U - (cntr - wtotal.U)
        raddr2 := cntr - wtotal.U
    }
    dut.io.raddr1 := raddr1
    dut.io.raddr2 := raddr2

    val waddr = WireDefault(0.U(5.W))
    val wdata = WireDefault(5.U(32.W))
    val wen = WireDefault(true.B)
    when(cntr < wtotal.U){
        waddr := cntr
        wdata := VecInit(wdatas)(cntr)
        wen := waddr.orR
    }

    dut.io.waddr := waddr
    dut.io.wdata := wdata
    dut.io.wen := wen

    val rs1 = dut.io.rs1
    val rs2 = dut.io.rs2

    printf("count = %d, wdata = %d, waddr = %d, rdata1 = %d, raddr1 = %d, rd1 cntr = %d, rdata2 = %d, raddr2 = %d, rd2 cntr = %d\n", 
                    cntr, wdata, waddr, rs1, raddr1, 27.U - (cntr - wtotal.U), rs2, raddr2, cntr-wtotal.U)
    when(cntr >=wtotal.U){
        when(!(wtotal.U -1.U - (cntr - wtotal.U))){
            assert(rs1 === 0.U)
            assert(rs2 === VecInit(wdatas)(cntr-wtotal.U))
        }.elsewhen(!(cntr - wtotal.U).orR){
            assert(rs1 === VecInit(wdatas)(wtotal.U -1.U-(cntr-wtotal.U)))
            assert(rs2 === 0.U)
        }.otherwise{
            assert(rs1 === VecInit(wdatas)(wtotal.U -1.U-(cntr-wtotal.U)))
            assert(rs2 === VecInit(wdatas)(cntr-wtotal.U))
        }
    }.otherwise{
        assert(rs1 === 0.U && rs2 === 0.U)
    }

    when(done) { stop(); stop() } 
}

class RegFileTests extends FlatSpec with Matchers {
  "RegFile" should "pass" in {
    assert(TesterDriver execute (() => new RegFileTester))
  }
}