package org.ldlibsec.anonymization.rdf.evaluation.limes;

public class ConfidencePair {

    public String entity;
    public Double confidence;

    public ConfidencePair(String entity, Double confidence) {
        this.entity=entity;
        this.confidence=confidence;
    }
}
