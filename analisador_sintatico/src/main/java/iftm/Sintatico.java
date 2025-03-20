package iftm;

public class Sintatico {

    private Lexico lexico;
    private Token token;

    public Sintatico(String filename){
        lexico = new Lexico(filename);
        token = lexico.next_token();
    }

    public void analyze(){
        program();
    }

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    private void program(){

        if(isReservedWord("program")){
            token = lexico.next_token();
            if(token.getTokenClass() == TokenClass.Identifier){
                token = lexico.next_token();
                //{A01}
                if(token.getTokenClass() == TokenClass.Semicolon){
                    token = lexico.next_token();
                    body();
                    if(token.getTokenClass() == TokenClass.Dot){
                        token = lexico.next_token();
                        //{A45}
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
                token = lexico.next_token();
                // {A57}
                if (token.getTokenClass() == TokenClass.Assignment){
                    token = lexico.next_token();
                    expression();
                    // { A11 }
                    if (isReservedWord("to")){
                        token = lexico.next_token();
                        expression();
                        //{ A12 }
                        if (isReservedWord("do")){
                            token = lexico.next_token();
                            if (isReservedWord("begin")){
                                token = lexico.next_token();
                                sentences();
                                if (isReservedWord("end")){
                                    token = lexico.next_token();
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
            sentences();
            if (isReservedWord("until")){
                token = lexico.next_token();
                if (token.getTokenClass() == TokenClass.LeftParenthesis){
                    token = lexico.next_token();
                    logical_expression();
                    if (token.getTokenClass() == TokenClass.RightParenthesis){
                        token = lexico.next_token();
                        // {A15}
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
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                logical_expression();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                    // {A17}
                    if (isReservedWord("do")){
                        token = lexico.next_token();
                        if ( isReservedWord("begin")){
                            token = lexico.next_token();
                            sentences();
                            if (isReservedWord("end")) {
                                token = lexico.next_token();
                                // { A18 }
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
            if (token.getTokenClass() == TokenClass.LeftParenthesis){
                token = lexico.next_token();
                logical_expression();
                if (token.getTokenClass() == TokenClass.RightParenthesis){
                    token = lexico.next_token();
                    // {A19}
                    if ( isReservedWord("then")){
                        token = lexico.next_token();
                        if (isReservedWord("begin")){
                            token = lexico.next_token();
                            sentences();
                            if(isReservedWord("end")){
                                token = lexico.next_token();
                                //{A20}
                                falsep();
                                //{A21}
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
            token = lexico.next_token();
            // {Â49}
            if (token.getTokenClass() == TokenClass.Assignment){
                token = lexico.next_token();
                expression();
                //{A22}
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
            token = lexico.next_token();
            // {A55}
        } else if (token.getTokenClass() ==  TokenClass.IntegerNumber){
            token = lexico.next_token();
            // {A55}
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
            more_term();
        } else if (token.getTokenClass() == TokenClass.Division) {
            token = lexico.next_token();
            factor();
            //{A40}
            more_term();
        }
    }

    // <mais_expressao> ::= + <termo> {A37} <mais_expressao>  | - <termo> {A38} <mais_expressao>  | ε
    private void more_expression(){
        if(token.getTokenClass() ==  TokenClass.Addition){
            token = lexico.next_token();
            term();
            //{A37}
            more_expression();
        }else if (token.getTokenClass() ==  TokenClass.Subtraction){
            token = lexico.next_token();
            term();
            //{A38}
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
        } else if (isReservedWord("true")) {
            token = lexico.next_token();
        }  else if (isReservedWord("false")) {
            token = lexico.next_token();
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
        } else if (token.getTokenClass() == TokenClass.LessThan){
            token = lexico.next_token();
            expression();
            //{A32}
        } else if (token.getTokenClass() == TokenClass.LessThanOrEqual){
            token = lexico.next_token();
            expression();
            //{A33}
        } else if (token.getTokenClass() == TokenClass.GreaterThan){
            token = lexico.next_token();
            expression();
            //{A34}
        } else if (token.getTokenClass() == TokenClass.GreaterThanOrEqual){
            token = lexico.next_token();
            expression();
            //{A35}
        } else if (token.getTokenClass() == TokenClass.NotEqual){
            token = lexico.next_token();
            expression();
            //{A36}
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
            token = lexico.next_token();
            // {A09}
            more_write_expression();
        }else if(token.getTokenClass() == TokenClass.String){
            token = lexico.next_token();
            // {A43}
            more_write_expression();
        }else if(token.getTokenClass() == TokenClass.IntegerNumber){
            token = lexico.next_token();
            // {A43}
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
        }else{
            error("Faltou o dois pontos (:) na declaração de variáveis");
        }
    }

    // <variaveis> ::= id {A03} <mais_var>
    private void variables(){
        if(token.getTokenClass() == TokenClass.Identifier){
            token = lexico.next_token();
            // {A03}
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
    
}
