package org.ldlibsec.anonymization.rdf.evaluation;

import org.ldlibsec.evaluation.score.StringDoublePair;

import java.util.List;

public class AnonymityMeasure {

    private int kAnonymity;
    private List<StringDoublePair> lDiversity;
    private  List<StringDoublePair> tCloseness;

    public int getkAnonymity() {
        return kAnonymity;
    }

    public void setkAnonymity(int kAnonymity) {
        this.kAnonymity = kAnonymity;
    }

    public List<StringDoublePair> getlDiversity() {
        return lDiversity;
    }

    public void setlDiversity(List<StringDoublePair> lDiversity) {
        this.lDiversity = lDiversity;
    }

    @Deprecated
    public  List<StringDoublePair> gettCloseness() {
        return tCloseness;
    }

    public void settCloseness( List<StringDoublePair> tCloseness) {
        this.tCloseness = tCloseness;
    }
}
