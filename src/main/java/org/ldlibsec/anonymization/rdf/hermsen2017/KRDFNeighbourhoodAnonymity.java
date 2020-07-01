package org.ldlibsec.anonymization.rdf.hermsen2017;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.ldlibsec.anonymization.rdf.hermsen2017.util.FNHC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementing the k-RDF-Neighbourhood Anonymity Algorithm from the respective Paper published at PrivOn 2017 @ ISWC
 *
 * Paper: Heitmann, Benjamin, Felix Hermsen, and Stefan Decker. "k-RDF-Neighbourhood Anonymity: Combining Structural and Attribute-based Anonymisation for Linked Data." PrivOn@ ISWC 1951 (2017).
 *
 */
public class KRDFNeighbourhoodAnonymity {

    private String uriPrefix = "http://ldlibsec.org/anonymize/resource/";

    /**
     *
     * @param graph
     * @param entitiesOfInterest
     * @param k
     * @param alpha
     * @param beta
     * @param gamma
     * @param delta
     */
    public void anonymize(Model graph, List<Resource> entitiesOfInterest, List<Property> identifiableAttr, int k, double alpha, double beta, double gamma, double delta){
        int id=0;
        List<Resource> newEntities = new ArrayList<Resource>();
        Map<String, FNHC> fhncMap = new HashMap<String, FNHC>();
        for(Resource entity : entitiesOfInterest){
            FNHC code = calculateFHNC(graph, entity);
            removeIdentAttr(graph, entity, identifiableAttr);
            Resource newIdent = ResourceFactory.createResource(uriPrefix+id++);
            updateEntityURI(graph, entity, newIdent);
            newEntities.add(newIdent);
            fhncMap.put(newIdent.toString(), code);
        }

        while(newEntities.size()>(2*(k-1))){
            Resource target = getHighestDegreeEOI(graph, newEntities);
            newEntities.remove(target);
            FNHC targetFNHC= fhncMap.get(target);
            List<Double> distanceList = new ArrayList<Double>();
            for(Resource eoi : newEntities){
                FNHC eoiFNHC = fhncMap.get(eoi);
                distanceList.add(targetFNHC.distance(eoiFNHC, alpha, beta, gamma, delta));
            }
            List<Resource> bestFittingResources = new ArrayList<Resource>();
            for(int i=0; i<(k-1);i++){
                //TODO should also include l-Diversity
                bestFittingResources.add(findMinimumDistanceVertex(distanceList, newEntities));
            }
            graphModification(graph, target, bestFittingResources);
            //bestFittingResources are already removed
        }
    }

    private Resource getHighestDegreeEOI(Model graph, List<Resource> newEntities) {
        int inEdges=0;
        Resource ret = null;
        for(Resource entity : newEntities){
            int currentInEdges = graph.listStatements(entity, null, (RDFNode)null).toList().size();
            if(currentInEdges>inEdges){
                inEdges=currentInEdges;
                ret = entity;
            }
        }
        return ret;
    }

    private void graphModification(Model graph, Resource target, List<Resource> bestFittingResources) {
        //TODO remove all edges where at least one  vertex does not have this attribute
        //TODO for all String attributes -> try and get Common Lexical ground (e.g. 12345 and 17899 would result into 1*)
        //              otherwise try and get a*-t* or similar to age group
        //TODO age should be grouped (not in youngest to oldest!, round up for oldest, down for youngest)

    }

    private Resource findMinimumDistanceVertex(List<Double> distanceList, List<Resource> eois) {
        double val = 5;
        Resource ret=null;
        int j=0;
        for(int i=0;i<distanceList.size();i++){
            Double cVal = distanceList.get(i);
            if(cVal !=null && cVal<val){
                val = cVal;
                ret = eois.get(i);
                j=i;
            }
        }
        distanceList.remove(j);
        eois.remove(j);
        return ret;
    }

    private void updateEntityURI(Model graph, Resource entity, Resource newIdent) {
        StmtIterator stmtIt = graph.listStatements(entity, null, (RDFNode) null);
        List<Statement> add = new ArrayList<Statement>();
        List<Statement> remove = new ArrayList<Statement>();

        while(stmtIt.hasNext()){
            Statement stmt =stmtIt.nextStatement();
            remove.add(stmt);
            add.add(new StatementImpl(newIdent, stmt.getPredicate(), stmt.getObject()));
        }
        stmtIt = graph.listStatements(null, null, entity);
        while(stmtIt.hasNext()){
            Statement stmt =stmtIt.nextStatement();
            remove.add(stmt);
            add.add(new StatementImpl(stmt.getSubject(), stmt.getPredicate(), newIdent));
        }
        graph.remove(remove);
        graph.add(add);
    }


    private void updateEntityURI(String endpoint, Resource entity, Resource newIdent) {
        String queryStrOut = "DELETE {<ENTITY> ?p ?o} INSERT {<NEWIDENT> ?p ?o} WHERE {<ENTITY> ?p ?o}";
        String queryStrIn = "DELETE {?s ?p <ENTITY>} INSERT {?s ?p  <NEWIDENT>} WHERE {?s ?p <ENTITY>}";
        queryStrOut = queryStrOut.replace("ENTITY", entity.toString()).replace("NEWIDENT", newIdent.toString());
        queryStrIn = queryStrIn.replace("ENTITY", entity.toString()).replace("NEWIDENT", newIdent.toString());
        UpdateRequest req = UpdateFactory.create(queryStrOut);
        req.add(queryStrIn);
        UpdateProcessor uprocessor = UpdateExecutionFactory.createRemote(req.getOperations().get(0), endpoint);
        uprocessor.execute();
    }

    private void removeIdentAttr(Model graph, Resource entity, List<Property> identifiableAttr) {
        for(Property attr : identifiableAttr){
            graph.removeAll(entity, attr, (RDFNode) null);
        }
    }

    private FNHC calculateFHNC(Model graph, Resource entity) {
        //TODO
        FNHC ret = new FNHC();
        //TODO get all attribute vals  for attributes (age, based_near,  collab, social)
        return ret;
    }

}
