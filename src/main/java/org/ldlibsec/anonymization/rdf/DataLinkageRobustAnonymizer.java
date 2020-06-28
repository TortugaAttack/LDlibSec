package org.ldlibsec.anonymization.rdf;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.ldlibsec.anonymization.rdf.util.ConnectedComponentsGatherer;
import org.ldlibsec.anonymization.rdf.util.GraphComponent;
import org.ldlibsec.anonymization.rdf.util.SGP;
import org.ldlibsec.util.RDFFormatter;

import java.util.*;

/**
 * Implementing the Algorithms of the RDF graph anonymization robust to data linkage
 *
 * Paper: Delanaux, Rémy, et al. "RDF graph anonymization robust to data linkage." International Conference on Web Information Systems Engineering. Springer, Cham, 2019.
 **
 */
public class DataLinkageRobustAnonymizer {

    private static final String BLANK_NODE_PREFIX = "_:b";
    private static final String VAR_PREFIX = "?v";

    public List<UpdateRequest> findSaveOpsSameAs(List<Query> privacyQueries){
        List<UpdateRequest> updates = new ArrayList<UpdateRequest>();
        for(Query q : privacyQueries){
            //Gather all connected components
            ConnectedComponentsGatherer ccg = new ConnectedComponentsGatherer(false);
            ElementWalker.walk(q.getQueryPattern(), ccg);
            Collection<GraphComponent> connectedComponents = ccg.getConnectedComponents();

            //iterate over each connected component block
            for(GraphComponent component : connectedComponents){
                for(Triple t : component.getTriples()) {
                    Map<String, Integer> crit2blank = new HashMap<String, Integer>();
                    Integer bnID = 1;
                    Integer varID = 1;
                    while (component.getBlanks().contains("b" + bnID)) {
                        bnID++;
                    }
                    for (String critVal : component.gettCrit(q.getProjectVars())) {
                        crit2blank.put(critVal,  bnID);
                        bnID++;
                    }
                    String body = RDFFormatter.getTripleBody(t);
                    String insertBody = body;
                    for (String replace : crit2blank.keySet()) {
                        insertBody = insertBody.replace(replace, BLANK_NODE_PREFIX+crit2blank.get(replace));
                        while(component.getVariables().contains(VAR_PREFIX+varID)){
                            varID++;
                        }
                        body = body.replace(replace, VAR_PREFIX+varID++);
                    }
                    Set<String> vars = new HashSet<String>();
                    getVars(t, vars);
                    updates.add(UpdateFactory.create(generateUpdate(vars, body, insertBody)));
                }
                if(component.getVariables().isEmpty()){
                    Random r = new Random();
                    int remove = r.nextInt(component.getTriples().size());
                    Triple t = component.getTriples().get(remove);
                    updates.add(UpdateFactory.create("DELETE {"+t.toString()+"} WHERE "+component.getBody()));
                }
            }
        }
        return updates;
    }


    private String generateUpdate(Set<String> vars, String body, String insertBody){

        StringBuilder notBlankFilter = new StringBuilder("FILTER(");

        int i=0;
        for(String var : vars){
            notBlankFilter.append("!isBlank(").append(var).append(")");
            if(i<vars.size()-1){
                notBlankFilter.append(" && ");
            }
            i++;
        }
        notBlankFilter.append(")");
        StringBuilder sbuilder = new StringBuilder("DELETE {");
        sbuilder.append(body);
        sbuilder.append("} INSERT {");
        sbuilder.append(insertBody);
        sbuilder.append("} WHERE {").append(body).append(notBlankFilter.toString()).append("}");
        return sbuilder.toString();
    }


    public List<UpdateRequest> findSaveOps(List<Query> privacyQueries){
        List<UpdateRequest> updates = new ArrayList<UpdateRequest>();
        for(Query q : privacyQueries){
            //Gather all connected components
            ConnectedComponentsGatherer ccg = new ConnectedComponentsGatherer(true);
            ElementWalker.walk(q.getQueryPattern(), ccg);
            Collection<GraphComponent> connectedComponents = ccg.getConnectedComponents();

            //iterate over each connected component block
            for(GraphComponent component : connectedComponents){
                for(SGP sgp : component.getConnectedBuckets()) {
                    Map<String, String> crit2blank = new HashMap<String, String>();
                    Integer bnID = 1;
                    if (component.getBlanks().contains("b" + bnID)) {
                        bnID++;
                    }
                    for (String critVal : sgp.getProjectionCritVals(component.gettCrit(q.getProjectVars()))) {
                        crit2blank.put(critVal, BLANK_NODE_PREFIX + bnID);
                        bnID++;
                    }
                    String body = sgp.getBody();
                    String insertBody = body;
                    for (String replace : crit2blank.keySet()) {
                        insertBody = insertBody.replace(replace, crit2blank.get(replace));
                    }

                    updates.add(UpdateFactory.create(generateUpdate(sgp.getVariables(), body, insertBody)));
                }
                if(component.getVariables().isEmpty()){
                    Random r = new Random();
                    int remove = r.nextInt(component.getTriples().size());
                    Triple t = component.getTriples().get(remove);
                    updates.add(UpdateFactory.create("DELETE {"+t.toString()+"} WHERE "+component.getBody()));
                }
            }
        }
        return updates;
    }

    private void getVars(Triple t, Set<String> vars) {
        if(t.getSubject().isVariable()){vars.add(t.getSubject().toString());}
        if(t.getPredicate().isVariable()){vars.add(t.getPredicate().toString());}
        if(t.getObject().isVariable()){vars.add(t.getObject().toString());}
    }

    private void countResVarOccurence(Triple t, Map<String, Integer> counts) {
        String subject = t.getSubject().toString();
        String object = t.getObject().toString();
        if(t.getSubject().isVariable() || t.getSubject().isURI()){
            counts.putIfAbsent(subject, 0);
            counts.put(subject, counts.get(subject)+1);
        }
        if(t.getObject().isVariable() || t.getObject().isURI() || t.getObject().isLiteral()){
            counts.putIfAbsent(object, 0);
            counts.put(object, counts.get(object)+1);
        }

    }

}
