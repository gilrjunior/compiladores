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
            setences();
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

    private void setences(){
        comand();
        m_sentences();
    }

    private void comand(){

    }

    //<mais_sentencas> ::= ; <cont_sentencas>
    private void m_sentences(){
        if(token.getTokenClass() == TokenClass.Semicolon){
            token = lexico.next_token();
            c_sentences();
        }else{
            error("Faltou ponto e vírgula (;) no final de um comando");
        }
    }

    // <cont_sentencas> ::= <sentencas> | e
    private void c_sentences(){
        if(isReservedWord("read") || isReservedWord("wirte")  || isReservedWord("writeln") ||
           isReservedWord("for")  || isReservedWord("repeat") || isReservedWord("while") ||
           isReservedWord("if")   || token.getTokenClass() == TokenClass.Identifier)
           {
                setences();
           }
    }

    //<var_read> ::= id {A08} <mais_var_read>
    private void read_var(){
        if(token.getTokenClass() == TokenClass.Identifier){
            token = lexico.next_token();
            m_read_var();
        }else{
            error("Faltou o identificador da variável a ser lida");
        }
    }

    //<mais_var_read> ::= , <var_read> | e
    private void m_read_var(){
        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            read_var();
        }
    }

    private void write_expression(){
        if(token.getTokenClass() == TokenClass.Identifier){
            token = lexico.next_token();
            // {A09}
            m_write_expression();
        }else if(token.getTokenClass() == TokenClass.String){
            token = lexico.next_token();
            // {A43}
            m_write_expression();
        }else if(token.getTokenClass() == TokenClass.IntegerNumber){
            token = lexico.next_token();
            // {A43}
            m_write_expression();
        }else{
            error("Era esperado um identificador ou uma string ou um número inteiro no WRITE/WRITELN");
        }
        
    }

    private void m_write_expression(){
        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            write_expression();
        }
    }

    private void dvar(){
        variables();
        if(token.getTokenClass() == TokenClass.Colon){
            token = lexico.next_token();
            var_type();
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
            m_var();
        }else{
            error("Faltou o identificador de uma variável");
        }
    }

    // <mais_var> ::= , <variaveis> | e
    private void m_var(){

        if(token.getTokenClass() == TokenClass.Comma){
            token = lexico.next_token();
            variables();
        }

    }

    //<tipo_var> ::= integer
    private void var_type(){
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
