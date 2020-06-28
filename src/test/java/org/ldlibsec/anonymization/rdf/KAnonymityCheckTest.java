package org.ldlibsec.anonymization.rdf;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KAnonymityCheckTest {



    @Test
    public void test() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/kanon.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/gender"),
                ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/age"),
                ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/zip"));
        int k = RDFGraphAnonymizer.kAnonymityCheck(eoi, quasiId, m);
        assertEquals(2,k);
    }
}
