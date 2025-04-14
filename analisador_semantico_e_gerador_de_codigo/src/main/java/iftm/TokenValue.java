package iftm;

public class TokenValue {

    private String text;
    private int integer;
    private double FloatingPoint;

    public TokenValue(double FloatingPoint){
        this.FloatingPoint = FloatingPoint;
    }
    
    public TokenValue(int integer){
        this.integer = integer;
    }

    public TokenValue(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getInteger() {
        return integer;
    }
    public void setInteger(int integer) {
        this.integer = integer;
    }
    public double getFloatingPoint() {
        return FloatingPoint;
    }
    public void setFloatingPoint(double floatingPoint) {
        FloatingPoint = floatingPoint;
    }

    @Override
    public String toString() {
        return "TokenValue [text=" + text + ", integer=" + integer + ", FloatingPoint=" + FloatingPoint + "]";
    }
    
}
