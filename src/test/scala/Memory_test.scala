package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class MemoryTester extends BasicTester {
    val dut = Module(new Memory)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)

    val mem_depth_bits = log2Ceil(256*4)

    val rnd = new scala.util.Random
    def rand_addr = Seq.fill(7)(rnd.nextInt(1 << mem_depth_bits).U(mem_depth_bits.W))
    
    

    val offsets = VecInit(0.U, 2.U, 0.U, 0.U, 1.U, 2.U, 3.U) //masks "1111", "1100", "0011", "0001", "0010", "0100", "1000"
    val write_size = VecInit("b1111".U, "b11".U, "b11".U, "b1".U, "b1".U, "b1".U, "b1".U)
    val wdatas = Seq.fill(7)((toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W))


    val (cntr, done) = Counter(true.B, wdatas.size * 3)

    val wdata = VecInit(wdatas)(cntr/3.U)
    dut.io.req.bits.data := wdata

    dut.io.req.valid := true.B

    val mask = Mux(cntr%3.U === 0.U, (write_size(cntr/3.U) << offsets(cntr/3.U)), 0.U)//Mux(cntr(0), 0.U, )
    dut.io.req.bits.mask := mask
    
    val waddr = 9.U
    val addr_allign = (VecInit(rand_addr)(cntr/3.U) >> 2.U << 2.U)
    val addr = addr_allign + offsets(cntr/3.U)
    dut.io.req.bits.addr := addr

    val valid = dut.io.resp.valid
    val response = dut.io.resp.bits.data
    val rdata = Mux(valid, response, 0.U(32.W)) >> (offsets(cntr/3.U) << 3.U)
    
    

    val rdata_comp = MuxLookup(write_size(cntr/3.U), rdata,
        Array(
            "b11".U -> (rdata(15,0)),
            "b1".U -> (rdata(7,0))
        ))
    
    val wdata_shift = wdata >> ((offsets(cntr/3.U) << 3))
    val wdata_comp = MuxLookup(write_size(cntr/3.U), wdata_shift,
        Array(
            "b11".U -> (wdata_shift(15,0)),
            "b1".U -> (wdata_shift(7,0))
        ))

    
    printf("counter = %d, addr = %d, wdata = %d, wdata_relevant = %d, rdata = %d, rdata_relevant = %d, valid = %d, mask = %d \n\n",
             cntr, addr/* (addr_allign + offsets(cntr >> 1)) */, wdata, wdata_comp, rdata, rdata_comp, valid, mask)
    when(cntr%3.U === 2.U){
        assert(rdata_comp === wdata_comp)
    }
/*     when(cntr%3.U === 2.U){
        assert(rdata === VecInit(wdatas)(cntr/3.U))
    } */
    when(done) { stop(); stop() } 
}

class MemoryTests extends FlatSpec with Matchers {
  "Memory" should "pass" in {
    assert(TesterDriver execute (() => new MemoryTester))
  }
}