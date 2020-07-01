package org.ldlibsec.anonymization.rdf.hermsen2017.util;

import org.apache.jena.rdf.model.Resource;
import org.ldlibsec.anonymization.rdf.evaluation.AttributeCount;

import java.util.Set;

public class FNHC {

    public Resource entity;
    public Set<NHC> neighbourHoodCode;


    /**
     * sim(FNHC, FNHC) in the paper
     *
     * @param other
     * @return
     */
    public double distance(FNHC other, double alpha, double beta, double gamma, double delta){
        //TODO
        return 0;
    }

}
