package iftm;

public class Symbol {
    
    private String lexema;
    private SymbolCategory category;
    private SymbolType type;
    private int address;
    
    public String getLexema() {
        return lexema;
    }
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }
    public SymbolCategory getCategory() {
        return category;
    }
    public void setCategory(SymbolCategory category) {
        this.category = category;
    }
    public SymbolType getType() {
        return type;
    }
    public void setType(SymbolType type) {
        this.type = type;
    }
    public int getAddress() {
        return address;
    }
    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "SymbolTable [lexema=" + lexema + ", category=" + category + ", type=" + type + ", address=" + address
                + "]";
    }

    

}
