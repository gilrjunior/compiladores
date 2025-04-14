package iftm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sintatico {

    private Lexico lexico;
    private Token token;

    private HashMap<String, Symbol> symbolTable;
    private String label = "";
    private int numberLabel = 1;
    private int offsetVariables = 0;
    private String OutPutFilename;
    private String OutPutFilepath;
    private BufferedWriter bw;  
    private FileWriter fw;
    private static final int integerSize = 4;
    private List<String> variables = new ArrayList<>();
    private List<String> sectionData = new ArrayList<>();
    private Symbol symbol;
    private String labelElse = "";

    public Sintatico(String filename){
        symbolTable = new HashMap<>();
        lexico = new Lexico(filename);
        token = lexico.next_token();

        OutPutFilename = "queronemver.asm";
        OutPutFilepath = java.nio.file.Paths.get(OutPutFilename).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(OutPutFilepath, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }


    }

    public void analyze(){
        program();
    }

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    private void program(){

        if(isReservedWord("program")){
            token = lexico.next_token();
            if(token.getTokenClass() == TokenClass.Identifier){
                //{A01}
                Symbol symbol = new Symbol();
                symbol.setCategory(SymbolCategory.MAINPROGRAM);
                symbol.setLexema(token.getTokenValue().getText());
                symbolTable.put(token.getTokenValue().getText(), symbol);
                offsetVariables = 0;
                CodeWriter("global main");
                CodeWriter("extern printf");
                CodeWriter("extern scanf\n");
                CodeWriter("section .text");
                label = "main";
                CodeWriter("\t; Program entry");
                CodeWriter("\tpush ebp");
                CodeWriter("\tmov ebp, esp");
                token = lexico.next_token();
                if(token.getTokenClass() == TokenClass.Semicolon){
                    token = lexico.next_token();
                    body();
                    if(token.getTokenClass() == TokenClass.Dot){
                        token = lexico.next_token();
                        //{A45}
                        CodeWriter("\tleave");
                        CodeWriter("\tret");
                        if (!sectionData.isEmpty()){
                            CodeWriter("\nsection .data\n");
                            for (String message : sectionData){
                                CodeWriter(message);
                            }
                        }

                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar o arquivo de saída");
                        }

                    }else{
                        error("Faltou ponto final (.) no fim do programa");
                    }
                }else{
                    error("Faltou ponto e virgula (;) depois do nome do programa");
                }
            }else{
                error("Faltou o nome do programa");
            }
        }else{
            error("O programa não se inicia com PROGRAM");
        }

    }

    private void body(){
        declaration();

        if(isReservedWord("begin")){
            token = lexico.next_token();
            sentences();
            if (isReservedWord("end")) {
                token = lexico.next_token();
            }else{
                error("Faltou END no final do corpo");
            }
        }else{
            error("Faltou BEGIN no início do corpo");
        }

    }

    private void declaration(){
        if(isReservedWord("var")){
            token = lexico.next_token();
            dvar();
            m_dc();
        }

    }

    private void sentences(){
        command();
        more_sentences();
    }

    private void command(){
        //   read ( <var_read> ) |
        if(isReservedWord("read")){
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                read_var();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                } else { 
                    error("Faltou fechar parenteses ')' no READ ");
                }
            } else { 
                error("Faltou abrir parenteses '(' no READ ");
            }
        //   write ( <exp_write> ) |
        } else if (isReservedWord("write")) {
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                write_expression();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                } else { 
                    error("Faltou fechar parenteses ')' no WRITE ");
                }
            } else { 
                error("Faltou abrir parenteses '(' no WRITE ");
            }
        //   writeln ( <exp_write> ) {A61} |
        } else if (isReservedWord("writeln")) {
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                write_expression();
                // {A61}

                String newLine = "rotuloStringLN: db '', 10,0";
                
                if(!sectionData.contains(newLine)){
                    sectionData.add(newLine);
                }
                CodeWriter("\tpush rotuloStringLN");
                CodeWriter("\tcall printf");
                CodeWriter("\tadd esp, 4");


                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                } else { 
                    error("Faltou fechar parenteses ')' no WRITELN ");
                }
            } else { 
                error("Faltou abrir parenteses '(' no WRITELN ");
            }
        //   for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} |
        } else if (isReservedWord("for")) {
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.Identifier){
                // {A57}
                String variable = token.getTokenValue().getText();

                if(!symbolTable.containsKey(variable)){
                    error("Variável " + variable + " não declarada no FOR");
                    System.exit(-1);
                } else {
                    symbol = symbolTable.get(variable);
                    if(symbol.getCategory() != SymbolCategory.VARIABLE){
                        error("O identificador " + variable + " não é uma variável");
                        System.exit(-1);
                    }
                }

                token = lexico.next_token();
                if (token.getTokenClass() == TokenClass.Assignment){
                    token = lexico.next_token();
                    expression();
                    // { A11 }
                    CodeWriter("\tpop dword [ebp-" + symbol.getAddress() + "]");
                    String labelInput = LabelCreator("FOR");
                    String labelOutput = LabelCreator("FIMFOR");
                    label = labelInput;
                    if (isReservedWord("to")){
                        token = lexico.next_token();
                        expression();
                        //{ A12 }
                        CodeWriter("\tpush ecx\n"
                                + "\tmov ecx, dword[ebp - " + symbol.getAddress() + "]\n"
                                + "\tcmp ecx, dword[esp+4]\n"
                                + "\tjg " + labelOutput + "\n"
                                + "\tpop ecx");
                        if (isReservedWord("do")){
                            token = lexico.next_token();
                            if (isReservedWord("begin")){
                                token = lexico.next_token();
                                sentences();
                                if (isReservedWord("end")){
                                    token = lexico.next_token();
                                    //{A13}
                                    CodeWriter("\tadd dword[ebp-" + symbol.getAddress() + "], 1");
                                    CodeWriter("\tjmp " + labelInput);
                                    label = labelOutput;
                                } else {
                                    error("Faltou END no final do FOR");
                                }
                            } else {
                                error("Faltou BEGIN no FOR");
                            }
                        } else {
                            error("Faltou Do no FOR");
                        }
                    } else {
                        error("Faltou TO no FOR");
                    }
                } else {
                    error("Faltou Atribuição := no FOR");
                }
            } else {
                error("Faltou Identificador no FOR");
            }
        //    repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
        } else if (isReservedWord("repeat")) {
            token = lexico.next_token();
            // { A14 }
            String labelRepeat = LabelCreator("REPEAT");
            label = labelRepeat;
            sentences();
            if (isReservedWord("until")){
                token = lexico.next_token();
                if (token.getTokenClass() == TokenClass.LeftParenthesis){
                    token = lexico.next_token();
                    logical_expression();
                    if (token.getTokenClass() == TokenClass.RightParenthesis){
                        token = lexico.next_token();
                        // {A15}
                        CodeWriter("\tcmp dword[esp], 0");
                        CodeWriter("\tje " + labelRepeat);
                    } else { 
                        error("Faltou fechar parenteses ')' no REPEAT ");
                    }
                } else { 
                    error("Faltou abrir parenteses '(' no REPEAT ");
                }
            } else {
                error("Faltou o UNTIL no repeat");
            }
        //    while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
        } else if (isReservedWord("while")){
            token = lexico.next_token();
            // { A16 }
            String labelWhile = LabelCreator("WHILE");
            String labelEndWhile = LabelCreator("FIMWHILE");
            label = labelWhile;
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                logical_expression();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                    // {A17}
                    CodeWriter("\tcmp dword[esp], 0");
                    CodeWriter("\tje " + labelEndWhile);
                    if (isReservedWord("do")){
                        token = lexico.next_token();
                        if ( isReservedWord("begin")){
                            token = lexico.next_token();
                            sentences();
                            if (isReservedWord("end")) {
                                token = lexico.next_token();
                                // { A18 }
                                CodeWriter("\tjmp " + labelWhile);
                                label = labelEndWhile;
                            } else {
                                error("Faltou END no WHILE");
                            }
                        } else { 
                            error("Faltou BEGIN no WHILE");
                        }
                    } else {
                        error("Faltou DO no WHILE");
                    }
                } else { 
                    error("Faltou fechar parenteses ')' no WHILE ");
                }
            } else { 
                error("Faltou abrir parenteses '(' no WHILE ");
            }
        //   if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21} |
        } else if (isReservedWord("if")){
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                logical_expression();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                    // {A19}
                    labelElse = LabelCreator("ELSE");
                    String labelEndIf = LabelCreator("FIMIF");
                    CodeWriter("\tcmp dword[esp], 0\n");
                    CodeWriter("\tje " + labelElse);
                    if ( isReservedWord("then")){
                        token = lexico.next_token();
                        if (isReservedWord("begin")){
                            token = lexico.next_token();
                            sentences();
                            if(isReservedWord("end")){
                                token = lexico.next_token();
                                //{A20}
                                CodeWriter("\tjmp " + labelEndIf);
                                falsep();
                                //{A21}
                                label = labelEndIf;
                            } else {
                                error("Faltou END no THEN do IF");
                            }
                        } else {
                            error("Faltou BEGIN no THEN do IF");
                        }
                    } else {
                        error("Faltou THEN no IF");
                    }
                } else { 
                    error("Faltou fechar parenteses ')' no IF ");
                }
            } else { 
                error("Faltou abrir parenteses '(' no IF ");
            }
        } else if(token.getTokenClass() == TokenClass.Identifier) {
            // {A49}
            String variable = token.getTokenValue().getText();
            if(!symbolTable.containsKey(variable)){
                error("Variável " + variable + " não declarada no IF");
                System.exit(-1);
            } else {
                symbol = symbolTable.get(variable);
                if(symbol.getCategory() != SymbolCategory.VARIABLE){
                    error("O identificador " + variable + " não é uma variável");
                    System.exit(-1);
                }
            }
            token = lexico.next_token();
            if (token.getTokenClass() == TokenClass.Assignment){
                token = lexico.next_token();
                expression();
                //{A22}
                symbol = symbolTable.get(variable);
                CodeWriter("\tpop eax");
                CodeWriter("\tmov dword[ebp-" + symbol.getAddress() + "], eax");
            } else {
                error("Faltou a atribuição ':=' em uma atribuição");
            }
        } else {
            error("Faltou um COMANDO");
        }
    }

    //<pfalsa> ::= {A25} else begin <sentencas> end | ε
    private void falsep(){
        //{A25}
        CodeWriter(labelElse + ":");
        if (isReservedWord("else")){
            token = lexico.next_token();
            if (isReservedWord("begin")){
                token = lexico.next_token();
                sentences();
                if (isReservedWord("end")){
                    token = lexico.next_token();
                } else {
                    error("Faltou END no ELSE");
                }
            } else {
                error("Faltou BEGIN no ELSE");
            }
        }
    }

    //<expressao> ::= <termo> <mais_expressao>
    private void expression(){
        term();
        more_expression();
    }

    // <termo> ::= <fator> <mais_termo>
    private void term(){
        factor();
        more_term();
    }

    //<fator> ::= id {A55} | intnum {A41} | ( <expressao> ) 
    private void factor(){
        if (token.getTokenClass() ==  TokenClass.Identifier){
            // {A55}
            String variable = token.getTokenValue().getText();
            if(!symbolTable.containsKey(variable)){
                error("Variável " + variable + " não declarada no FATOR");
                System.exit(-1);
            } else {
                symbol = symbolTable.get(variable);
                if(symbol.getCategory() != SymbolCategory.VARIABLE){
                    error("O identificador " + variable + " não é uma variável");
                    System.exit(-1);
                }
            }
            CodeWriter("\tpush dword[ebp-" + symbol.getAddress() + "]");
            token = lexico.next_token();
        } else if (token.getTokenClass() ==  TokenClass.IntegerNumber){
            // {A41}
            CodeWriter("\tpush " + token.getTokenValue().getInteger());
            token = lexico.next_token();
        } else if (token.getTokenClass() ==  TokenClass.LeftParenthesis) {
            token = lexico.next_token();
            expression();
            if  (token.getTokenClass() ==  TokenClass.RightParenthesis) {
                token = lexico.next_token();
            } else {
                error("Faltou fechar parênteses ')' no FATOR" );
            }
        } else {
            error("Faltou identificador ou faltou fechar parênteses '(' no FATOR" );
        }
    }

    //<mais_termo> ::= * <fator> {A39} <mais_termo>  | / <fator> {A40} <mais_termo>  | ε
    private void more_term(){
        if (token.getTokenClass() == TokenClass.Multiplication) {
            token = lexico.next_token();
            factor();
            //{A39}
            CodeWriter("\tpop eax");
            CodeWriter("\timul eax, dword[esp]");
            CodeWriter("\tmov dword[esp], eax");
            more_term();
        } else if (token.getTokenClass() == TokenClass.Division) {
            token = lexico.next_token();
            factor();
            //{A40}
            CodeWriter("\tpop ecx");
            CodeWriter("\tpop eax");
            CodeWriter("\tidiv ecx");
            CodeWriter("\tpush eax");
            more_term();
        }
    }

    // <mais_expressao> ::= + <termo> {A37} <mais_expressao>  | - <termo> {A38} <mais_expressao>  | ε
    private void more_expression(){
        if(token.getTokenClass() ==  TokenClass.Addition){
            token = lexico.next_token();
            term();
            //{A37}
            CodeWriter("\tpop eax");
            CodeWriter("\tadd dword[esp], eax");
            more_expression();
        }else if (token.getTokenClass() ==  TokenClass.Subtraction){
            token = lexico.next_token();
            term();
            //{A38}
            CodeWriter("\tpop eax");
            CodeWriter("\tsub dword[esp], eax");
            more_expression();
        }
    }

    //<expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void logical_expression(){
        logical_term();
        more_logical_expression();
    }
    //<mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica>  | ε
    private void more_logical_expression(){
        if (isReservedWord("or")){
            token = lexico.next_token();
            logical_term();
            //{A26}
            String labelOutput = LabelCreator("SaidaMEL");
            String labelTruth = LabelCreator("VerdadeMEL");
            CodeWriter("\tcmp dword [esp + 4], 1");
            CodeWriter("\tje " + labelTruth);
            CodeWriter("\tcmp dword [esp], 1");
            CodeWriter("\tje " + labelTruth);
            CodeWriter("\tmov dword [esp + 4], 0");
            CodeWriter("\tjmp " + labelOutput);
            label = labelTruth;
            CodeWriter("\tmov dword [esp + 4], 1");
            label = labelOutput;
            CodeWriter("\tadd esp, 4");
            more_logical_expression();
        }
    }

    //<termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void logical_term(){
        logical_factor();
        more_logical_factor();
    }

    //<mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico>  | ε
    private void more_logical_factor(){
        if (isReservedWord("and")){
            token = lexico.next_token();
            logical_factor();
            //{A27}
            String labelOutput = LabelCreator("SaidaMTL");
            String labelFalse = LabelCreator("FalsoMTL");
            CodeWriter("\tcmp dword [esp + 4], 1");
            CodeWriter("\tjne " + labelOutput);
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjne " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
            more_logical_factor();
        }
    }

    //<fator_logico> ::= <relacional> |
    //               ( <expressao_logica> ) |
    //               not <fator_logico> {A28} |
    //               true {A29} |
    //               false {A30}
    private void logical_factor(){
        if (token.getTokenClass() == TokenClass.LeftParenthesis) {
            token = lexico.next_token();
            logical_expression();
            if (token.getTokenClass() == TokenClass.RightParenthesis){
                token = lexico.next_token();
            } else {
                error("Faltou fechar parentesis 'f' no fator lógico");
            }
        } else if (isReservedWord("not")) {
            token = lexico.next_token();
            logical_factor();
            // {A28}
            String labelFalse = LabelCreator("FalsoFL");
            String labelOutput = LabelCreator("SaidaFL");
            CodeWriter("\tcmp dword [esp], 1");
            CodeWriter("\tjne " + labelFalse);
            CodeWriter("\tmov dword [esp], 0");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 1");
            label = labelOutput;
        } else if (isReservedWord("true")) {
            token = lexico.next_token();
            // {A29}
            CodeWriter("\tpush 1"); 
        }  else if (isReservedWord("false")) {
            token = lexico.next_token();
            // {A30}
            CodeWriter("\tpush 0");
        } else {
            relational();
        }
    }

    //<relacional> ::= <expressao> =  <expressao> {A31} |
    //             <expressao> >  <expressao> {A32} |
    //             <expressao> >= <expressao> {A33} |
    //             <expressao> <  <expressao> {A34} |
    //             <expressao> <= <expressao> {A35} |
    //             <expressao> <> <expressao> {A36}
    private void relational(){
        expression();
        if (token.getTokenClass() == TokenClass.Equal){
            token = lexico.next_token();
            expression();
            //{A31}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjne " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else if (token.getTokenClass() == TokenClass.LessThan){
            token = lexico.next_token();
            expression();
            //{A32}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjle " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else if (token.getTokenClass() == TokenClass.LessThanOrEqual){
            token = lexico.next_token();
            expression();
            //{A33}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjg " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else if (token.getTokenClass() == TokenClass.GreaterThan){
            token = lexico.next_token();
            expression();
            //{A34}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjge " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else if (token.getTokenClass() == TokenClass.GreaterThanOrEqual){
            token = lexico.next_token();
            expression();
            //{A35}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tjg " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else if (token.getTokenClass() == TokenClass.NotEqual){
            token = lexico.next_token();
            expression();
            //{A36}
            String labelFalse = LabelCreator("FalsoREL");
            String labelOutput = LabelCreator("SaidaREL");
            CodeWriter("\tpop eax");
            CodeWriter("\tcmp dword [esp], eax");
            CodeWriter("\tje " + labelFalse);
            CodeWriter("\tmov dword [esp], 1");
            CodeWriter("\tjmp " + labelOutput);
            label = labelFalse;
            CodeWriter("\tmov dword [esp], 0");
            label = labelOutput;
        } else {
            error("Faltou um operador relacional");
        }
    }

    //<mais_sentencas> ::= ; <cont_sentencas>
    private void more_sentences(){
        if(token.getTokenClass() == TokenClass.Semicolon){
            token = lexico.next_token();
            c_sentences();
        }else{
            error("Faltou ponto e vírgula (;) no final de um comando");
        }
    }

    // <cont_sentencas> ::= <sentencas> | e
    private void c_sentences(){
        if(isReservedWord("read") || isReservedWord("write")  || isReservedWord("writeln") ||
           isReservedWord("for")  || isReservedWord("repeat") || isReservedWord("while") ||
           isReservedWord("if")   || token.getTokenClass() == TokenClass.Identifier)
           {
                sentences();
           }
    }

    //<var_read> ::= id {A08} <mais_var_read>
    private void read_var(){
        if(token.getTokenClass() == TokenClass.Identifier){
            // {A08}
            String variable = token.getTokenValue().getText();
            if (!symbolTable.containsKey(variable)) {
                error("Variável " + variable + " não foi declarada");
                System.exit(-1);
            } else {
                Symbol symbol = symbolTable.get(variable);
                if (symbol.getCategory() != SymbolCategory.VARIABLE) {
                    error("Identificador " + variable + " não é uma variável");
                    System.exit(-1);
                } else {
                    CodeWriter("\tmov edx, ebp");
                    CodeWriter("\tlea eax, [edx - " + symbol.getAddress() + "]");
                    CodeWriter("\tpush eax");
                    CodeWriter("\tpush @Integer");
                    CodeWriter("\tcall scanf");
                    CodeWriter("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.next_token();
            more_read_var();
        }else{
            error("Faltou o identificador da variável a ser lida");
        }
    }

    //<mais_var_read> ::= , <var_read> | e
    private void more_read_var(){
        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            read_var();
        }
    }

    private void write_expression(){
        if(token.getTokenClass() == TokenClass.Identifier){
            // {A09}
            String variable = token.getTokenValue().getText();
            if (!symbolTable.containsKey(variable)) {
                error("Variável " + variable + " não foi declarada");
                System.exit(-1);
            } else {
                    Symbol symbol = symbolTable.get(variable);
                if (symbol.getCategory() != SymbolCategory.VARIABLE) {
                    error("Identificador " + variable + " não é uma variável");
                    System.exit(-1);
                } else {
                    CodeWriter("\tpush dword[ebp - " + symbol.getAddress() + "]");
                    CodeWriter("\tpush @Integer");
                    CodeWriter("\tcall printf");
                    CodeWriter("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }
            token = lexico.next_token();
            more_write_expression();
        }else if(token.getTokenClass() == TokenClass.String){
            // {A59}
            String string = token.getTokenValue().getText();
            String label = LabelCreator("String");
            sectionData.add(label + ": db '" + string + " ',0");
            CodeWriter("\tpush " + label);
            CodeWriter("\tcall printf");
            CodeWriter("\tadd esp, 4");
            token = lexico.next_token();
            more_write_expression();
        }else if(token.getTokenClass() == TokenClass.IntegerNumber){
            // {A43}
            CodeWriter("\tpush " + token.getTokenValue().getInteger());
            CodeWriter("\tpush @Integer");
            CodeWriter("\tcall printf");
            CodeWriter("\tadd esp, 8");
            if (!sectionData.contains("@Integer: db '%d',0")) {
                sectionData.add("@Integer: db '%d',0");
            }
            token = lexico.next_token();
            more_write_expression();
        }else{
            error("Era esperado um identificador ou uma string ou um número inteiro no WRITE/WRITELN");
        }
        
    }

    private void more_write_expression(){
        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            write_expression();
        }
    }

    private void dvar(){
        variables();
        if(token.getTokenClass() == TokenClass.Colon){
            token = lexico.next_token();
            variable_type();
            // {A02}
            int size = 0;
            for (String var : variables) {
                symbolTable.get(var).setType(SymbolType.INTEGER);
                size += integerSize;
            }
            CodeWriter("\tsub esp, " + size);
            variables.clear();
        }else{
            error("Faltou o dois pontos (:) na declaração de variáveis");
        }
    }

    // <variaveis> ::= id {A03} <mais_var>
    private void variables(){
        if(token.getTokenClass() == TokenClass.Identifier){
            // {A03}
            String variable = token.getTokenValue().getText();
            if (symbolTable.containsKey(variable)) {
                System.err.println("Variável " + variable + " já foi declarada anteriormente");
                System.exit(-1);
            } else {
                Symbol symbol = new Symbol();
                symbol.setCategory(SymbolCategory.VARIABLE);
                symbol.setLexema(variable);
                symbol.setAddress(offsetVariables);
                symbolTable.put(variable, symbol);
                offsetVariables += integerSize;
                variables.add(variable);
            }  
            token = lexico.next_token();
            more_variables();
        }else{
            error("Faltou o identificador de uma variável");
        }
    }

    // <mais_var> ::= , <variaveis> | e
    private void more_variables(){

        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            variables();
        }

    }

    //<tipo_var> ::= integer
    private void variable_type(){
        if(isReservedWord("integer")){
            token = lexico.next_token();
        }else{
            error("Faltou o tipo (integer) na declaração de variáveis");
        }
    }


    // <mais_dc> ::= ; <cont_dc>
    private void m_dc() {

        if (token.getTokenClass() == TokenClass.Semicolon){
                token = lexico.next_token();
                c_dc();
        }else{
            error("Faltou ponto e vírgula (;) na declaração de veriáveis");
        }
    }

    //<cont_dc> ::= <dvar><mais_dc> | e
    private void c_dc(){
        if(token.getTokenClass() == TokenClass.Identifier){
            dvar();
            m_dc();    
        }
    }


    private boolean isReservedWord(String text){
        return token.getTokenClass() == TokenClass.ReservedWord && token.getTokenValue().getText().equalsIgnoreCase(text);
    }

    private void error(String message){
        System.out.println(token.getLine() + ", " + token.getColumn() + " - Erro sintático: "+message);
    }

    private void CodeWriter(String instruction) {
        try {
            if (label.isEmpty()) {
                bw.write(instruction + "\n");
            } else {
                bw.write(label + ": " + instruction + "\n");
                label = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }
    
    private String LabelCreator(String text) {
        String returnText = "rotulo" + text + numberLabel;
        numberLabel++;
        return returnText;
    }
    
}

