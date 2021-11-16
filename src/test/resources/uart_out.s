nop
addi x3, x0, 0x3
addi x4, x0, 0x15
addi x5, x0, 0x5

sw   x3, 0x400(x0)
sw   x4, 0x408(x0)
_loop:
sw   x3, 0x404(x0)
sw   x5, 0x404(x0)
sw   x3, 0x404(x0)
sw   x5, 0x404(x0)
jal  x0, _loop
