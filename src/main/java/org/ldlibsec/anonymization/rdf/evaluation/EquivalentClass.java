package org.ldlibsec.anonymization.rdf.evaluation;

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

    public int countSensibleValues(){
        int size=0;
        for(AttributeCount aCount : sensibleValues){
            size+=aCount.getCount();
        }
        return size;
    }

    public int calculateLDiversity(){
        //TODO per Attribute
        //deduplicate and get size
        return new HashSet<AttributeCount>(this.sensibleValues).size();
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
        if(sensibleValues!=null)
            this.sensibleValues.addAll(sensibleValues);
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
