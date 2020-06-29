package org.ldlibsec.anonymization.rdf.delanaux2020.util;

import org.apache.jena.graph.Triple;
import org.ldlibsec.util.RDFFormatter;

import java.util.HashSet;
import java.util.Set;

public class SGPItem {

    private Set<Triple> triples = new HashSet<Triple>();
    private Set<String> connectors = new HashSet<String>();
    private Set<String> variables = new HashSet<String>();

    public Set<String> getVariables() {
        return variables;
    }

    public void addAll(SGPItem sgp){
        for(Triple t : sgp.triples){
            add(t);
        }
    }

    public void add(Triple t){
        triples.add(t);
        connectors.add(t.getSubject().toString());
        connectors.add(t.getObject().toString());
        if(t.getSubject().isVariable()){variables.add(t.getSubject().toString());}
        if(t.getPredicate().isVariable()){variables.add(t.getPredicate().toString());}
        if(t.getObject().isVariable()){variables.add(t.getObject().toString());}

    }

    public boolean checkConnection(Triple t){
        if(connectors.contains(t.getSubject().toString())){return true;}
        if(connectors.contains(t.getObject().toString())){return true;}
        return false;
    }

    public Set<Triple> getSet(){
        return triples;
    }

    public Set<String> getProjectionCritVals(Set<String> tCrit){
        Set<String> projection = new HashSet<String>();
        for(Triple t : triples){
            String subject = t.getSubject().toString();
            String predicate = t.getPredicate().toString();
            String object = t.getObject().toString();
            if(tCrit.contains(subject)){projection.add(subject);}
            if(tCrit.contains(predicate)){projection.add(predicate);}
            if(tCrit.contains(object)){projection.add(object);}
        }
        return projection;
    }

    public Integer size(){
        return triples.size();
    }

    public String getBody() {
        StringBuilder sb = new StringBuilder(" ");
        for(Triple t : triples){

            sb.append(RDFFormatter.getTripleBody(t));
        }
        sb.append(" ");

        return sb.toString();
    }
}
