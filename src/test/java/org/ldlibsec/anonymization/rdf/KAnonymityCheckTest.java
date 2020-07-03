package org.ldlibsec.anonymization.rdf;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.*;
import org.junit.Test;
import org.ldlibsec.anonymization.rdf.evaluation.AnonymityEvaluator;
import org.ldlibsec.anonymization.rdf.evaluation.AnonymityMeasure;
import org.ldlibsec.evaluation.score.StringDoublePair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class KAnonymityCheckTest {



    @Test
    public void testKAnonymity() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/kanon.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/gender"),
                ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/age"),
                ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/zip"));
        int k = AnonymityEvaluator.kAnonymityCheck(eoi, quasiId, m);
        assertEquals(2,k);
    }


    @Test
    public void testklt() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/tCloseness2.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty(":zone"));
        List<Property> sensible = Lists.newArrayList(ResourceFactory.createProperty(":incident"));
        AnonymityMeasure measures = AnonymityEvaluator.kltAnonymityCheck(eoi, quasiId, sensible, m);
        System.out.println(measures);
        assertEquals(2, measures.getkAnonymity());
        assertEquals(1.0, measures.getlDiversity().get(0).getValue(),0);
        assertEquals(0.4285714285714285, measures.gettCloseness().get(0).getValue(), 0);
    }

    @Test
    public void testTCloseness3() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/tCloseness3.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty(":age"),
                ResourceFactory.createProperty(":zip"));
        List<Property> sensible = Lists.newArrayList(ResourceFactory.createProperty(":disease"));
        List<StringDoublePair> tPairs = AnonymityEvaluator.tClosenessCheck(eoi, quasiId, sensible, m);
        System.out.println(tPairs);
    }

    @Test
    public void testTCloseness() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/tCloseness.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty("uri:age"),
                ResourceFactory.createProperty("uri:zip"));
        List<Property> sensible = Lists.newArrayList(ResourceFactory.createProperty("uri:salary"),
                ResourceFactory.createProperty("uri:disease"));
        List<StringDoublePair> tPairs = AnonymityEvaluator.tClosenessCheck(eoi, quasiId, sensible, m);
        System.out.println(tPairs);

    }

    @Test
    public void anonCheck() throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileReader("src/test/resources/tCloseness.nt"),null, "N-TRIPLE");
        List<Resource> eoi = m.listResourcesWithProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")).toList();
        List<Property> quasiId = Lists.newArrayList(ResourceFactory.createProperty("uri:age"),
                ResourceFactory.createProperty("uri:zip"));
        List<Property> sensible = Lists.newArrayList(ResourceFactory.createProperty("uri:salary"),
                ResourceFactory.createProperty("uri:disease"));
        AnonymityMeasure measure = AnonymityEvaluator.kltAnonymityCheck(eoi, quasiId, sensible, m);
        System.out.println(measure.getkAnonymity());
        System.out.println(measure.getlDiversity());
        System.out.println(measure.gettCloseness());
    }

}
