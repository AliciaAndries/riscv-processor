NAME = all_uart
RECOURCES = /home/alicia/Documents/thesis/riscv-processor/src/test/resources

s_to_hex:
	riscv32-unknown-elf-as -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CoreFPGAOutInitMem"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

