nop
addi x1, x0, 0x6
addi x2, x0, 0x5
beq  x1, x2, _equal
addi x2, x2, 0x1
beq  x1, X2, _equal
addi X2, x2, 0x1
addi X2, x2, 0x1
addi X2, x3, 0x1
addi X2, X2, 0x1
_equal:
sw   x2, 0x4(x0)