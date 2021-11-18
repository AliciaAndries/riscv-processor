package core

import chisel3._
import chisel3.util._

object FPGAInstructions {

   val all = VecInit(
        "b00000000000100000000000010010011".U,
        "b00001110000100000000000100010011".U,
        "b00000000101000000000000110010011".U,
        "b00000001100000000000100000010011".U,
        "b00000011010100000000101000010011".U,
        "b00000000001100010000001000110011".U,
        "b01000000001100100000001010110011".U,
        "b00000000000100101101001100110011".U,
        "b00000000001000110110001110110011".U,
        "b00000000000100010100010000110011".U,
        "b00000001100001000001010010010011".U,
        "b00000000011100101101011001100011".U,
        "b00000001000000111001001110110011".U,
        "b11111111100111111111000001101111".U,
        "b00000000010100111110011001100011".U,
        "b00000001100100111101001110010011".U,
        "b11111110011100101111110011100011".U,
        "b00000000010101000000011001100011".U,
        "b00000000000101000000010000010011".U,
        "b11111111100111111111000001101111".U,
        "b00000000010101000001011001100011".U,
        "b00000000000101000000010000010011".U,
        "b11111110100000101100110011100011".U,
        "b01000001010000000010010000100011".U,
        "b00000010010100011001111010100011".U,
        "b00000011110000011010010100000011".U,
        "b01000000010100000010001000100011".U,
        "b00000000100100101010111100100011".U,
        "b00000001110100101101010110000011".U,
        "b00000001110100101001011100000011".U,
        "b01000000111000000010001000100011".U,
        "b00000000111001011111011110110011".U,
        "b01000000111100000010001000100011".U,
        "b01000000101101001000011110110011".U,
        "b01000000111100000010001000100011".U,
        "b01000000001101110101011110110011".U,
        "b01000000111100000010001000100011".U,
        "b00000001000001001110100000010011".U,
        "b01000001000000000010001000100011".U,
        "b00000001000010000100100000010011".U,
        "b00000001000010000111100010010011".U,
        "b01000001000100000010001000100011".U,
        "b01000001000110000101100000010011".U,
        "b00000001111000101100100000000011".U,
        "b00000010100000101000011100100011".U,
        "b00000010111000101000011100000011".U,
        "b01000001000000000010001000100011".U,
        "b00000000000101110011011110010011".U,
        "b01000000111100000010001000100011".U,
        "b00000000000101110010011110010011".U,
        "b01000000111100000010001000100011".U,
        "b00000000001101110010011110110011".U,
        "b01000000111100000010001000100011".U,
        "b00000000001101110011011110110011".U,
        "b01000000111100000010001000100011".U,
        "b00000000000000011001011000110111".U,
        "b00000000000000000001011010010111".U,
        "b01000000110100000010001000100011".U,
        "b00000001000001101000000001100111".U,
        "b00000000000000000000000000010011".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
        "b00000000000000000000000000000000".U,
    )
}
