package iftm;

// Importa classes necessárias para leitura de arquivos e manipulação de caminhos
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Classe Lexico: responsável pela análise léxica de um arquivo, convertendo a sequência de caracteres em tokens.
public class Lexico {
    
    // Armazena o caractere atual que está sendo processado
	private char caractere;
    // BufferedReader utilizado para ler o arquivo
    private BufferedReader br;
    // Variáveis para controle da posição (linha e coluna) para possíveis mensagens de erro ou rastreamento
    private int line = 1;
    private int column = 0;
    private List<String> reservedWords = new ArrayList<>();

    // Construtor que recebe o nome do arquivo a ser analisado
    public Lexico(String filename){
        // Obtém o caminho absoluto do arquivo
		String file_path = Paths.get(filename).toAbsolutePath().toString();

        try{
            // Inicializa o BufferedReader para ler o arquivo usando codificação UTF-8
            br = new BufferedReader(new FileReader(file_path, StandardCharsets.UTF_8));
            // Lê o primeiro caractere para iniciar a análise
            caractere = next_caractere();

            reservedWords.addAll(Arrays.asList("program", "begin", "end", "var", "integer", "procedure", "function",
                                                "read", "write", "writeln", "for", "do", "repeat", "until", "while",
                                                "if", "then", "else", "or", "and", "not", "true", "false", "to"));

        } catch (Exception e) {
            // Em caso de erro (por exemplo, arquivo não encontrado ou problema de leitura), exibe uma mensagem de erro e o stack trace
            System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + filename);
			e.printStackTrace();
        }
    }

    // Método que retorna o próximo token encontrado na leitura do arquivo
    public Token next_token(){
        String lexema; // Armazena o lexema (sequência de caracteres) do token atual
        Token token;   // Variável para o token a ser retornado

        // Loop que percorre os caracteres enquanto não atingir o final da stream
        // (a conversão de -1 para char resulta em 65535)
        while ((int) caractere != 65535) { 
            lexema = "";
            
            // Verifica se o caractere atual é uma letra para identificar um identificador (nome, variável, etc.)
            if (Character.isLetter(caractere)) {
                token = new Token(line, column, TokenClass.Identifier);
                // Continua lendo letras ou dígitos para formar o identificador completo
                while(Character.isLetter(caractere) || Character.isDigit(caractere)){
                    lexema += caractere;
                    caractere = next_caractere();
                }
                // Atribui o lexema lido ao token
                token.setTokenValue(new TokenValue(lexema));
                if (reservedWords.contains(lexema.toLowerCase())){
                    token.setTokenClass(TokenClass.ReservedWord);
                }
                return token;
            } 
            // Se o caractere atual for um dígito, inicia a leitura de um número
            else if (Character.isDigit(caractere)) {
                token = new Token(line, column, TokenClass.IntegerNumber);
                // Lê todos os dígitos consecutivos para formar o número inteiro
                while(Character.isDigit(caractere)){
                    lexema += caractere;
                    caractere = next_caractere();
                }

                // Verifica se o número possui parte decimal (indicada pelo ponto '.')
                if(caractere == '.'){
                    lexema += caractere;
                    caractere = next_caractere();
                    // Altera a classe do token para representar um número de ponto flutuante
                    token.setTokenClass(TokenClass.FloatingPointNumber);
                    if(Character.isDigit(caractere)){
                        // Lê os dígitos da parte decimal
                        while(Character.isDigit(caractere)){
                            lexema += caractere;
                            caractere = next_caractere();
                        }
                        // Converte o lexema para um valor do tipo Double e atribui ao token
                        token.setTokenValue(new TokenValue(Double.parseDouble(lexema)));
                    } else {
                        // Se não houver dígitos após o ponto, emite um erro léxico e encerra a execução
                        System.out.println(line + ", " + column + " - Erro léxico: O número está sem a parte decimal");
                        System.exit(-1);
                    }
                } else {
                    // Se não há parte decimal, converte o lexema para inteiro e atribui ao token
                    token.setTokenValue(new TokenValue(Integer.parseInt(lexema)));
                }
                return token;
            } 
            // Se o caractere for um espaço em branco, ignora-o e lê o próximo caractere
            else if (Character.isWhitespace(caractere)) {
                caractere = next_caractere();
            } 
            // Processa o operador de adição '+'
            else if (caractere == '+'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Addition);
                return token;
            } 
            // Processa o caractere ':' que pode representar dois tokens: ':' ou ':=' (atribuição)
            else if (caractere == ':'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Colon);
                if(caractere == '='){
                    lexema += caractere;
                    caractere = next_caractere();
                    // Altera o token para o operador de atribuição
                    token.setTokenClass(TokenClass.Assignment);
                }
                return token;
            } 
            // Processa o operador de subtração '-'
            else if (caractere == '-'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Subtraction);
                return token;
            } 
            // Processa o operador de multiplicação '*'
            else if (caractere == '*'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Multiplication);
                return token;
            } 
            // Processa o operador de divisão '/'
            else if (caractere == '/'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Division);

                if(caractere == '/'){  // Processa o comentário
                    while (caractere != '\n' || (int)caractere != 65635) {
                        caractere = next_caractere();
                    }
                }else{
                    return token;
                }
            } 
            // Processa o operador '>' e verifica se há o caractere '=' formando ">="
            else if (caractere == '>'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.GreaterThan);
                if(caractere == '='){
                    lexema += caractere;
                    caractere = next_caractere();
                    token.setTokenClass(TokenClass.GreaterThanOrEqual);
                }
                return token;
            } 
            // Processa o operador '<' e verifica as possibilidades: "<=", ou "<>" (não igual)
            else if (caractere == '<'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.LessThan);
                if(caractere == '='){
                    lexema += caractere;
                    caractere = next_caractere();
                    token.setTokenClass(TokenClass.LessThanOrEqual);
                } else if(caractere == '>'){
                    lexema += caractere;
                    caractere = next_caractere();
                    token.setTokenClass(TokenClass.NotEqual);
                }
                return token;
            } 
            // Processa o ponto '.'
            else if (caractere == '.'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Dot);
                return token;
            } 
            // Processa o operador de igualdade '='
            else if (caractere == '='){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Equal);
                return token;
            // Processa o comentário de bloco
            }else if (caractere == '{'){
                while (caractere != '}' && (int)caractere != 65535) {
                    caractere = next_caractere();
                }
                if(caractere == '}'){
                    caractere = next_caractere();
                }
            }else if(caractere == '\''){    
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.String);
                while (caractere != '\'' && caractere != '\n' && (int)caractere != 65535){
                    lexema += caractere;
                    caractere = next_caractere();
                }
                
                if(caractere == '\''){
                    caractere = next_caractere();
                    token.setTokenValue(new TokenValue(lexema));
                    return token;
                } else if (caractere == '\n') {
                    System.out.println(line + ", " + column + " - Erro léxico: String não foi terminada");
                    System.exit(-1);
                }else if ((int)caractere == 65535){
                    System.out.println(line + ", " + column + " - Erro léxico: String não foi terminada");
                    System.exit(-1);
                }

            }
            // Processa a vírgula ','
            else if (caractere == ','){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Comma);
                return token;
            } 
            // Processa o ponto e vírgula ';'
            else if (caractere == ';'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.Semicolon);
                return token;
            } 
            // Processa o parêntese esquerdo '('
            else if (caractere == '('){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.LeftParenthesis);
                return token;
            } 
            // Processa o parêntese direito ')'
            else if (caractere == ')'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.RightParenthesis);
                return token;
            } 
            // Processa o colchete esquerdo '['
            else if (caractere == '['){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.LeftBracket);
                return token;
            } 
            // Processa o colchete direito ']'
            else if (caractere == ']'){
                lexema += caractere;
                caractere = next_caractere();
                token = new Token(line, column, TokenClass.RightBracket);
                return token;
            } 
            // Se o caractere não corresponde a nenhum dos casos reconhecidos, emite uma mensagem de erro e encerra a execução
            else {
                System.out.println(line + ", " + column + " - Erro léxico: Caractere Inválido " + (int) caractere);
                System.exit(-1);
            }
        }

        // Se nenhum caractere válido for encontrado (fim do arquivo), retorna um token especial EOF (End Of File)
        token = new Token(line, column, TokenClass.EOF);
        return token;
    }

    // Método que lê o próximo caractere do arquivo
    private char next_caractere(){
        try{
            // Lê o próximo caractere a partir do BufferedReader
            char c = (char) br.read();

            // Identifica linha e coluna do valor lido
            if (c == '\n'){
                line++;
                column = 0;
            } else {
                column++;
            }

            return c;
		} catch (IOException e) {
			// Em caso de erro na leitura, exibe mensagem de erro e imprime o stack trace
			System.err.println("Erro ao processar os caracteres");
			e.printStackTrace();
		}
        // Retorna caractere nulo se ocorrer algum erro
        return '\0';
    }

}
