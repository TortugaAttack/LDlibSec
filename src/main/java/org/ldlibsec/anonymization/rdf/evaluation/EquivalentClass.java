package org.ldlibsec.anonymization.rdf.evaluation;

import org.w3c.dom.Attr;

import java.util.*;

public class EquivalentClass {

    private Collection<AttributeCount> sensibleValues;
    private Collection<AttributeCount> attributesIn;
    private Collection<AttributeCount> attributesOut;

    private Set<String> entities = new HashSet<String>();

    public EquivalentClass(String entity, Collection<AttributeCount> attrIn, Collection<AttributeCount> attrOut, Collection<AttributeCount> sensibleValues) {
        this.entities.add(entity);
        this.attributesIn=attrIn;
        this.attributesOut=attrOut;
        this.sensibleValues = sensibleValues;
    }


    public int countSensibleValues(String property){
        int size=0;
        for(AttributeCount aCount : sensibleValues){
            if(aCount.getProperty().equals(property)) {
                size += aCount.getCount();
            }
        }
        return size;
    }

    public int calculateLDiversity(String attr){
        int l=0;
        for(AttributeCount aCount : new HashSet<AttributeCount>(this.sensibleValues)){
            if(aCount.getProperty().equals(attr)){
                l++;
            }
        }
        //deduplicate and get size
        return l;
    }

    public int size(){
        return entities.size();
    }

    public boolean check(Collection<AttributeCount> attributesIn, Collection<AttributeCount> attributesOut){
        boolean check = checkAttr(this.attributesIn, attributesIn);
        check &= checkAttr(this.attributesOut, attributesOut);
        return check;
    }

    public void addEntity(String entity, Collection<AttributeCount> sensibleValues){

        this.entities.add(entity);
        Set<AttributeCount> add = new HashSet<AttributeCount>();
        if(sensibleValues!=null) {
            for(AttributeCount nCount : sensibleValues) {
                boolean notFound =true;
                for (AttributeCount aCount : this.sensibleValues) {
                    if(aCount.equals(nCount)){
                        aCount.incrCount(nCount.getCount());
                        notFound=false;
                        break;
                    }
                }
                if(notFound){
                    add.add(nCount);
                }
            }
            this.sensibleValues.addAll(add);
        }
    }

    private boolean checkAttr(Collection<AttributeCount> expectedAttr, Collection<AttributeCount> actualAttr){
        if(actualAttr.size()!=expectedAttr.size()){
            //cant be the same
            return false;
        }

        for(AttributeCount actual : actualAttr){
            boolean notFound=true;
            for(AttributeCount expected : expectedAttr){
                if(actual.equals(expected)){
                    notFound=false;
                    break;
                }
            }
            if(notFound){
                return false;
            }
        }
        //all could be found and as they are the same size and have no duplicates (Set), they have to be the same.
        return true;
    }

    public Collection<AttributeCount> getSensibleValues() {
        return sensibleValues;
    }

    public Collection<AttributeCount> getSensibleValues(String attribute) {
        List<AttributeCount> ret = new ArrayList<AttributeCount>();
        for(AttributeCount aCount : sensibleValues){
            if(attribute.equals(aCount.getProperty())){
                ret.add(aCount);
            }
        }
        return ret;
    }
}
