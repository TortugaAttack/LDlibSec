package org.ldlibsec.evaluation.score;

import org.apache.commons.math3.util.MathArrays;

public class VariationDistance implements  DistancesMeasure{

    @Override
    public double compute(double[] a, double[] b){
        MathArrays.checkEqualLength(a, b);
        double ret=0.0;
        for(int i=0;i<a.length;i++){
            ret +=  Math.abs(a[i]-b[i]);
        }
        return 0.5 * ret;
    }
}
