import numpy as np
import sys

file_name = str(sys.argv[1])
file2 = open(file_name, 'r')

Lines = file2.readlines()
count = 0
hex = []
bin_L = []

for line in Lines:
    line = line.rstrip()
    line = str(bin(int(line, 16)))[2:].zfill(32)
    count = count + 1
    line = "        \"b" +line + "\".U,\n"
    bin_L.append(line)

for x in range(3):
    line = "        \"b00000000000000000000000000010011\".U,\n"
    if x == 29:
        line = "        \"b00000000000000000000000000010011\".U\n"
    bin_L.append(line)
    
    


for line in Lines:
    line = line.rstrip()
    line = "0x" +line + ".U,\n"
    hex.append(line)
    
        
    

file_name_hex = str(sys.argv[2])
file1 = open(file_name_hex, 'w')
file1.writelines((hex))
file1.close()

file_name_bin = "../../main/scala/InstructionsFpgaTests.scala"

lines3_start =  ["package core\n\n",
                 "import chisel3._\n",
                 "import chisel3.util._\n\n",
                 "object FPGAInstructions {\n\n",
                 "   val all = VecInit(\n"]

lines3_end =    ["    )\n", "}\n"]
file3 = open(file_name_bin, 'w')
file3.writelines((lines3_start+bin_L+ lines3_end))
file3.close()
file2.close()