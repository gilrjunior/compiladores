package iftm;

public enum SymbolType {
    
    INTEGER ("Inteiro"),
    REAL ("Real"),
    BOOLEAN ("Booleano"),
    STRING ("String");

    private String description;

    private SymbolType(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
