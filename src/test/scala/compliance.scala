package core
import java.io._

import chisel3.iotesters.PeekPokeTester
import chisel3.iotesters.ChiselFlatSpec
import chisel3.iotesters
import chisel3.experimental.BaseModule

import scala.io.{BufferedSource, Source}
import firrtl.FileUtils
import java.nio.file.{Files, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING


import java.io.{BufferedWriter, FileWriter ,PrintWriter}

case class CoreTester_compliance(c: CoreTest[IMemory, DataflowALUSplit], dir: String) extends PeekPokeTester(c) {
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

class TestCoreAll extends ChiselFlatSpec {
     "rv32ui-p-add.hex tester using iotesters" should "be pass test." in {
        val targetDirName = "test_run_dir/CoreTester_compliance"
        FileUtils.makeDirectory(targetDirName)

        val path1 = Paths.get(targetDirName + "/rv32ui-p-add.hex")
        //Files.copy(getClass.getResourceAsStream("/rv32ui-p-add.hex"), path1, REPLACE_EXISTING)

        iotesters.Driver.execute(
        args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
        dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-add.hex",2500), true, new DataflowALUSplit(true))
        ) { c =>
            new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-add.hex")
        } should be (true)
        /* iotesters.Driver.execute(Array(), () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-add.hex",2500), true, new Dataflow(true)))(testerGen = c => { 
            CoreTester_compliance(c)
        }) should be (true) */
    }
/*  "rv32ui-p-addi.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-addi.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-addi.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-addi.hex")
    } should be (true)
}

"rv32ui-p-and.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-and.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-and.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-and.hex")
    } should be (true)
}

"rv32ui-p-andi.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-andi.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-andi.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-andi.hex")
    } should be (true)
}

"rv32ui-p-auipc.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-auipc.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-auipc.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-auipc.hex")
    } should be (true)
}
"rv32ui-p-beq.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-beq.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-beq.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-beq.hex")
    } should be (true)
}

"rv32ui-p-bge.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-bge.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-bge.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-bge.hex")
    } should be (true)
}

"rv32ui-p-bgeu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-bgeu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-bgeu.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-bgeu.hex")
    } should be (true)
}

"rv32ui-p-blt.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-blt.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-blt.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-blt.hex")
    } should be (true)
}

"rv32ui-p-bltu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-bltu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-bltu.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-bltu.hex")
    } should be (true)
}

"rv32ui-p-bne.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-bne.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-bne.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-bne.hex")
    } should be (true)
}

/* "rv32ui-p-fence_i.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-fence_i.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-fence_i.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-fence_i.hex")
    } should be (true)
} */

"rv32ui-p-jal.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-jal.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-jal.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-jal.hex")
    } should be (true)
}

"rv32ui-p-jalr.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-jalr.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-jalr.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-jalr.hex")
    } should be (true)
}
*/
"rv32ui-p-lb.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lb.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lb.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lb.hex")
    } should be (true)
}

"rv32ui-p-lbu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lbu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lbu.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lbu.hex")
    } should be (true)
}

"rv32ui-p-lh.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lh.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lh.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lh.hex")
    } should be (true)
}

"rv32ui-p-lhu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lhu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lhu.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lhu.hex")
    } should be (true)
}
/*
"rv32ui-p-lui.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lui.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lui.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lui.hex")
    } should be (true)
}

"rv32ui-p-lw.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-lw.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-lw.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-lw.hex")
    } should be (true)
}

"rv32ui-p-or.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-or.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-or.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-or.hex")
    } should be (true)
}

"rv32ui-p-ori.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-ori.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-ori.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-ori.hex")
    } should be (true)
}
*/
"rv32ui-p-sb.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sb.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sb.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sb.hex")
    } should be (true)
}

"rv32ui-p-sh.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sh.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sh.hex",2500), true, new DataflowALUSplit(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sh.hex")
    } should be (true)
}
/*
"rv32ui-p-simple.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-simple.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-simple.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-simple.hex")
    } should be (true)
}

"rv32ui-p-sll.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sll.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sll.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sll.hex")
    } should be (true)
}

"rv32ui-p-slli.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-slli.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-slli.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-slli.hex")
    } should be (true)
}

"rv32ui-p-slt.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-slt.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-slt.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-slt.hex")
    } should be (true)
}

"rv32ui-p-slti.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-slti.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-slti.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-slti.hex")
    } should be (true)
}

"rv32ui-p-sltiu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sltiu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sltiu.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sltiu.hex")
    } should be (true)
}

"rv32ui-p-sltu.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sltu.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sltu.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sltu.hex")
    } should be (true)
}

"rv32ui-p-sra.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sra.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sra.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sra.hex")
    } should be (true)
}

"rv32ui-p-srai.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-srai.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-srai.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-srai.hex")
    } should be (true)
}

"rv32ui-p-srl.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-srl.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-srl.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-srl.hex")
    } should be (true)
}

"rv32ui-p-srli.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-srli.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-srli.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-srli.hex")
    } should be (true)
}

"rv32ui-p-sub.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sub.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sub.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sub.hex")
    } should be (true)
}

"rv32ui-p-sw.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-sw.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-sw.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-sw.hex")
    } should be (true)
}

"rv32ui-p-xor.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-xor.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-xor.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-xor.hex")
    } should be (true)
}

"rv32ui-p-xori.hex tester using iotesters" should "be pass test." in {
    val targetDirName = "test_run_dir/CoreTester_compliance"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/rv32ui-p-xori.hex")

    iotesters.Driver.execute(
    args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
    dut = () => new CoreTest(2500, new IMemory("src/test/official_resources/rv32ui-p-xori.hex",2500), true, new Dataflow(true))
    ) { c =>
        new CoreTester_compliance(c,"src/test/official_resources/rv32ui-p-xori.hex")
    } should be (true)
} */
} 