package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import Opcode._

class Dataflow_tester extends BasicTester{
    val dut = Module(new Dataflow(true))

    def toBigInt(x: Int) = (BigInt(x >>> 1) << 1) | (x & 0x1)
    val nr_insts = 29
    val NOP = Cat(0.U(12.W), 0.U(5.W), Funct3.ADD, 0.U(5.W), Opcode.ITYPE)

    val rnd = new scala.util.Random
    
    val imm = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    val rs1 = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))

    val reg_idx = scala.util.Random.shuffle(1 to 31)    //different reg indexes otherwise they overwrite each other    
    
    val st_offset = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<12).U(12.W)))
    val base = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<5).U(5.W)))

    val func3_ran = VecInit(Seq.fill(nr_insts)(rnd.nextInt(1<<3).U(3.W)))

    val insts = Seq(
        NOP,                                                                                                    //give time to mem to init
        Cat(imm(1.U), 0.U(5.W), Funct3.LB, reg_idx(1).U(5.W), Opcode.LOAD),                                          //write data to register reg_idx(0.U)
        Cat(imm(2.U), 0.U(5.W), Funct3.LB, reg_idx(2).U(5.W), Opcode.LOAD),                                          //write data to register reg_idx(1.U)
        NOP,                                                                                                    //

        Cat(Funct7.U, reg_idx(1).U(5.W), reg_idx(2).U(5.W), Funct3.ADD, reg_idx(4).U(5.W), Opcode.RTYPE),                      //reg_idx(0.U) + reg_idx(1.U) to reg_idx(2.U)
        Cat(st_offset(5.U)(11,5), reg_idx(4).U(5.W), base(5.U), Funct3.SB, st_offset(5.U)(4,0), Opcode.STORE),       //read reg_idx(2.U)

        Cat(imm(6.U), reg_idx(4).U(5.W), Funct3.ADD, reg_idx(6).U(5.W), Opcode.ITYPE),                                    //addi reg_idx(4.U) + imm
        Cat(st_offset(7.U)(11,5), reg_idx(6).U(5.W), base(7.U), Funct3.SB, st_offset(7.U)(4,0), Opcode.STORE),       //read the result of addi

        Cat(imm(9.U)(11), imm(9.U)(9,4), reg_idx(6).U(5.W), reg_idx(4).U(5.W), Funct3.ADD, imm(9.U)(3,0), imm(9.U)(10), Opcode.BRANCH),       //branch not taken
                                            //reg_idx(7.0) === reg_idx(4.U) -> problem
        Cat(imm(6.U), reg_idx(4).U(5.W), Funct3.ADD, reg_idx(7).U(5.W), Opcode.ITYPE),
        Cat(imm(10.U)(11), imm(10.U)(9,4), reg_idx(6).U(5.W), reg_idx(7).U(5.W), Funct3.ADD, imm(10.U)(3,0), imm(10.U)(10), Opcode.BRANCH),  //branch taken

        Cat(imm(11.U), reg_idx(6).U(5.W), Funct3.LB, reg_idx(11).U(5.W), Opcode.LOAD),                                    //check if branch is taken + load into 12.U
        Cat(st_offset(12.U)(11,5), reg_idx(11).U(5.W), base(12.U), Funct3.SB, st_offset(12.U)(4,0), Opcode.STORE),    //read the result of load

        //LUI
        Cat(imm(13.U), base(13.U), func3_ran(13.U), reg_idx(13).U(5.W), Opcode.LUI),               //add 
        Cat(st_offset(14.U)(11,5), reg_idx(13).U(5.W), base(14.U), Funct3.SB, st_offset(14.U)(4,0), Opcode.STORE),       //read reg_idx(13.U)
        //AUIPC
        Cat(imm(15.U), base(15.U), func3_ran(15.U), reg_idx(15).U(5.W), Opcode.AUIPC),
        Cat(st_offset(16.U)(11,5), reg_idx(15).U(5.W), base(16.U), Funct3.SB, st_offset(16.U)(4,0), Opcode.STORE),       //read reg_idx(15.U)
        //JAL
        Cat(imm(17.U), base(17.U), func3_ran(17.U), reg_idx(17).U(5.W), Opcode.JAL),                                     //jump to pc + im
        Cat(st_offset(18.U)(11,5), reg_idx(17).U(5.W), base(18.U), Funct3.SW, st_offset(18.U)(4,0), Opcode.STORE),       //read pc + 4
        //JALR
        Cat(imm(19.U), reg_idx(1).U(5.W), Funct3.ADD, reg_idx(19).U(5.W), Opcode.JALR),                                       //jump to pc + reg(1.U)
        Cat(st_offset(20.U)(11,5), reg_idx(19).U(5.W), base(20.U), Funct3.SW, st_offset(20.U)(4,0), Opcode.STORE),       //read pc + 4

        //LDs&&STs
        Cat(imm(21.U), reg_idx(6).U(5.W), Funct3.LBU, reg_idx(21).U(5.W), Opcode.LOAD), 
        Cat(imm(22.U), reg_idx(6).U(5.W), Funct3.LH, reg_idx(22).U(5.W), Opcode.LOAD), 
        Cat(imm(23.U), reg_idx(1).U(5.W), Funct3.LHU, reg_idx(23).U(5.W), Opcode.LOAD),
        Cat(imm(24.U), reg_idx(1).U(5.W), Funct3.LW, reg_idx(24).U(5.W), Opcode.LOAD),  
        
        Cat(st_offset(25.U)(11,5), reg_idx(21).U(5.W), base(25.U), Funct3.SB, st_offset(25.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(26.U)(11,5), reg_idx(22).U(5.W), base(26.U), Funct3.SH, st_offset(26.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(27.U)(11,5), reg_idx(23).U(5.W), base(27.U), Funct3.SW, st_offset(27.U)(4,0), Opcode.STORE),       //read
        Cat(st_offset(28.U)(11,5), reg_idx(24).U(5.W), base(28.U), Funct3.SW, st_offset(28.U)(4,0), Opcode.STORE),       //read
    )

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
    /* assert(reg_idx(cntr) =/= 0.U) */

    //print all info that could be handy for debugging
    printf("count = %d, PC = %d, valid_d_out = %d, data_to_reg = %d, data_addr(1,0) = %d, wb_data = %d, alu_op1 = %d, alu_op2 = %d, aluresult = %d, raddr1 = %d, raddr2 = %d, rs2 = %d, rd = %d",
            cntr, pc, loadvalid, data_to_reg, data_addr(1,0), dut.io.test.wb_data, dut.io.test.op1, dut.io.test.op2, dut.io.test.aluresult, dut.io.test.raddr1, dut.io.test.raddr2, dut.io.test.rs2, dut.io.test.rwdata)
    when(inst(6,0)===Opcode.LOAD){
        printf(", load\n\n")
    }.otherwise{
        printf("\n\n")
    }
    //check if this is the case, shouldnt happen anymore cause you rs1 is set to 0
    when(cntr === 1.U){
        assert(data_addr === addr1)
    }
    when(cntr === 2.U){
        assert(data_addr === addr2)
    }

    //Test the generated data_addr for the loads compared to what they should be
    when(cntr < 2.U && pc.orR){
        assert(data_addr === Cat(Cat(Seq.fill(20)(imm(cntr)(11))), imm(cntr)) + 0.U)
    }
    

    //test add
    val actual_sum = RegInit(0.U(32.W))
    val mem1 = (mem(addr1 << 2.U) >> (addr1(1,0) << 3))(7,0)
    val sext_mem1 = Cat(Cat(Seq.fill(24)(mem1(7))), mem1)
    val mem2 = (mem(addr2 << 2.U) >> (addr2(1,0) << 3))(7,0)
    val sext_mem2 = Cat(Cat(Seq.fill(24)(mem2(7))), mem2)

    when(cntr === 4.U){
        actual_sum := sext_mem1 + sext_mem2
        assert(sext_mem2 === dut.io.test.op1)
        assert(sext_mem1 === dut.io.test.op2)
    }

    when(cntr === 5.U){
        val sum = ((sext_mem1 + sext_mem2) << (data_addr(1,0) << 3))(31,0)
        printf("sum = %d, check sum = %d, actual_sum = %d, sext_mem1 = %d, sext_mem2 = %d\n", data, sum, actual_sum, sext_mem1, sext_mem2)

        assert(data === sum)
    }
    
    //test addi
    val actual_addi =RegInit(0.U(32.W))

    when(cntr === 7.U){
        val isum = actual_sum + Cat(Cat(Seq.fill(20)(imm((cntr)-1.U)(11))), imm((cntr)-1.U))
        actual_addi := isum
        val isum_rd = (isum << (data_addr(1,0) << 3))(31,0)
        printf("isum = %d, check isum = %d, isum_unshifted = %d, actual sum = %d, imm: %d\n", data, isum_rd, isum, actual_sum, imm((cntr)-1.U))
        assert(data === isum_rd)
    }

    //test branching
    val prev_instr = RegInit(0.U(32.W))
    //not taken
    when(cntr === 8.U){
        prev_instr := pc
    }
    //taken
    when(cntr === 9.U){
        assert(prev_instr + 4.U === pc)
    }
    when(cntr === 10.U){
        prev_instr := pc
    }
    when(cntr === 11.U){
        val extended = Cat(Cat(Seq.fill(20)(imm(10.U)(11))), imm(10.U)(10), imm(10.U)(9,4), imm(10.U)(3,0), 0.U(1.W))
        val inst_addr_check = prev_instr + extended
        printf("extended_check = %d, ", extended)
        printf("inst_addr = %d, inst_addr_check = %d\n", pc, inst_addr_check)
        assert(inst_addr_check === pc)
    }
    
    //check load with non 0 rs1
    val addr11 = Cat(Cat(Seq.fill(20)(imm(11.U)(11))), imm(11.U)) + actual_addi
    val data11 = actual_sum
    when(cntr === 8.U){
        mem(addr11 << 2.U) := data11
    }
    when(cntr === 12.U){
        val addr = Cat(Cat(Seq.fill(20)(imm((cntr)-1.U)(11))), imm((cntr)-1.U)) + actual_addi
        val lddata_non_ext = (actual_sum >> (addr(1,0) << 3))(7,0)
        val lddata = Cat(Cat(Seq.fill(24)(lddata_non_ext(7))),lddata_non_ext)
        val stdata = (lddata << (data_addr(1,0) << 3))(31,0) 
        printf("stdata = %d, data = %d, reg_mem = %d\n", stdata, data, actual_sum)
        assert(addr === addr11)
        assert(stdata === data)
    }

    //LUI
    when(cntr === 14.U){
        val literal = Cat(imm(cntr - 1.U), base(cntr - 1.U), func3_ran(cntr - 1.U))
        val extended = Cat(literal, Cat(Seq.fill(12)(0.U)))
        val stdata = (extended << (data_addr(1,0) << 3))(31,0) 
        printf("extended = %d, data = %d, og = %d\n", stdata, data, literal)
        assert(stdata === data)
    }
    //AUIPC
    when(cntr === 15.U){
        prev_instr := pc
    }
    when(cntr === 16.U){
        val literal = Cat(imm(cntr - 1.U), base(cntr - 1.U), func3_ran(cntr - 1.U))
        val extended = Cat(literal, Cat(Seq.fill(12)(0.U)))
        val sum = extended + prev_instr
        val stdata = (sum << (data_addr(1,0) << 3))(31,0) 
        printf("extended = %d, data = %d, og = %d\n", stdata, data, literal)
        assert(stdata === data)
    }
    //JAL
    when(cntr === 17.U){
        prev_instr := pc
    }
    when(cntr === 18.U){
        val literal = Cat(Cat(Seq.fill(12)(imm(cntr - 1.U)(11))), base(cntr - 1.U), func3_ran(cntr - 1.U), imm(cntr-1.U)(0), imm(cntr-1.U)(10,1), 0.U(1.W))
        val check_pc = literal + prev_instr
        val stored_pc = prev_instr + 4.U
        val stdata = stored_pc(31,0) 
        printf("storedpc = %d, data = %d, current_pc = %d, check_pc = %d\n", stdata, data, pc, check_pc)
        assert(stdata === data)
        assert(pc === check_pc)
    }
    //JALR
    when(cntr === 19.U){
        prev_instr := pc
    }
    when(cntr === 20.U){
        val literal = Cat(Cat(Seq.fill(20)(imm(cntr-1.U)(11))), imm(cntr-1.U))
        val check_pc = literal + sext_mem1
        val stored_pc = prev_instr + 4.U
        val stdata = stored_pc(31,0) 
        printf("storedpc = %d, data = %d, current_pc = %d, check_pc = %d, literal = %d, data1_sext = %d, isum = %d\n", stdata, data, pc, check_pc, literal, sext_mem1, actual_addi)
        assert(stdata === data)
        assert(pc === check_pc)
    }
    //LOADS and STORES
    val addr21 = Cat(Cat(Seq.fill(20)(imm(21.U)(11))), imm(21.U)) + actual_addi
    val data21 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    val addr22 = Cat(Cat(Seq.fill(20)(imm(22.U)(11))), imm(22.U)) + actual_addi
    val data22 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    val addr23 = Cat(Cat(Seq.fill(20)(imm(23.U)(11))), imm(23.U)) + sext_mem1
    val data23 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    val addr24 = Cat(Cat(Seq.fill(20)(imm(24.U)(11))), imm(24.U)) + sext_mem1
    val data24 = (toBigInt(rnd.nextInt(1<<8) << 24) + toBigInt(rnd.nextInt(1<<8) << 16) + toBigInt(rnd.nextInt(1<<8) << 8) + toBigInt(rnd.nextInt(1<<8))).U(32.W)
    val ldaddrs = VecInit(addr21, addr22, addr23, addr24) 
    //After prev ld/st otherwise may overwite and it will break
    when(cntr === 13.U){
        mem(addr21 << 2.U) := data21
        mem(addr22 << 2.U) := data22
        mem(addr23 << 2.U) := data23
        mem(addr24 << 2.U) := data24
    }
    when(cntr > 20.U && cntr < 25.U){
        val check_addr = ldaddrs(cntr-21.U)
        printf("data_addr = %d, check_addr= %d\n", check_addr, data_addr)
        assert(check_addr === data_addr)
    }
    when(cntr === 25.U ){
        val lddata_non_ext = (data21 >> (addr21(1,0) << 3))(7,0)
        val lddata = Cat(Cat(Seq.fill(24)(0.U)),lddata_non_ext)
        val stdata = (lddata << (data_addr(1,0) << 3))(31,0) 
        val stmask = "b1".U << data_addr(1,0)
        printf("stdata = %d, data = %d, reg_mem = %d, addr21 = %d, mask = %d, check_mask = %d\n", stdata, data, data21, addr21, mask, stmask)
        assert(stdata === data)
        assert(stmask === mask)
    }
    when(cntr === 26.U ){
        val ldoffset = addr22(1,0) & "b10".U
        val lddata_non_ext = (data22 >> (ldoffset << 3))(15,0)
        val lddata = Cat(Cat(Seq.fill(16)(lddata_non_ext(15.U))),lddata_non_ext)
        val stoffset = data_addr(1,0) & "b10".U
        val stdata = (lddata << (stoffset << 3))(31,0) 
        val stmask = "b11".U << stoffset
        printf("stdata = %d, data = %d, reg_mem = %d, addr21 = %d, mask = %d, check_mask = %d\n", stdata, data, data22, addr22, mask, stmask)
        assert(stdata === data)
        assert(stmask === mask)
    }
    when(cntr === 27.U){
        val ldoffset = addr23(1,0) & "b10".U
        val lddata_non_ext = (data23 >> (ldoffset << 3))(15,0)
        val lddata = Cat(Cat(Seq.fill(16)(0.U)),lddata_non_ext)
        val stoffset = 0.U
        val stdata = (lddata << (stoffset << 3))(31,0) 
        val stmask = "b1111".U << stoffset
        printf("stdata = %d, data = %d, reg_mem = %d, addr21 = %d, mask = %d, check_mask = %d\n", stdata, data, data23, addr23, mask, stmask)
        assert(stdata === data)
        assert(stmask === mask)
    }
    when(cntr === 28.U){
        val ldoffset = 0.U
        val lddata_non_ext = (data24 >> (ldoffset << 3))(31,0)
        val lddata = Cat(Cat(Seq.fill(0)(0.U)),lddata_non_ext)
        val stoffset = 0.U
        val stdata = (lddata << (stoffset << 3))(31,0) 
        val stmask = "b1111".U << stoffset
        printf("stdata = %d, data = %d, reg_mem = %d, addr21 = %d, mask = %d, check_mask = %d\n", stdata, data, data23, addr23, mask, stmask)
        assert(stdata === data)
        assert(stmask === mask)
    }
    //LHU and that stuff
    when(done) { stop(); stop() } 
}

class DataflowTests extends FlatSpec with Matchers {
  "Dataflow" should "pass" in {
    assert(TesterDriver execute (() => new Dataflow_tester))
  }
}