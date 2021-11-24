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

for i in range(0, 6):
    for line in Lines:
        line = Lines[random.randint(0, len(Lines)-1)]

        line = line.rstrip()
        line = "mem[" + str(count) + "]" + "=" + "32'h" + line + ";\n"
        hex.append(line)
        count = count + 1
        
file1 = open('init.txt', 'w')
file1.writelines((hex))
file1.close()
file2.close()