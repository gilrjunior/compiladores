package iftm;

public enum SymbolCategory {

    VARIABLE ("Função"),
    FUNCTION ("Variável"),
    PARAMETER ("Parâmetro"),
    TYPE ("Tipo"),
    PROCEDURE ("Procedimento"),
    MAINPROGRAM ("Programa Principal");

    private String description;

    private SymbolCategory(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

}
