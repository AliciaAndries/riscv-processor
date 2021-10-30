package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import Opcode._

class Dataflow_tester extends BasicTester{
    val dut = Module(new Dataflow(true))

    val NOP = Cat(0.U(12.W), 0.U(5.W), Funct3.ADD, 0.U(5.W), Opcode.ITYPE)
    val rnd = new scala.util.Random
    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)
    val nr_insts = 13

    val imm = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    //val imm = VecInit(imm_seq)
    val rs1 = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))
    val reg_idx = VecInit(Seq.fill(nr_insts)((rnd.nextInt((1<<5)-1)+1).U(5.W)))
    
    
    val st_offset = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    val base = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))

    val func3_ran = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<3).U(3.W)))

    val insts = Seq(
        NOP,                                                                                                    //give time to mem to init
        Cat(imm(1.U), 0.U(5.W), Funct3.LB, reg_idx(1.U), Opcode.LOAD),                                          //write data to register reg_idx(0.U)
        Cat(imm(2.U), 0.U(5.W), Funct3.LB, reg_idx(2.U), Opcode.LOAD),                                          //write data to register reg_idx(1.U)
        NOP,                                                                                                    //give time to mem to init

        Cat(Funct7.U, reg_idx(1.U), reg_idx(2.U), Funct3.ADD, reg_idx(4.U), Opcode.RTYPE),                      //reg_idx(0.U) + reg_idx(1.U) to reg_idx(2.U)
        Cat(st_offset(5.U)(11,5), reg_idx(4.U), base(5.U), Funct3.SB, st_offset(5.U)(4,0), Opcode.STORE),       //read reg_idx(2.U)

        Cat(imm(6.U), reg_idx(4.U), Funct3.ADD, reg_idx(6.U), Opcode.ITYPE),                                    //addi reg_idx(2.U) + imm
        Cat(st_offset(7.U)(11,5), reg_idx(6.U), base(7.U), Funct3.SB, st_offset(7.U)(4,0), Opcode.STORE),       //read the result of addi

        Cat(imm(9.U)(11), imm(9.U)(9,4), reg_idx(6.U), reg_idx(4.U), Funct3.ADD, imm(9.U)(3,0), imm(9.U)(10), Opcode.BRANCH),       //branch not taken
                                            //reg_idx(7.0) === reg_idx(4.U) -> problem
        Cat(imm(6.U), reg_idx(4.U), Funct3.ADD, reg_idx(7.U), Opcode.ITYPE),
        Cat(imm(10.U)(11), imm(10.U)(9,4), reg_idx(6.U), reg_idx(7.U), Funct3.ADD, imm(10.U)(3,0), imm(10.U)(10), Opcode.BRANCH),  //branch taken

        //TODO: is this actually correct
        Cat(imm(11.U), reg_idx(9.U), Funct3.LB, reg_idx(11.U), Opcode.LOAD),                                    //check if branch is taken + load into 12.U
        Cat(st_offset(12.U)(11,5), reg_idx(6.U), base(12.U), Funct3.SB, st_offset(12.U)(4,0), Opcode.STORE),    //read the result of load

        //LUI
        Cat(imm(13.U), base(13.U), func3_ran(13.U), reg_idx(13.U), Opcode.LUI),               //add 
        Cat(st_offset(14.U)(11,5), reg_idx(13.U), base(14.U), Funct3.SB, st_offset(14.U)(4,0), Opcode.STORE),       //read reg_idx(13.U)
        //AUIPC
        Cat(imm(15.U), base(15.U), func3_ran(15.U), reg_idx(15.U), Opcode.AUIPC),
        Cat(st_offset(16.U)(11,5), reg_idx(15.U), base(16.U), Funct3.SB, st_offset(16.U)(4,0), Opcode.STORE),       //read reg_idx(15.U)
        //JAL
        Cat(imm(17.U), base(17.U), func3_ran(17.U), reg_idx(17.U), Opcode.JAL),               //jump to pc + im
        Cat(st_offset(18.U)(11,5), reg_idx(17.U), base(18.U), Funct3.SB, st_offset(18.U)(4,0), Opcode.STORE),       //read pc + 4
        //JALR
        Cat(imm(19.U), reg_idx(1.U), Funct3.ADD, reg_idx(19.U), Opcode.JALR),                                       //jump to pc + reg(1.U)
        Cat(st_offset(20.U)(11,5), reg_idx(19.U), base(20.U), Funct3.SB, st_offset(20.U)(4,0), Opcode.STORE),       //read pc + 4

        //LDs&&STs
        Cat(imm(21.U), reg_idx(2.U), Funct3.LBU, reg_idx(21.U), Opcode.LOAD), 
        Cat(imm(22.U), reg_idx(2.U), Funct3.LH, reg_idx(22.U), Opcode.LOAD), 
        Cat(imm(23.U), reg_idx(2.U), Funct3.LHU, reg_idx(23.U), Opcode.LOAD),
        Cat(imm(24.U), reg_idx(2.U), Funct3.LW, reg_idx(24.U), Opcode.LOAD),  
        
        Cat(st_offset(25.U)(11,5), reg_idx(21.U), base(25.U), Funct3.SB, st_offset(25.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(26.U)(11,5), reg_idx(22.U), base(26.U), Funct3.SH, st_offset(26.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(27.U)(11,5), reg_idx(23.U), base(27.U), Funct3.SW, st_offset(27.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(28.U)(11,5), reg_idx(24.U), base(28.U), Funct3.SW, st_offset(28.U)(4,0), Opcode.STORE),       //read
    )
    assert(reg_idx(1.U) =/= reg_idx(2.U))
    assert(reg_idx(6.U) =/= reg_idx(9.U))
    assert(reg_idx(6.U) =/= reg_idx(4.U))

    //init memory    
    val mem = RegInit(VecInit(Seq.fill(256)(23.U(32.W))))
    val addr1 = Cat(Cat(Seq.fill(20)(imm(1.U)(11))), imm(1.U)) + 0.U
    val data1 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    mem(addr1 << 2.U) := data1
    val addr2 = Cat(Cat(Seq.fill(20)(imm(2.U)(11))), imm(2.U)) + 0.U
    val data2 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    mem(addr2 << 2.U) := data2
    
    assert(addr1 =/= addr2)

    val pc = dut.io.iMemIO.req.bits.addr
    
    val (cntr, done) = Counter(true.B, nr_insts)

    val inst = VecInit(insts)(cntr)
    dut.io.iMemIO.resp.bits.data := inst
    dut.io.iMemIO.resp.valid := true.B

    

    val data_addr = dut.io.dMemIO.req.bits.addr
    val data = dut.io.dMemIO.req.bits.data
    val mask = dut.io.dMemIO.req.bits.mask
    val valid = dut.io.dMemIO.req.valid

    val loaddata = mem(data_addr << 2.U)
    dut.io.dMemIO.resp.bits.data := loaddata
    val loadvalid = inst(6,0) === Opcode.LOAD
    dut.io.dMemIO.resp.valid := loadvalid
    val data_to_reg = Mux(loadvalid, (loaddata >> (data_addr(1,0) << 3))(7,0), 0.U)


    //check that reg_indx isnt 0.U, should not be possible
    assert(reg_idx(pc>>2.U) =/= 0.U)

    //print all info that could be handy for debugging
    printf("count = %d, PC = %d, valid_d_out = %d, data_to_reg = %d, data_addr(1,0) = %d, wb_data = %d, alu_op1 = %d, alu_op2 = %d, aluresult = %d, raddr1 = %d, raddr2 = %d, rs2 = %d, rd = %d",
            cntr, pc, loadvalid, data_to_reg, data_addr(1,0), dut.io.test.wb_data, dut.io.test.op1, dut.io.test.op2, dut.io.test.aluresult, dut.io.test.raddr1, dut.io.test.raddr2, dut.io.test.rs2, dut.io.test.rwdata)
    when(inst(6,0)===Opcode.LOAD){
        printf(", load\n\n")
    }.otherwise{
        printf("\n\n")
    }
    //check if this is the case, shouldnt happen anymore cause you rs1 is set to 0
    when(pc>>2.U === 1.U){
        assert(data_addr === addr1)
    }
    when(pc>>2.U === 2.U){
        assert(data_addr === addr2)
    }

    //Test the generated data_addr for the loads compared to what they should be
    when(pc>>2.U < 2.U && pc.orR){
        assert(data_addr === Cat(Cat(Seq.fill(20)(imm(pc>>2.U)(11))), imm(pc>>2.U)) + 0.U)
    }
    

    //test add
    val actual_sum = RegInit(0.U(32.W))
    val mem1 = (mem(addr1 << 2.U) >> (addr1(1,0) << 3))(7,0)
    val sext_mem1 = Cat(Cat(Seq.fill(24)(mem1(7))), mem1)
    val mem2 = (mem(addr2 << 2.U) >> (addr2(1,0) << 3))(7,0)
    val sext_mem2 = Cat(Cat(Seq.fill(24)(mem2(7))), mem2)

    when(pc>>2.U === 4.U){
        actual_sum := sext_mem1 + sext_mem2
        assert(sext_mem2 === dut.io.test.op1)
        assert(sext_mem1 === dut.io.test.op2)
    }

    when(pc>>2.U === 5.U){
        val sum = ((sext_mem1 + sext_mem2) << (data_addr(1,0) << 3))(31,0)
        printf("sum = %d, check sum = %d, actual_sum = %d, sext_mem1 = %d, sext_mem2 = %d\n", data, sum, actual_sum, sext_mem1, sext_mem2)

        assert(data === sum)
    }
    
    //test addi
    val actual_addi =RegInit(0.U(32.W))

    when(pc>>2.U === 7.U){
        val isum = actual_sum + Cat(Cat(Seq.fill(20)(imm((pc>>2.U)-1.U)(11))), imm((pc>>2.U)-1.U))
        actual_sum := isum
        val isum_rd = (isum << (data_addr(1,0) << 3))(31,0)
        printf("isum = %d, check isum = %d, isum_unshifted = %d, actual sum = %d, imm: %d\n", data, isum_rd, isum, actual_sum, imm((pc>>2.U)-1.U))
        assert(data === isum_rd)
    }

    //test branching
    val prev_instr = RegInit(0.U(32.W))
    //not taken
    when(pc>>2.U === 8.U){
        prev_instr := pc
    }
    //taken
    when(pc>>2.U === 9.U){
        assert(prev_instr + 4.U === pc)
    }
    when(pc>>2.U === 10.U){
        prev_instr := pc
    }
    when(pc>>2.U === 11.U){
        val extended = Cat(Cat(Seq.fill(20)(imm(11.U)(11))), imm(11.U)(10), imm(11.U)(9,4), imm(11.U)(3,0), 0.U(1.W))
        val inst_addr_check = prev_instr + extended
        printf("extended_check = %d, ", extended)
        printf("inst_addr = %d, inst_addr_check = %d\n", pc, inst_addr_check)
        assert(inst_addr_check === pc)
    }
    
    //check load with non 0 rs1
    when(pc>>2.U === 12.U){
        val addr = Cat(Cat(Seq.fill(20)(imm((pc>>2.U)-1.U)(11))), imm((pc>>2.U)-1.U)) + actual_addi
        val lddata = (actual_sum >> (addr(1,0) << 3))(31,0)
        val stdata = (lddata << (data_addr(1,0) << 3))(32,0) 
    }
    when(done) { stop(); stop() } 
}

class DataflowTests extends FlatSpec with Matchers {
  "Dataflow" should "pass" in {
    assert(TesterDriver execute (() => new Dataflow_tester))
  }
}