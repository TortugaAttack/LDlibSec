package org.ldlibsec.anonymization.rdf.evaluation;

import com.github.andrewoma.dexx.collection.internal.adapter.MapAdapter;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.XSD;
import org.w3c.dom.Attr;

import java.util.Set;

public class AttributeCount {

    private String property;
    private String resource;
    private int count;
    private boolean isNumeric=false;

    public AttributeCount(RDFNode property, RDFNode resource){
        this.resource=resource.toString();
        this.property=property.toString();
        boolean test=false;

        if(resource.isLiteral()){
            //TODO what about language tags (they are equal and should be handled that way)
            this.resource=resource.asLiteral().getValue().toString();
            if(Number.class.isAssignableFrom(resource.asLiteral().getDatatype().getJavaClass())) {
                isNumeric = true;
            }
        }
        count=1;
    }

    public AttributeCount(String property, String resource, Boolean isNumeric){
        this.resource=resource;
        this.property=property;
        this.isNumeric=isNumeric;
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

    public boolean isNumeric() {
        return isNumeric;
    }
}
