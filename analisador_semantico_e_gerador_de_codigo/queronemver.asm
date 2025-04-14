global main
extern printf
extern scanf

section .text
main: 	; Program entry
	push ebp
	mov ebp, esp
	sub esp, 20
	push 1
	pop eax
	mov dword[ebp-0], eax
	push 1
	pop eax
	mov dword[ebp-4], eax
	push 0
	pop eax
	mov dword[ebp-16], eax
	push 0
	pop eax
	mov dword[ebp-8], eax
	mov edx, ebp
	lea eax, [edx - 16]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push dword[ebp - 0]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push dword[ebp - 4]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push 2
	pop eax
	mov dword[ebp-12], eax
rotuloWHILE1: 	push dword[ebp-12]
	push dword[ebp-16]
	pop eax
	cmp dword [esp], eax
	jg rotuloFalsoREL3
	mov dword [esp], 1
	jmp rotuloSaidaREL4
rotuloFalsoREL3: 	mov dword [esp], 0
rotuloSaidaREL4: 	cmp dword[esp], 0
	je rotuloFIMWHILE2
	push dword[ebp-0]
	push dword[ebp-4]
	pop eax
	add dword[esp], eax
	pop eax
	mov dword[ebp-8], eax
	push dword[ebp - 8]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push dword[ebp-4]
	pop eax
	mov dword[ebp-0], eax
	push dword[ebp-8]
	pop eax
	mov dword[ebp-4], eax
	push dword[ebp-12]
	push 1
	pop eax
	add dword[esp], eax
	pop eax
	mov dword[ebp-12], eax
	jmp rotuloWHILE1
rotuloFIMWHILE2: 	leave
	ret

section .data

@Integer: db '%d',0
rotuloStringLN: db '', 10,0
