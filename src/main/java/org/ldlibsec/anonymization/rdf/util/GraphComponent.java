package org.ldlibsec.anonymization.rdf.util;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.sparql.core.Var;
import org.ldlibsec.util.RDFFormatter;

import java.io.StringWriter;
import java.util.*;

public class GraphComponent {

    private List<Triple> triples = new ArrayList<Triple>();
    private List<SGP> sgps = new ArrayList<SGP>();
    private Set<String> variables = new HashSet<String>();
    private Set<String> tCrit = new HashSet<String>();
    private Set<String> blanks = new HashSet<String>();
    boolean buildSGPs=false;


    public GraphComponent(){
        this(false);
    }

    public GraphComponent(Boolean buildSGPs){
        this.buildSGPs=buildSGPs;
    }

    private Map<String, Integer> viriCount = new HashMap<String, Integer>();

    public List<Triple> getTriples() {
        return triples;
    }

    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public List<SGP> getConnectedBuckets(){
        Collections.sort(sgps, new Comparator<SGP>() {
            @Override
            public int compare(SGP sgp, SGP t1) {
                return t1.size().compareTo(sgp.size());
            }
        });
        return sgps;
    }

    public void setVariables(Set<String> variables) {
        this.variables = variables;
    }

    public Set<String> getBlanks(){
        return blanks;
    }



    public Set<String> gettCrit(List<Var> projectVars) {
        for(Var v : projectVars){
            if(variables.contains(v.toString(true))){
                tCrit.add(v.toString(true));
            }
        }
        return tCrit;
    }

    public void settCrit(Set<String> tCrit) {
        this.tCrit = tCrit;
    }


    public void addAll(GraphComponent gC) {
        for(Triple t : gC.triples){
            add(t);
        }


    }

    public void add(Triple t) {
        this.triples.add(t);
        if(buildSGPs){
            List<SGP> toAdd = new ArrayList<SGP>();
            for(SGP sgp : sgps){
                if(sgp.checkConnection(t)){
                    SGP newBucket = new SGP();
                    newBucket.addAll(sgp);
                    newBucket.add(t);
                    toAdd.add(newBucket);
                }
            }
            List<SGP> toAddCulm = new ArrayList<SGP>();
            //creating b5   here: b1: ac , b2: de, t: cd -> b3: acd and b4: cde, b5: acde
            for(int i=0;i<toAdd.size();i++){
                for(int k=i+1;k<toAdd.size();k++){
                    SGP newBucket = new SGP();
                    newBucket.addAll(toAdd.get(i));
                    newBucket.addAll(toAdd.get(k));
                    toAddCulm.add(newBucket);
                }
            }
            sgps.addAll(toAdd);
            sgps.addAll(toAddCulm);
            SGP singleSGP = new SGP();
            singleSGP.add(t);
            sgps.add(singleSGP);
        }
        addBlankNodes(t);
        //update Counts, tCrit and vars
        String subject = t.getSubject().toString();
        String object = t.getObject().toString();
        if(t.getSubject().isVariable()){variables.add(subject);}
        if(t.getObject().isVariable()){variables.add(object);}

        if(viriCount.containsKey(subject)){
            viriCount.put(subject, viriCount.get(subject)+1);
            tCrit.add(subject);
        }
        else{
            viriCount.put(subject, 1);
        }
        if(viriCount.containsKey(object)){
            viriCount.put(subject, viriCount.get(object)+1);
            tCrit.add(object);
        }
        else{
            viriCount.put(object, 1);
        }

    }

    private void addBlankNodes(Triple t) {
        if(t.getSubject().isBlank()){blanks.add(t.getSubject().getBlankNodeLabel());}
        if(t.getPredicate().isBlank()){blanks.add(t.getPredicate().getBlankNodeLabel());}
        if(t.getObject().isBlank()){blanks.add(t.getObject().getBlankNodeLabel());}
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
