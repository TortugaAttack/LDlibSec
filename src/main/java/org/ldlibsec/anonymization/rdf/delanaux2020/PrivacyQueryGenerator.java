package org.ldlibsec.anonymization.rdf.delanaux2020;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Aims to generate data-dependent Privacy Queries for the @{DataLinkageRobustAnonymizer}
 *
 */
public class PrivacyQueryGenerator {

    /**
     * Generates privacy queries based upon attributes of interest
     *
     * @param qexec
     * @param attributesOfInterest
     * @return
     */
    public Collection<Query> generateAOIBased(QueryExecution qexec, Collection<Property> attributesOfInterest){
        return null;
    }

    /**
     * Generates privacy queries based upon entity of interest
     * @param qexec
     * @param entitiesOfInterest
     * @return
     */
    public Collection<Query> generateEOIBased(QueryExecution qexec, Collection<Resource> entitiesOfInterest){
        return null;
    }
}
