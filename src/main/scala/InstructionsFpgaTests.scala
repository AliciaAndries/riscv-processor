package core

import chisel3._
import chisel3.util._

object FPGAInstructions {

    val all = VecInit(
        "b00000000101000000000000010010011".U,	//ADDI 10
        "b00000000011000000000000100010011".U,	//ADDI 6
        "b00000000001000001110000110110011".U,	//OR 14
        "b00000000101000000000000010010011".U,	//ADDI 10
        "b00000000011000000000000100010011".U,	//ADDI 6
        "b01000000001000001000000110110011".U,	//SUB 4
        "b00000000101000000000000010010011".U,	//ADDI 10
        "b00000000011000000000000100010011".U,	//ADDI 6
        "b00000000001000001111000110110011".U,	//AND -> 2
        "b00000000101000000000000010010011".U,  //ADDI1 10
        "b00000000101000000000000100010011".U,  //ADDI2 10
        "b00000000001000001000000110110011".U,  //ADD ADDI1 + ADDI2
        "b00000000101000000000000010010011".U,  //addi
        "b00000000000101110000100000100011".U,  //st
        "b00000001000001110000000100000011".U,  //ld
        "b00000000000000000000000000000011".U,  //ld nothing to nothing
        "b00000000101000000000000010010011".U,  //ADDI
        "b00000000000000001000100001100011".U,  //BEQ NT
        "b00000000101000000000000100010011".U,  //ADDI
        "b00000000001000001000100001100011".U,  //BEQ T
        "b00000000101000000000000000010011".U,  //ADDI to nowhere
        "b00000000001000001110000110110011".U,
        "b00000000101000000000000010010011".U,
        "b00000000011000000000000100010011".U,
        "b00000000001000001110000110110011".U,
        "b00000000101000000000000010010011".U,
        "b00000000011000000000000100010011".U,
        "b00000000001000001110000110110011".U,
        "b00000000101000000000000010010011".U,
        "b00000000011000000000000100010011".U,
        "b00000000001000001110000110110011".U,
        "b00000000101000000000000010010011".U
    )
}