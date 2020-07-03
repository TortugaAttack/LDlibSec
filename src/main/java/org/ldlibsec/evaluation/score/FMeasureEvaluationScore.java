package org.ldlibsec.evaluation.score;

import java.util.List;

/**
 * provides macro and micro f1 score, precision and recall
 */
public class FMeasureEvaluationScore {

    public enum F1Type {MacroF1, MicroF1}

    private double[] micro = new double[3];
    private double[] macro = new double[3];
    private F1Type name=F1Type.MacroF1;

    /**
     * tp, fp, fn
     * @param values
     */
    public FMeasureEvaluationScore(List<FMeasuresCounts> values) {
        calculate(values);
    }

    private void calculate(List<FMeasuresCounts> values){
        FMeasuresCounts summedCounts = new FMeasuresCounts(0,0,0);
        double[] measureSum = new double[]{0,0,0};
        for(FMeasuresCounts val : values){
            summedCounts.add(val);
            double[] measure = calculateMeasures(val);
            measureSum[0]+=measure[0];
            measureSum[1]+=measure[1];
            measureSum[2]+=measure[2];
        }
        this.micro = calculateMeasures(summedCounts);
        this.macro = new double[]{measureSum[0]*1.0/values.size(), measureSum[1]*1.0/values.size(),measureSum[2]*1.0/values.size()};
    }

    private double[] calculateMeasures(FMeasuresCounts val) {
        double precision, recall, f1;
        if (val.getTruePositives()== 0) {
            if ((val.getFalsePositives() == 0) && (val.getFalseNegatives() == 0)) {
                precision = 1.0;
                recall = 1.0;
                f1 = 1.0;
            } else {
                precision = 0.0;
                recall = 0.0;
                f1 = 0.0;
            }
        } else {
            precision = val.getTruePositives() *1.0 / (val.getTruePositives() + val.getFalsePositives());
            recall = val.getTruePositives() *1.0/ (double) (val.getTruePositives()+val.getFalseNegatives());
            f1 = (2 * precision * recall) / (precision + recall);
        }
        return new double[] { f1, precision, recall,  };
    }


    public double getMicroF1score() {
        return micro[0];
    }

    public double getMicroPrecision() {
        return micro[1];
    }

    public double getMicroRecall() {
        return micro[2];
    }

    public double getMacroF1score() {
        return macro[0];
    }

    public double getMacroPrecision() {
        return macro[1];
    }

    public double getMacroRecall() {
        return macro[2];
    }
}
