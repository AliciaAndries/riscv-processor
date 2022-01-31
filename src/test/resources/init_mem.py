import numpy as np
import sys
import random

file_name = str(sys.argv[1])
file2 = open(file_name, 'r')

Lines = file2.readlines()
count = 0
hex = []
bin_L = []

count = 0
for line in Lines:
    line = line.rstrip()
    line = "mem[" + str(count) + "]" + "=" + "32'h" + line + ";\n"
    hex.append(line)
    count = count + 1

""" for i in range(0, 6):
    for line in Lines:
        line = Lines[random.randint(0, len(Lines)-1)]

        line = line.rstrip()
        line = "mem[" + str(count) + "]" + "=" + "32'h" + line + ";\n"
        hex.append(line)
        count = count + 1 """

file1 = open('init.txt', 'w')
file1.writelines((hex))

file3 = open("/home/alicia/Documents/thesis/riscv-processor/Core.v", "r")

core_lines = file3.readlines()
core_line_nr = 0
module_is_mem = False
half1 = []
half2 = []
file3.close()


for line in core_lines:
    line = line.rstrip()
    if line == "module IMemory(":
        module_is_mem = True
    if module_is_mem:
        if line == "// Register and memory initialization":
            half1 = core_lines[:core_line_nr + 1]
        if line == "endmodule":
            half2 = core_lines[core_line_nr:len(core_lines)]
            break
    core_line_nr = core_line_nr + 1
file3.close()

middle_front = ["initial begin\n"]
middle = ["end // initial\n"]
file3 = open("/home/alicia/Documents/thesis/riscv-processor/Core.v", "w")
file3.writelines(half1 + middle_front+ hex + middle + half2)


file1.close()
file2.close()


# search for "module IMemory(" then get rid of content between first "initial begin" and "endmodule"
# add lines from .hex file then add "end // initial"