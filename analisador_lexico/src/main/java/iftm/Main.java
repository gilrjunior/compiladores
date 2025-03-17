package iftm;

public class Main {
    public static void main(String[] args) {
        
        Lexico lexico = new Lexico("teste.pas");

        Token token;

        do {
        
            token = lexico.next_token();
            System.out.println(token);

        }   while (token.getTokenClass() != TokenClass.EOF);

    }
}