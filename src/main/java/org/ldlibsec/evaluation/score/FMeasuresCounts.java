package org.ldlibsec.evaluation.score;

public class FMeasuresCounts {

    private int tp;
    private int fp;
    private int fn;

    public FMeasuresCounts(int truePositives, int falsePositives, int falseNegatives){
        this.tp=truePositives;
        this.fp=falsePositives;
        this.fn=falseNegatives;
    }

    public int getTruePositives() {
        return tp;
    }

    public int getFalsePositives() {
        return fp;
    }

    public int getFalseNegatives() {
        return fn;
    }

    public void add(int truePositives, int falsePositives, int falseNegatives){
        this.tp += truePositives;
        this.fp += falsePositives;
        this.fn += falseNegatives;
    }

    public void add(FMeasuresCounts counts){
        this.add(counts.tp, counts.fp, counts.fn);
    }
}
