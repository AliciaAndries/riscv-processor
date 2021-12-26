package core
import java.io._

import chisel3.iotesters.PeekPokeTester
import chisel3.iotesters.ChiselFlatSpec
import chisel3.iotesters

import scala.io.{BufferedSource, Source}
import firrtl.FileUtils
import java.nio.file.{Files, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

case class CoreTester_benchmark(c: CoreTest[IMemory, DataflowALUSplit], dir: String) extends PeekPokeTester(c) {
    // PrintWriter

    val start = BigInt("f3470713", 16)
    val end = BigInt("d6490913", 16)//BigInt("b00026f3", 16)

    val pw = new PrintWriter(new File("log.txt" ))
    poke(c.io.fpgatest.reg_addr, 0)
    poke(c.io.fpgatest.halt_in, 0)

    val s: BufferedSource = Source.fromFile(dir)
    var buffs: Array[String] = _
    try {
        buffs = s.getLines.toArray
    } finally {
        s.close()
    }
    printf("length = %d\n",buffs.length)

    for (addr <- 0 until buffs.length * 4 by 4) {
        poke(c.io.fpgatest.halt_in, 1)
        val mem_val = buffs(addr / 4).replace(" ", "")
        val mem = Integer.parseUnsignedInt(mem_val, 16)

        poke(signal = c.io.fpgatest.waddr, value = addr)
        poke(signal = c.io.fpgatest.wdata, value = mem)
        poke(signal = c.io.fpgatest.wmask, value = 15)
        pw.println(f"write: addr = 0x$addr%04X,\tdata = 0x$mem%08X")
        step(1)
    }
    poke(c.io.fpgatest.halt_in, 0)
    poke(c.io.fpgatest.pc_reset, 1)
    step(1)
    poke(c.io.fpgatest.pc_reset, 0)
    // about 1000 cycle, we can finish 'riscv-tests'.
    // change parameters on your another projects.
    var to_bench_started = false
    var start_seen_once = false
    var inst = BigInt(0)
    var bench_not_ended = true
    var count = 0
    while(bench_not_ended) {
        poke(c.io.fpgatest.halt_in, 0)
        val a           = peek(signal = c.io.fpgatest.decode_pc)   // pc count
        inst           = peek(signal = c.io.fpgatest.decode_inst)  // instruction
        val exraddr1    = peek(c.io.fpgatest.rs1)     // rs1 address
        val exraddr2    = peek(c.io.fpgatest.rs2)     // rs2 address
        val exrs1       = peek(c.io.fpgatest.rs1data)        // rs1 data
        val exrs2       = peek(c.io.fpgatest.rs2data)        // rs2 data
        val reg_input1  = peek(c.io.fpgatest.reg_input1) 
        val reg_input2  = peek(c.io.fpgatest.reg_input2) 
        //val eximm       = peek(c.io.sw.r_ex_imm)        // imm
        val exalu       = peek(c.io.fpgatest.aluresult)   // alu(MEM stage)
        //val wbaluo      = peek(c.io.sw.r_wb_alu_out)    // alu(WB stage)
        val wbaddr      = peek(c.io.fpgatest.id_ex_rd)   // write-back rd address
        val wbdata      = peek(c.io.fpgatest.wb)   // write-back rd data
        val stallsig    = peek(c.io.fpgatest.halt)     // stall signal
        poke(signal = c.io.fpgatest.reg_addr, value = 3)
        val data = {
            peek(signal = c.io.fpgatest.reg_data)
        }
        step(1)
        pw.println(f"0x$a%04X, \t0x$inst%08X\t| x($exraddr1)=>0x$exrs1%08X, x($exraddr2)=>0x$exrs2%08X, select1=$reg_input1%08X select2=$reg_input2%08X  aluresult = $exalu%d, x($wbaddr%d)\t<= 0x$wbdata%08X, $stallsig%x, $data%x") //peek(c.io.sw.data)
        //pw.println(f"0x$a%04X,\t0x$d%08X\t| x($exraddr1)=>0x$exrs1%08X, x($exraddr2)=>0x$exrs2%08X,\t0x$eximm%08X\t| 0x$memaluo%08X\t| 0x$wbaluo%08X, x($wbaddr%d)\t<= 0x$wbdata%08X, $stallsig%x") //peek(c.io.sw.data)

        if(inst == start){
            to_bench_started = true
            println("i made it to the first csrr")

        }
        if(to_bench_started){
            count += 1
        }
        if(inst == end){
            bench_not_ended = false
        }
    }
    
  step(1)
  pw.println("---------------------------------------------------------")

  poke(signal = c.io.fpgatest.halt_in, value = 1)
  step(2)

  for (lp <- 0 to 31 by 1) {
    poke(signal = c.io.fpgatest.halt_in, value = 1)

    poke(signal = c.io.fpgatest.reg_addr, value = lp)
    val d = {
        peek(signal = c.io.fpgatest.reg_data)
    }
    step(1)
    pw.println(f"read : x$lp%2d = 0x$d%08X ") //peek(c.io.sw.data)
  }

    poke(signal = c.io.fpgatest.reg_addr, value = 3)                 // select x3 register (gp)
    step(1)
    var User_Time = count
    var Number_Of_Runs = 500
    var Mic_secs_Per_Second = 1000000
    var HZ = 1000000
    var microseconds = ((User_Time / Number_Of_Runs) * Mic_secs_Per_Second) / HZ;
    var dhrystones_Per_Second = (HZ * Number_Of_Runs) / User_Time;
    pw.println(f"time : $count")
    pw.println(f"Microseconds for one run through Dhrystone: $microseconds")
    pw.println(f"Dhrystone per second: $dhrystones_Per_Second")
    val d: BigInt = peek(signal = c.io.fpgatest.reg_data)
    step(1)
    if (d == 1) {                                           // if(x3 == 1) then PASS else FAIL
        pw.println(f"PASS: simulation finished.")
    } else {
        pw.println(f"FAIL: simulation finished.")
    }
    pw.println(f"simulation finished")
    expect(c.io.fpgatest.reg_data, expected = 1)
    
    pw.close

}

class Benchmarks extends ChiselFlatSpec {
"dhrystone tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-addi.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(131072, new IMemory("src/test/official_resources/dhrystone.hex",5300), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_benchmark(c,"src/test/official_resources/dhrystone.hex")
    } should be (true)
}
}