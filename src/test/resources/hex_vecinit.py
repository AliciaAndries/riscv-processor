import numpy as np

file2 = open('add.hex', 'r')

Lines = file2.readlines()
count = 0
hex = []
bin_L = []

for line in Lines:
    line = line.rstrip()
    line = str(bin(int(line, 16)))[2:].zfill(32)
    line = "\"b" +line + "\".U,\n"
    bin_L.append(line)
    count = count + 1

for line in Lines:
    line = line.rstrip()
    line = "0x" +line + ".U,\n"
    hex.append(line)
    count = count + 1

file1 = open('add_hex.txt', 'w')
file1.writelines((hex))
file1.close()
file3 = open('add_bin.txt', 'w')
file3.writelines((bin_L))
file3.close()
file2.close()