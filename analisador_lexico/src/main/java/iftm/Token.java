package iftm;

public class Token {
    
    private TokenClass tokenClass;
    private TokenValue tokenValue;
    private int line;
    private int column;

    public Token(int line, int column){
        this.line = line;
        this.column = column;
    }

    public Token(int line, int column, TokenClass tokenClass){
        this.line = line;
        this.column = column;
        this.tokenClass = tokenClass;
    }

    public TokenClass getTokenClass() {
        return tokenClass;
    }
    public void setTokenClass(TokenClass token_class) {
        this.tokenClass = token_class;
    }
    public TokenValue getTokenValue() {
        return tokenValue;
    }
    public void setTokenValue(TokenValue token_value) {
        this.tokenValue = token_value;
    }
    public int getLine() {
        return line;
    }
    public void setLine(int line) {
        this.line = line;
    }
    public int getColumn() {
        return column;
    }
    public void setColumn(int column) {
        this.column = column;
    }
    @Override
    public String toString() {

        String value = "";

        if (tokenClass == TokenClass.Identifier ||tokenClass == TokenClass.ReservedWord || tokenClass == TokenClass.String){
            value = tokenValue.getText();
        }else if (tokenClass == TokenClass.IntegerNumber){
            value = "" + tokenValue.getInteger();
        }else if (tokenClass == TokenClass.FloatingPointNumber){
            value = "" + tokenValue.getFloatingPoint();
        }

        return "Token [token_class=" + tokenClass + ", line=" + line + ", column="
                + column + "], token_value=" + value ;
    }

}
