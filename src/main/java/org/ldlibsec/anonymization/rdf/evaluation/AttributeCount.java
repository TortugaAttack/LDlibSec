package org.ldlibsec.anonymization.rdf.evaluation;

import org.w3c.dom.Attr;

public class AttributeCount {

    private String property;
    private String resource;
    private int count;

    public AttributeCount(String property, String resource){
        this.resource=resource;
        this.property=property;
        count=1;
    }

    public void incrCount(){
        this.count++;
    }
    public void incrCount(int n){
        this.count+=n;
    }

    public int getCount(){
        return count;
    }

    public String getProperty() {
        return property;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public int hashCode(){
        return (property+" "+resource).hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof AttributeCount){
            AttributeCount acOther = (AttributeCount) other;
            return  this.resource.equals(acOther.resource) && this.property.equals(acOther.property);
        }
        return false;
    }
}
