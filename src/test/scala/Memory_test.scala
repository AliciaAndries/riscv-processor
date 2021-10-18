package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class MemoryTester extends BasicTester {
    val dut = Module(new Memory)

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)

    val mem_depth_bits = log2Ceil(256)

    val rnd = new scala.util.Random
    def rand_addr = rnd.nextInt(1 << mem_depth_bits).U(mem_depth_bits.W)
    val addr_allign = (rand_addr >> 2.U << 2.U)     //for some reason this  doesnt give what you would expect
    val shifted = (rand_addr >> 2.U)

    val offsets = VecInit(0.U, 2.U, 0.U, 0.U, 1.U, 2.U, 3.U) //masks "1111", "1100", "0011", "0001", "0010", "0100", "1000"
    val write_size = VecInit("b1111".U, "b11".U, "b11".U, "b1".U, "b1".U, "b1".U, "b1".U)
    val wdatas = Seq.fill(7)((toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W))    //maybe test with bytes so you can actually see whats going on

    val (cntr, done) = Counter(true.B, wdatas.size << 1)

    val wdata = VecInit(wdatas)(cntr >> 1)
    dut.io.req.bits.data := wdata
    dut.io.req.valid := true.B
    val mask = Mux(cntr(0), 0.U, (write_size(cntr >> 1) << offsets(cntr >> 1)))
    printf("mask = %d\n", mask)
    dut.io.req.bits.mask := mask
    val addr = addr_allign + offsets(cntr >> 1)
    dut.io.req.bits.addr := addr

    val valid = dut.io.resp.valid
    assert(valid === 1.B)
    val rdata = Mux(valid, dut.io.resp.bits.data, 0.U(32.W)) >> (offsets(cntr >> 1) << 3)
    
    val response = dut.io.resp.bits.data
    //assert(response =/= 0.U)
    val rdata_comp = MuxLookup(write_size(cntr >> 1), rdata,
        Array(
            "b11".U -> (rdata(15,0)),
            "b1".U -> (rdata(7,0))
        ))
    
    val wdata_shift = wdata >> ((offsets(cntr >> 1) << 3))
    val wdata_comp = MuxLookup(write_size(cntr >> 1), wdata_shift,
        Array(
            "b11".U -> (wdata_shift(15,0)),
            "b1".U -> (wdata_shift(7,0))
        ))

    when(done) { stop(); stop() } 
    printf("counter = %d, addr = %d, wdata = %d, rdata = %d, valid = %d \n", cntr, (addr_allign + offsets(cntr >> 1)), wdata_comp, rdata_comp, valid)
    when(!mask.orR){
        
        assert(rdata_comp === wdata_comp)
    }
}

class MemoryTests extends FlatSpec with Matchers {
  "Memory" should "pass" in {
    assert(TesterDriver execute (() => new MemoryTester))
  }
}