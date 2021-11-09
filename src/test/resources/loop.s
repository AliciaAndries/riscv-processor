nop
addi x1, x0, 0x0 
lui  x2, 0x989
addi x3, x0, 0x0
_loop_start:
    beq  x1, x2, _toggle
    sw   x3, 0x400(x0)
    addi x1, x1, 0x1
    sw   x3, 0x400(x0)
    jal  x0, _loop_start
_toggle:
    addi x3, x3, 0x1
    sw   x3, 0x400(x0)
    addi x1, x0, 0x0
    sw   x3, 0x400(x0)
    jal  x0, _loop_start

