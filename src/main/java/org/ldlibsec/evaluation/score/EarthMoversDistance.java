package org.ldlibsec.evaluation.score;

import org.apache.commons.math3.util.MathArrays;

public class EarthMoversDistance implements DistancesMeasure {

    @Override
    public double compute(double[] a, double[] b) {
        return new org.apache.commons.math3.ml.distance.EarthMoversDistance().compute(a, b)/(a.length-1);
    }

    private double computeBad(double[] a, double[] b){
        MathArrays.checkEqualLength(a, b);
        double ret=0.0;
        for(int i=0;i<a.length;i++) {
            double tmp = 0.0;
            for (int k = 0; k < i; k++) {
                tmp += a[k] - b[k];
            }
            ret+=Math.abs(tmp);

        }
        return 1.0/(a.length-1) * ret;
    }
}
