NAME = all_uart

s_to_hex:
	riscv32-unknown-elf-as -o $(NAME).elf -c $(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(NAME).elf --output $(NAME).hex
	python3 hex_vecinit.py $(NAME).hex $(NAME)_hex.txt $(NAME)_bin.txt

