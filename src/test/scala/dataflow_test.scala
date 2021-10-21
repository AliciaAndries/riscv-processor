package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import Opcode._

class Dataflow_tester extends BasicTester{
    val dut = Module(new Dataflow)

    val rnd = new scala.util.Random
    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)
    val nr_insts = 13

    val imm = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    //val imm = VecInit(imm_seq)
    val rs1 = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))
    val reg_idx = VecInit(Seq.fill(nr_insts)((rnd.nextInt((1<<5)-1)+1).U(5.W)))
    
    
    val st_offset = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    val base = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))

    val insts = Seq(
        Cat(imm(0.U), rs1(0.U), Funct3.LB, 0.U(5.W), Opcode.LOAD),                                              //give time to mem to init
        Cat(imm(1.U), rs1(1.U), Funct3.LB, reg_idx(1.U), Opcode.LOAD),                                          //write data to register reg_idx(0.U)
        Cat(imm(2.U), rs1(2.U), Funct3.LB, reg_idx(2.U), Opcode.LOAD),                                          //write data to register reg_idx(1.U)
        Cat(imm(3.U), rs1(3.U), Funct3.LB, 0.U(5.W), Opcode.LOAD),                                              //wait cycle aka load into 0.U

        Cat(Funct7.U, reg_idx(1.U), reg_idx(2.U), Funct3.ADD, reg_idx(4.U), Opcode.RTYPE),                      //reg_idx(0.U) + reg_idx(1.U) to reg_idx(2.U)
        Cat(st_offset(5.U)(11,5), reg_idx(4.U), base(5.U), Funct3.SB, st_offset(5.U)(4,0), Opcode.STORE),       //read reg_idx(2.U)
        Cat(imm(6.U), rs1(6.U), Funct3.LB, 0.U(5.W), Opcode.LOAD),                                              //wait cycle

        Cat(imm(7.U), reg_idx(4.U), Funct3.ADD, reg_idx(7.U), Opcode.ITYPE),                                    //addi reg_idx(2.U) + imm
        Cat(st_offset(8.U)(11,5), reg_idx(7.U), base(8.U), Funct3.SB, st_offset(8.U)(4,0), Opcode.STORE),       //read the result of addi
        //Cat(imm(9.U), rs1(9.U), Funct3.LB, 0.U(5.W), Opcode.LOAD),

        Cat(imm(9.U)(11), imm(9.U)(9,4), reg_idx(7.U), reg_idx(4.U), Funct3.ADD, imm(9.U)(3,0), imm(9.U)(10), Opcode.BRANCH),       //branch not taken
                                            //reg_idx(7.0) === reg_idx(4.U) -> problem
        Cat(imm(7.U), reg_idx(4.U), Funct3.ADD, reg_idx(10.U), Opcode.ITYPE),
        Cat(imm(11.U)(11), imm(11.U)(9,4), reg_idx(7.U), reg_idx(10.U), Funct3.ADD, imm(11.U)(3,0), imm(11.U)(10), Opcode.BRANCH),  //branch taken
        Cat(imm(12.U), rs1(12.U), Funct3.LB, 0.U(5.W), Opcode.LOAD)                                                                   //check if branch is taken
    )
    assert(reg_idx(1.U) =/= reg_idx(2.U))

    //init memory    
    val mem = RegInit(VecInit(Seq.fill(256)(23.U(32.W))))
    val addr1 = Cat(Cat(Seq.fill(20)(imm(1.U)(11))), imm(1.U)) + 0.U
    val data1 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    mem(addr1 << 2.U) := data1
    val addr2 = Cat(Cat(Seq.fill(20)(imm(2.U)(11))), imm(2.U)) + 0.U
    val data2 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    mem(addr2 << 2.U) := data2
    //printf("memdata1 = %d, data_addr1 = %d, memdata2 = %d, data_addr2 = %d\n", mem(addr1 << 2.U), addr1, mem(addr2 << 2.U), addr2)
    
    assert(addr1 =/= addr2)

    val (cntr, done) = Counter(true.B, nr_insts)

    dut.io.iMemIO.resp.bits.data := VecInit(insts)(cntr)
    dut.io.iMemIO.resp.valid := true.B

    val inst_addr = dut.io.iMemIO.req.bits.addr

    val data_addr = dut.io.dMemIO.req.bits.addr
    val data = dut.io.dMemIO.req.bits.data
    val mask = dut.io.dMemIO.req.bits.mask
    val valid = dut.io.dMemIO.req.valid

    val loaddata = mem(data_addr << 2.U)
    dut.io.dMemIO.resp.bits.data := loaddata
    val loadvalid = Mux(VecInit(insts)(cntr)(6,0) === Opcode.LOAD, true.B, false.B)
    dut.io.dMemIO.resp.valid := loadvalid
    val data_to_reg = Mux(loadvalid, (loaddata >> (data_addr(1,0) << 3))(7,0), 0.U)

    assert(reg_idx(cntr) =/= 0.U)
    printf("count = %d, inst = %d, valid_d_out = %d, loaddata = %d, data_to_reg = %d, data_addr(1,0) = %d, wb_data = %d, alu_op1 = %d, alu_op2 = %d, aluresult = %d, raddr1 = %d, raddr2 = %d, rs1 = %d rs2 = %d, rd = %d\n\n",
            cntr, VecInit(insts)(cntr), loadvalid, loaddata, data_to_reg, data_addr(1,0), dut.io.test.wb_data, dut.io.test.op1, dut.io.test.op2, dut.io.test.aluresult, dut.io.test.raddr1, dut.io.test.raddr2, dut.io.test.rs1, dut.io.test.rs2, dut.io.test.rwdata)
    when(cntr === 1.U){
        assert(data_addr === addr1)
    }
    when(cntr === 2.U){
        assert(data_addr === addr2)
    }
    when(cntr < 2.U && cntr.orR){
        assert(data_addr === Cat(Cat(Seq.fill(20)(imm(cntr)(11))), imm(cntr)) + 0.U)
    }
    val actual_sum = RegInit(0.U(32.W))

    //test add
    when(cntr === 5.U){
        val mem1 = (mem(addr1 << 2.U) >> (addr1(1,0) << 3))(7,0)
        val sext_mem1 = Cat(Cat(Seq.fill(24)(mem1(7))), mem1)
        val mem2 = (mem(addr2 << 2.U) >> (addr2(1,0) << 3))(7,0)
        val sext_mem2 = Cat(Cat(Seq.fill(24)(mem2(7))), mem2)
        actual_sum := sext_mem1 + sext_mem2
        val sum = ((sext_mem1 + sext_mem2) << (data_addr(1,0) << 3))(31,0)
        printf("sum = %d, check sum = %d\n", data, sum)
        assert(data === sum)
    }
    val prev_instr = RegInit(0.U(32.W))
    //test addi
    when(cntr === 8.U){
        //set prev instr for branch testing
        prev_instr := inst_addr

        val isum = actual_sum + Cat(Cat(Seq.fill(20)(imm(cntr-1.U)(11))), imm(cntr-1.U))
        val isum_rd = (isum << (data_addr(1,0) << 3))(31,0)
        printf("isum = %d, check isum = %d, isum_unshifted = %d, actual sum = %d, imm: %d\n", data, isum_rd, isum, actual_sum, imm(cntr-1.U))
        assert(data === isum_rd)
    }

    //test branching
    //not taken
    when(cntr === 9.U){
        printf("taken = %d\n", dut.io.test.aluzero)
        prev_instr := inst_addr
    }
    //taken
    when(cntr === 10.U){
        assert(prev_instr + 4.U === inst_addr)
    }
    val extended_taken = RegInit(0.U(32.W))
    when(cntr === 11.U){
        printf("taken = %d\n", dut.io.test.aluzero)
        prev_instr := inst_addr
        extended_taken := dut.io.test.extended
    }
    when(cntr === 12.U){
        val extended = Cat(Cat(Seq.fill(20)(imm(11.U)(11))), imm(11.U)(10), imm(11.U)(9,4), imm(11.U)(3,0), 0.U(1.W))
        val inst_addr_check = prev_instr + extended
        printf("extended = %d, extended_check = %d, ", extended_taken, extended)
        printf("inst_addr = %d, inst_addr_check = %d\n", inst_addr, inst_addr_check)
        assert(inst_addr_check === inst_addr)
        assert(prev_instr + 4.U =/= inst_addr)
    }
    when(done) { stop(); stop() } 
}

class DataflowTests extends FlatSpec with Matchers {
  "Dataflow" should "pass" in {
    assert(TesterDriver execute (() => new Dataflow_tester))
  }
}