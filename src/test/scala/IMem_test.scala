package core

import java.nio.file.{Files, Paths}
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import chisel3._
import chisel3.util._
import chisel3.iotesters._
import org.scalatest.{Matchers, FlatSpec}
import firrtl.FileUtils
//import org.scalatest.freespec.AnyFreeSpec


class LoadMemoryFromFileTester(c: IMemory) extends PeekPokeTester(c) {
  for(addr <- 0 until 45*4 by 4) {
    poke(c.io.req.bits.addr, addr)
    poke(c.io.req.bits.mask, 0.U)
    poke(c.io.req.valid, true.B)
    step(1)
    println(f"peek from $addr ${peek(c.io.resp.bits.data)}%x")
    //expect(c.io.value, addr)
  }
}

class LoadMemoryFromFileSpec extends FlatSpec with Matchers {
  "this" should "Users can specify a source file to load memory from" in {

    val targetDirName = "test_run_dir/load_mem_test"
    FileUtils.makeDirectory(targetDirName)

    val path1 = Paths.get(targetDirName + "/all.hex")
    Files.copy(getClass.getResourceAsStream("/all.hex"), path1, REPLACE_EXISTING)

    iotesters.Driver.execute(
      args = Array("--backend-name", "verilator", "--target-dir", targetDirName, "--top-name", "load_mem_test"),
      dut = () => new IMemory("load_mem_test")
    ) { c =>
      new LoadMemoryFromFileTester(c)
    } should be (true)
  }
}