package org.ldlibsec.anonymization.rdf.evaluation;

import org.aksw.limes.core.controller.Controller;
import org.aksw.limes.core.controller.LimesResult;
import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.mapping.AMapping;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.ldlibsec.anonymization.rdf.evaluation.limes.ConfidencePair;
import org.ldlibsec.evaluation.score.*;

import java.io.File;
import java.util.*;

public class AnonymityEvaluator {


    /**
     * Tries to de-anonymize given entities of interest over two graphs, assuming that the entities are
     * somewhat anonymized within the anonymized graph.<br/>
     * using LIMES and returns F1, Precision and Recall<br/>
     * <br/>
     * Provides a simple evaluation if anonymized EOI can be de-anonymized with a public dataset which might include the EOIs
     *
     * @param benchmark The Limes Configuration representing the benchmark
     * @param anonymized The Graph which was anonymized (either absolute path or sparql endpoint)
     * @param additionalData Other graph which might contain these (either absolute path or sparql endpoint)
     * @param anonymizedToReal Basically the gold standard. Mapping from Anonymized Node to Actual Ident.
     * @return f1 score
     */
    public static FMeasureEvaluationScore limesDeAnonymizationTest(Configuration benchmark, String anonymized, String additionalData, Map<String, String> anonymizedToReal){
        List<FMeasuresCounts> values = new ArrayList<FMeasuresCounts>();
        benchmark.getSourceInfo().setEndpoint(anonymized);
        benchmark.getTargetInfo().setEndpoint(additionalData);
        Map<String, List<ConfidencePair>> deaonymizedEOIs = new HashMap<String, List<ConfidencePair>>();
        LimesResult res = Controller.getMapping(benchmark);
        Map<String, HashMap<String, Double>> reIdentMap = res.getAcceptanceMapping().getMap();
        for(String anonEOI : reIdentMap.keySet()){
            String actualEntity = anonymizedToReal.get(anonEOI);
            Map<String, Double> confidenceScores = reIdentMap.get(anonEOI);
            List<ConfidencePair> sortedConfidence = sortReIdentMap(confidenceScores);
            deaonymizedEOIs.put(anonEOI, sortedConfidence);
        }
        /*
        We check if Limes could link to the correct entity at all,
        if so: if it is the entity with the most confidence (we could identify the target with ease
                however if there are several other entities before our target, it isn't easy however we still could figure out the entity at some point
                if the target could not be identified, that's good.
         */
        for(String eoi : deaonymizedEOIs.keySet()){
            int tp=0;
            int fp=0;
            int fn=0;
            boolean foundEOI = false;
            double oldConfidence=0;
            for(ConfidencePair deAnonEOI : deaonymizedEOIs.get(eoi)){
                /*
                what if k entities (e.g. due to kAnonymity) were accepted by limes, but as they are indistinguishable for Limes, they all have the same confidence.
                thus we should use Blocks of Entities with the same confidence level instead and  if the entity is in there -> tp=1, and for all other entities fp=n-1 +(all previous false positive entities)
                 */
                if(oldConfidence != deAnonEOI.confidence && foundEOI){
                    //confidence changed so break the loop, otherwise as long as they have the same confidence: add fp 
                    break;
                }
                if((deAnonEOI.entity == null && eoi == null) ||
                        (deAnonEOI.entity != null && deAnonEOI.entity.equals(eoi))){
                    tp=1;
                    foundEOI=true;
                }
                else{
                    //Found higher ranked false positive
                    fp++;
                }
                oldConfidence = deAnonEOI.confidence;
            }
            //fn =1 if
            if(!foundEOI){
                //false negative is 1
                fn=1;
            }
            values.add(new FMeasuresCounts(tp,fp,fn));

        }
        return new FMeasureEvaluationScore(values);
    }

    private static List<ConfidencePair> sortReIdentMap(Map<String, Double> confidenceScores) {
        List<ConfidencePair> ret = new ArrayList<ConfidencePair>();
        for(String key : confidenceScores.keySet()){
            ret.add(new ConfidencePair(key, confidenceScores.get(key)));
        }

        Collections.sort(ret, new Comparator<ConfidencePair>() {

            @Override
            public int compare(ConfidencePair s, ConfidencePair t1) {
                return t1.confidence.compareTo(s.confidence);
            }
        });
        return ret;
    }


    /**
     * Checks the 1-hop Neighbourhood of each EOI and calculates the highest k-anonymity achieved
     *
     * Assuming that identifiable attributes and labels were deleted.
     *
     * @param entitiesOfInterest
     * @param graph
     * @return biggest k reached for all EOIs
     */
    public static int kAnonymityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, Model graph){
        return kltAnonymityCheck(entitiesOfInterest, quasiIdentifiable, new ArrayList<Property>(), graph).getkAnonymity();
    }

    public static AnonymityMeasure kltAnonymityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
        Collections.sort(quasiIdentifiable, new Comparator<Property>() {
            @Override
            public int compare(Property property, Property t1) {
                return property.toString().compareTo(t1.toString());
            }
        });
        List<EquivalentClass> eqClasses = new ArrayList<EquivalentClass>();
        for(Resource entity : entitiesOfInterest){
            List<AttributeCount> attrIn = new ArrayList<AttributeCount>();
            List<AttributeCount> attrOut = new ArrayList<AttributeCount>();
            List<AttributeCount> sensibleValues = new ArrayList<AttributeCount>();

            addEdges(entity, sensibleAttributes, graph, sensibleValues, sensibleValues);
            addEdges(entity, quasiIdentifiable, graph, attrOut, attrIn);
            boolean notFoundClass=true;
            for(EquivalentClass eqClass : eqClasses){
                if(eqClass.check(attrIn, attrOut)){
                    eqClass.addEntity(entity.toString(), sensibleValues);
                    notFoundClass=false;
                }
            }
            if(notFoundClass){
                EquivalentClass eqClass = new EquivalentClass(entity.toString(), attrIn, attrOut, sensibleValues);
                eqClasses.add(eqClass);
            }

        }
        AnonymityMeasure measure = new AnonymityMeasure();
        int k = calculateK(eqClasses);
        measure.setkAnonymity(k);
        List<StringDoublePair> l = calculateL(eqClasses);
        measure.setlDiversity(l);
        List<StringDoublePair> t = calculateT(entitiesOfInterest, eqClasses);
        measure.settCloseness(t);
        return measure;
    }

    private static List<StringDoublePair> calculateL(List<EquivalentClass> eqClasses) {
        List<StringDoublePair> ret = new ArrayList<StringDoublePair>();
        Collection<String> attributes = getAttributes(eqClasses);
        Map<String, List<AttributeCount>> features = getFeatures(eqClasses, attributes);
        for(String attr : features.keySet()) {

            int l = -1;
            for (EquivalentClass eqClass : eqClasses) {
                int classLDiv = eqClass.calculateLDiversity(attr);
                if (l == -1) {
                    l = classLDiv;
                } else {
                    l = Math.min(l, classLDiv);
                }
            }
            ret.add(new StringDoublePair(l, attr));
        }
        return ret;
    }

    private static List<StringDoublePair> calculateT(Collection<Resource> entitiesOfInterest, List<EquivalentClass> eqClasses) {
        List<StringDoublePair> ret = new ArrayList<StringDoublePair>();
        EarthMoversDistance emd = new EarthMoversDistance();
        Collection<String> attributes = getAttributes(eqClasses);
        Map<String, List<AttributeCount>> features = getFeatures(eqClasses, attributes);
        for(String attr : features.keySet()) {
            double t =-1;

            boolean isNumeric = features.get(attr).get(0).isNumeric();
            DistancesMeasure measure;
            if(isNumeric){
                measure = new EarthMoversDistance();
            }
            else{
                measure = new VariationDistance();
            }
            for (EquivalentClass eqClass : eqClasses) {

                double[][] distributions = getDistributions(eqClass,features.get(attr), attr, entitiesOfInterest.size(), isNumeric);
                if (t == -1) {
                    t = measure.compute(distributions[0], distributions[1]);
                } else {
                    t = Math.max(t, measure.compute(distributions[0], distributions[1]));
                }
            }
            ret.add(new StringDoublePair(t, attr));
        }
        return ret;
    }

    private static Collection<String> getAttributes(List<EquivalentClass> eqClasses) {
        Set<String> attributes = new HashSet<String>();
        for(EquivalentClass eqClass : eqClasses) {
            for(AttributeCount aCount : eqClass.getSensibleValues()){
                attributes.add(aCount.getProperty());
            }
        }
        return attributes;
    }



    private static double[][] getDistributions(EquivalentClass eqClass, List<AttributeCount> features, String attr, int numberOfEOIs, boolean numerical) {
        Collection<AttributeCount> classFeatures = eqClass.getSensibleValues(attr);
        double[] p = new double[features.size()];
        int valuesInClass = eqClass.countSensibleValues(attr);
        List<AttributeCount> featureCopy = new ArrayList<AttributeCount>(features);
        if(numerical){
            Collections.sort(featureCopy, new Comparator<AttributeCount>() {
                @Override
                public int compare(AttributeCount attributeCount, AttributeCount t1) {
                    Double val1 =  Double.parseDouble(attributeCount.getResource());
                    Double val2 =  Double.parseDouble(t1.getResource());
                    return val1.compareTo(val2);
                }
            });
        }
        for(AttributeCount classFeature : classFeatures) {
            p[featureCopy.indexOf(classFeature)] = classFeature.getCount()*1.0/valuesInClass;
        }
        double[] q = new double[features.size()];

        for(int i=0;i<featureCopy.size();i++){
            q[i]=featureCopy.get(i).getCount()*1.0/numberOfEOIs;
        }
        return new double[][]{p, q};
    }

    private static Map<String, List<AttributeCount>> getFeatures(List<EquivalentClass> eqClasses, Collection<String> attributes) {
        Map<String, List<AttributeCount>> features = new HashMap<String, List<AttributeCount>>();
        for(EquivalentClass eqClass : eqClasses){
            Collection<AttributeCount> currentFeatures = eqClass.getSensibleValues();
            for(AttributeCount feat : currentFeatures){
                //check if attribute already exists
                AttributeCount copy = new AttributeCount(feat.getProperty(), feat.getResource(), feat.isNumeric());
                copy.incrCount(feat.getCount()-1);
                features.putIfAbsent(feat.getProperty(), new ArrayList<AttributeCount>());
                int index = -1;
                if ((index = features.get(feat.getProperty()).indexOf(feat)) == -1) {
                    //Feature not yet seen, so just add it
                    features.get(feat.getProperty()).add(copy);
                } else {
                    features.get(feat.getProperty()).get(index).incrCount(copy.getCount());
                }
            }
        }
        return features;
    }



    private static int calculateK(List<EquivalentClass> eqClasses) {
        int k=-1;
        for(EquivalentClass eqClass : eqClasses){
            if(k==-1){
                k= eqClass.size();
            }else {
                k = Math.min(k, eqClass.size());
            }
        }
        return k;
    }

    private static void addEdges(Resource entity, List<Property> properties, Model graph, List<AttributeCount> outSet, List<AttributeCount> inSet){
        for(Property qIAttr : properties) {
            List<Statement> subjectParts = graph.listStatements(entity, qIAttr, (RDFNode) null).toList();
            //TODO what if stmts are empty? should a null be added?
            for(Statement stmt : subjectParts){
                AttributeCount acCount = new AttributeCount(stmt.getPredicate(), stmt.getObject());
                int index=-1;
                if((index=outSet.indexOf(acCount))!=-1){
                    outSet.get(index).incrCount();
                }
                outSet.add(acCount);
            }
            List<Statement> objectParts = graph.listStatements(null, qIAttr, entity).toList();
            //TODO what if stmts are empty? should a null be added?
            for(Statement stmt : objectParts){
                AttributeCount acCount = new AttributeCount(stmt.getPredicate(), stmt.getSubject());
                int index=-1;
                if((index=outSet.indexOf(acCount))!=-1){
                    inSet.get(index).incrCount();
                }
                inSet.add(acCount);
            }
        }
    }

    public static List<StringDoublePair>  lDiversityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
        return kltAnonymityCheck(entitiesOfInterest, quasiIdentifiable, sensibleAttributes, graph).getlDiversity();
    }

    /**
     *
     *
     * @param entitiesOfInterest
     * @param quasiIdentifiable
     * @param sensibleAttributes
     * @param graph
     * @return
     */
    public static  List<StringDoublePair> tClosenessCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
        return kltAnonymityCheck(entitiesOfInterest, quasiIdentifiable, sensibleAttributes, graph).gettCloseness();
    }


}
