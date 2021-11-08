addi x1, x0, 0x0 
_loop_start:
    addi x1, x1, 0x1
    sw   x1, 0x400(x0)
    jal  x0, _loop_start
