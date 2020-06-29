package org.ldlibsec.anonymization.rdf;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;
import org.ldlibsec.anonymization.rdf.delanaux2020.DataLinkageRobustAnonymizer;

import java.util.List;

public class DataLinkageRobustAnonymizationTest {

    @Test
    public void testPaperExample4(){
        String privacyQueryStr = "SELECT ?x WHERE { ?x <http://xmlns.com/foaf/0.1/seenBy> ?y . ?y <http://xmlns.com/foaf/0.1/specialistOf> ?z. }";
        Query q = QueryFactory.create(privacyQueryStr);
        List<Query> pq = Lists.newArrayList(q);
        DataLinkageRobustAnonymizer anonymizer = new DataLinkageRobustAnonymizer();
        List<UpdateRequest> updates = anonymizer.findSaveOps(pq);
        for(int i = 0; i<updates.size(); i++) {
            System.out.println(i+": "+updates.get(i));
        }
    }
}
