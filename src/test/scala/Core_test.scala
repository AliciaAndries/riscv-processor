package core

import java.nio.file.{Files, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import chisel3._
import chisel3.util._
import chisel3.iotesters._
import org.scalatest.{Matchers, FlatSpec}
import firrtl.FileUtils
import chisel3.experimental.BaseModule

class Core_tester(c: Core[IMemory]) extends PeekPokeTester(c) {    
  for(cntr <- 0 until 53) {
    println(f"ioled = ${c.io.ledio}%d, uart = ${c.io.uartSerialPort.tx}%d\n")
    step(1)
  }
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {

    val targetDirName = "test_run_dir/Core_tester"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/all.hex")
    Files.copy(getClass.getResourceAsStream("/all.hex"), path1, REPLACE_EXISTING)

    iotesters.Driver.execute(
      args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "Core_tester"),
      dut = () => new Core(new IMemory("Core_tester"), true)
    ) { c =>
      new Core_tester(c)
    } should be (true)
  }
}