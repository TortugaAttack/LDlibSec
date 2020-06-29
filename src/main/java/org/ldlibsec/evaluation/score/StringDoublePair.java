package org.ldlibsec.evaluation.score;

public class StringDoublePair {

    private double value=0;
    private String attribute;

    public StringDoublePair(double value, String attribute){
        this.attribute=attribute;
        this.value=value;
    }

    public double getValue() {
        return value;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public String toString(){
        return attribute+": "+value;
    }
}
