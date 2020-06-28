package org.ldlibsec.anonymization.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;

import java.util.Collection;

public class RDFGraphAnonymizer {

    public boolean kAnonymityCheck(Collection<Node> entitiesOfInterest, Model graph){
       return false;
    }
    public boolean lDiversityCheck(Collection<Node> entitiesOfInterest, Model graph){
        return false;
    }
    public boolean tClosenessCheck(Collection<Node> entitiesOfInterest, Model graph){
        return false;
    }


    public void kRDFNeighbourhoodAnonymity(){

    }

}
