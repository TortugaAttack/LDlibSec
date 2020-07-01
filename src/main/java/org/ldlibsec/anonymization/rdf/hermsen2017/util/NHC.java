package org.ldlibsec.anonymization.rdf.hermsen2017.util;

import org.ldlibsec.anonymization.rdf.evaluation.AttributeCount;

import java.util.ArrayList;
import java.util.List;

public abstract class NHC {

    protected List<AttributeCount> attributes = new ArrayList<>();

    protected NHC(List<AttributeCount> attributes){
        this.attributes=attributes;
    }



    public abstract String code();
}
