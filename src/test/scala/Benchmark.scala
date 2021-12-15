package core
import java.io._

import chisel3.iotesters.PeekPokeTester
import chisel3.iotesters.ChiselFlatSpec
import chisel3.iotesters

import scala.io.{BufferedSource, Source}
import firrtl.FileUtils
import java.nio.file.{Files, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

case class CoreTester_benchmark(c: CoreTest[IMemory, Dataflow], dir: String) extends PeekPokeTester(c) {
    // PrintWriter

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
    for (lp <- 0 until 600 by 1) {
        poke(c.io.fpgatest.halt_in, 0)
        val a           = peek(signal = c.io.fpgatest.decode_pc)   // pc count
        val d           = peek(signal = c.io.fpgatest.decode_inst)  // instruction
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
        // if you need fire external interrupt signal uncomment below
        //    if(lp == 96){
        //      poke(signal = c.io.sw.w_interrupt_sig, value = 1)
        //    }
        //    else{
        //      poke(signal = c.io.sw.w_interrupt_sig, value = 0)
        //    }
    /*
        if( (lp >= 20 && lp<=25) || lp == 65 || lp == 90){
        poke(signal = c.io.sw.w_waitrequest_sig, value = 1)
        }
        else{
        poke(signal = c.io.sw.w_waitrequest_sig, value = 0)
        }
    */
        poke(signal = c.io.fpgatest.reg_addr, value = 3)
        val data = {
            peek(signal = c.io.fpgatest.reg_data)
        }
        step(1)
        pw.println(f"0x$a%04X, \t0x$d%08X\t| x($exraddr1)=>0x$exrs1%08X, x($exraddr2)=>0x$exrs2%08X, select1=$reg_input1%08X select2=$reg_input2%08X  aluresult = $exalu%d, x($wbaddr%d)\t<= 0x$wbdata%08X, $stallsig%x, $data%x") //peek(c.io.sw.data)
        //pw.println(f"0x$a%04X,\t0x$d%08X\t| x($exraddr1)=>0x$exrs1%08X, x($exraddr2)=>0x$exrs2%08X,\t0x$eximm%08X\t| 0x$memaluo%08X\t| 0x$wbaluo%08X, x($wbaddr%d)\t<= 0x$wbdata%08X, $stallsig%x") //peek(c.io.sw.data)

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
"rv32ui-p-addi.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-addi.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/dhrystone.hex",5300), true)
    ) { c =>
        new CoreTester_benchmark(c,"src/test/official_resources/dhrystone.hex")
    } should be (true)
}
}