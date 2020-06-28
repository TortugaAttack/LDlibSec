package org.ldlibsec.anonymization.rdf.util;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

import java.util.*;

public class ConnectedComponentsGatherer extends ElementVisitorBase {

    private Map<String, Integer> nodes2CC = new HashMap<String, Integer>();
    private Map<Integer, GraphComponent> id2CC = new HashMap<Integer, GraphComponent>();
    private boolean buildSGPs = false;

    public ConnectedComponentsGatherer(boolean buildSGPs){
        this.buildSGPs=buildSGPs;
    }

    public Collection<GraphComponent> getConnectedComponents(){
        return id2CC.values();
    }

    public void visit(ElementPathBlock el) {
        Integer startId =0;
        for(TriplePath tp: el.getPattern().getList()){
            Triple t = tp.asTriple();
            String subject = t.getSubject().toString();
            String object = t.getObject().toString();
            int ID1=-1;
            int ID2=-1;
            if(nodes2CC.containsKey(subject)){
                ID1 = nodes2CC.get(subject);
            }
            if(nodes2CC.containsKey(object)){
                ID2 = nodes2CC.get(object);
            }
            if(ID1>=0 && ID2>=0 ){
                if(ID1 != ID2){
                    //combine, change ID2 and add all of ID2 to ID1
                    id2CC.get(ID1).addAll(id2CC.get(ID2));
                    id2CC.remove(ID2);
                    nodes2CC.put(object, ID1);
                }
                else{
                    //both are already connected, thus just add to list
                    id2CC.get(ID1).add(t);
                }
            }
            else if(ID1>=0){
                nodes2CC.put(object, ID1);
                id2CC.get(ID1).add(t);
            }
            else if(ID2>=0){
                nodes2CC.put(subject, ID2);
                id2CC.get(ID2).add(t);
            }
            else{
                int id = startId;
                startId++;
                id2CC.put(id, new GraphComponent(buildSGPs));
                id2CC.get(id).add(t);
                nodes2CC.put(subject, id);
                if(!subject.equals(object)) {
                    nodes2CC.put(object, id);
                }
            }
        }
    }

    public void visit(ElementTriplesBlock el) {
        Integer startId =0;
        for(Triple t: el.getPattern().getList()){
            String subject = t.getSubject().toString();
            String object = t.getObject().toString();
            int ID1=-1;
            int ID2=-1;
            if(nodes2CC.containsKey(subject)){
                ID1 = nodes2CC.get(subject);
            }
            if(nodes2CC.containsKey(object)){
                ID2 = nodes2CC.get(object);
            }
            if(ID1>=0 && ID2>=0 ){
                if(ID1 != ID2){
                    //combine, change ID2 and add all of ID2 to ID1
                    id2CC.get(ID1).addAll(id2CC.get(ID2));
                    id2CC.remove(ID2);
                    nodes2CC.put(object, ID1);
                }
                else{
                    //both are already connected, thus just add to list
                    id2CC.get(ID1).add(t);
                }
            }
            else if(ID1>=0){
                nodes2CC.put(object, ID1);
                id2CC.get(ID1).add(t);
            }
            else if(ID2>=0){
                nodes2CC.put(subject, ID2);
                id2CC.get(ID2).add(t);
            }
            else{
                int id = startId;
                startId++;
                id2CC.put(id, new GraphComponent(buildSGPs));
                id2CC.get(id).add(t);
                nodes2CC.put(subject, id);
                if(!subject.equals(object)) {
                    nodes2CC.put(object, id);
                }
            }
        }
    }
}
