package org.ldlibsec.anonymization.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;

import java.util.*;

public class RDFGraphAnonymizer {

    /**
     * Checks the 1-hop Neighbourhood of each EOI and calculates the highest k-anonymity achieved
     *
     * Assuming that identifiable attributes and labels were deleted.
     *
     * @param entitiesOfInterest
     * @param graph
     * @return biggest k reached for all EOIs
     */
    public static int kAnonymityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable,  Model graph){
        Collections.sort(quasiIdentifiable, new Comparator<Property>() {
            @Override
            public int compare(Property property, Property t1) {
                return property.toString().compareTo(t1.toString());
            }
        });
        Map<String, Integer> counter = new HashMap<String, Integer>();
        //TODO something better then this String rubbish
        for(Resource entity : entitiesOfInterest){
            StringBuilder outAttr = new StringBuilder();
            StringBuilder inAttr = new StringBuilder();

            for(Property qIAttr : quasiIdentifiable) {
                List<Statement> subjectParts = graph.listStatements(entity, qIAttr, (RDFNode) null).toList();
                for(Statement stmt : subjectParts){
                    outAttr.append(stmt.getObject().toString()).append(";");
                }
                List<Statement> objectParts = graph.listStatements(null, qIAttr, entity).toList();
                for(Statement stmt : objectParts){
                    inAttr.append(stmt.getSubject().toString()).append(";");
                }
                outAttr.append(".");
                inAttr.append(".");
            }
            String code = inAttr+"$"+outAttr;
            counter.putIfAbsent(code, 0);
            counter.put(code, counter.get(code)+1);
        }

        int k=-1;
        for(String key : counter.keySet()){
            if(k==-1){
                k= counter.get(key);
            }else {
                k = Math.min(k, counter.get(key));
            }
        }
       return k;
    }

    public static int lDiversityCheck(Collection<Node> entitiesOfInterest, Model graph){
        return 0;
    }
    public static int tClosenessCheck(Collection<Node> entitiesOfInterest, Model graph){
        return 0;
    }


    public void kRDFNeighbourhoodAnonymity(){

    }

}
