package org.ldlibsec.evaluation.score;

public class TClosenessPair {

    private double tCloseness=0;
    private String attribute;

    public TClosenessPair(double tCloseness, String attribute){
        this.attribute=attribute;
        this.tCloseness=tCloseness;
    }

    public double gettCloseness() {
        return tCloseness;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public String toString(){
        return attribute+": "+tCloseness;
    }
}
