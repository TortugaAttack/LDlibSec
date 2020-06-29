package org.ldlibsec.anonymization.rdf.evaluation;

import org.ldlibsec.evaluation.score.TClosenessPair;

import java.util.List;

public class AnonymityMeasure {

    private int kAnonymity;
    private int lDiversity;
    private  List<TClosenessPair> tCloseness;

    public int getkAnonymity() {
        return kAnonymity;
    }

    public void setkAnonymity(int kAnonymity) {
        this.kAnonymity = kAnonymity;
    }

    public int getlDiversity() {
        return lDiversity;
    }

    public void setlDiversity(int lDiversity) {
        this.lDiversity = lDiversity;
    }

    public  List<TClosenessPair> gettCloseness() {
        return tCloseness;
    }

    public void settCloseness( List<TClosenessPair> tCloseness) {
        this.tCloseness = tCloseness;
    }
}
