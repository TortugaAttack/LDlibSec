package org.ldlibsec.anonymization.rdf.evaluation;

import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.ldlibsec.evaluation.score.FMeasureEvaluationScore;
import org.ldlibsec.evaluation.score.FMeasuresCounts;
import org.ldlibsec.evaluation.score.StringDoublePair;
import org.ldlibsec.evaluation.score.VariationDistance;

import java.util.*;

public class AnonymityEvaluator {


    /**
     * Tries to de-anonymize given entities of interest over two graphs, assuming that the entities are
     * somewhat anonymized within the anonymized graph.<br/>
     * using LIMES and returns F1, Precision and Recall<br/>
     * <br/>
     * Provides a simple evaluation if anonymized EOI can be de-anonymized with a public dataset which might include the EOIs
     *
     * @param anonymized The Graph which was anonymized
     * @param additionalData Other graph which might contain these
     * @param entitiesOfInterestAnonymized anonymized entities within the anonymized graph
     * @param entitiesOfInterest the deanoymized entities (gold standard) which are either in the additionalData graph or null if not, has to be in the same order as entitiesOfInterestAnonymized
     * @return f1 score
     */
    public static FMeasureEvaluationScore limesDeAnonymizationTest(QueryExecution anonymized, QueryExecution additionalData, List<String> entitiesOfInterestAnonymized, List<String> entitiesOfInterest){
        //TODO for each anonymized EOI check with limes against anonymized and additionalData
        List<FMeasuresCounts> values = new ArrayList<FMeasuresCounts>();
        List<String> deaonymizedEOIs = new ArrayList<String>();
        for(String eoi : entitiesOfInterestAnonymized){
            //TODO evaluate if limes score is high enough and if so add to results otherwise add null

        }
        //calculate F1-score, however not the best measure here, sufficient for now
        for(int i=0; i<deaonymizedEOIs.size();i++){
            String eoi = deaonymizedEOIs.get(i);
            if((eoi!= null && eoi.equals(entitiesOfInterest.get(i))) || (eoi==null && entitiesOfInterest.get(i)==null)){
                values.add(new FMeasuresCounts(1,0,0));
            }
            else{
                values.add(new FMeasuresCounts(0,1,1));
            }
        }
        return new FMeasureEvaluationScore(values);
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
        return anonymityCheck(entitiesOfInterest, quasiIdentifiable, new ArrayList<Property>(), graph).getkAnonymity();
    }

    public static AnonymityMeasure anonymityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
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
        //TODO fix l Diversity: needs to be per attribute
        List<StringDoublePair> l = calculateL(eqClasses);
        measure.setlDiversity(l);
        List<StringDoublePair> t = calculateT(eqClasses);
        //TODO fix t closeness calculation
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

    private static List<StringDoublePair> calculateT(List<EquivalentClass> eqClasses) {
        List<StringDoublePair> ret = new ArrayList<StringDoublePair>();
        EarthMoversDistance emd = new EarthMoversDistance();
        Collection<String> attributes = getAttributes(eqClasses);
        Map<String, List<AttributeCount>> features = getFeatures(eqClasses, attributes);
        for(String attr : features.keySet()) {
            double t =-1;
            for (EquivalentClass eqClass : eqClasses) {
                double[][] distributions = getDistributions(eqClass,features.get(attr), attr);
                if (t == -1) {
                    //TODO EMD doesn't make particular sense in RDF. VD might be better
                    t = VariationDistance.compute(distributions[0], distributions[1]);
                } else {
                    t = Math.min(t, VariationDistance.compute(distributions[0], distributions[1]));
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



    private static double[][] getDistributions(EquivalentClass eqClass, List<AttributeCount> features, String attr) {
        Collection<AttributeCount> classFeatures = eqClass.getSensibleValues(attr);
        double[] p = new double[features.size()];
        int valuesInClass = classFeatures.size();
        int size=0;
        for(AttributeCount aCount : features){
            size+=aCount.getCount();
        }
        for(AttributeCount classFeature : classFeatures) {
            p[features.indexOf(classFeature)] = classFeature.getCount()*1.0/valuesInClass;
        }
        double[] q = new double[features.size()];

        for(int i=0;i<features.size();i++){
            q[i]=features.get(i).getCount()*1.0/size;
        }
        return new double[][]{p, q};
    }

    private static Map<String, List<AttributeCount>> getFeatures(List<EquivalentClass> eqClasses, Collection<String> attributes) {
        Map<String, List<AttributeCount>> features = new HashMap<String, List<AttributeCount>>();
        for(EquivalentClass eqClass : eqClasses){
            Collection<AttributeCount> currentFeatures = eqClass.getSensibleValues();
            for(AttributeCount feat : currentFeatures){
                //check if attribute already exists
                features.putIfAbsent(feat.getProperty(), new ArrayList<AttributeCount>());
                int index = -1;
                if ((index = features.get(feat.getProperty()).indexOf(feat)) == -1) {
                    //Feature not yet seen, so just add it
                    features.get(feat.getProperty()).add(feat);
                } else {
                    features.get(feat.getProperty()).get(index).incrCount(feat.getCount());
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
                AttributeCount acCount = new AttributeCount(stmt.getPredicate().toString(), stmt.getObject().toString());
                int index=-1;
                if((index=outSet.indexOf(acCount))!=-1){
                    outSet.get(index).incrCount();
                }
                outSet.add(acCount);
            }
            List<Statement> objectParts = graph.listStatements(null, qIAttr, entity).toList();
            //TODO what if stmts are empty? should a null be added?
            for(Statement stmt : objectParts){
                AttributeCount acCount = new AttributeCount(stmt.getPredicate().toString(), stmt.getSubject().toString());
                int index=-1;
                if((index=outSet.indexOf(acCount))!=-1){
                    inSet.get(index).incrCount();
                }
                inSet.add(acCount);
            }
        }
    }

    public static List<StringDoublePair>  lDiversityCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
        return anonymityCheck(entitiesOfInterest, quasiIdentifiable, sensibleAttributes, graph).getlDiversity();
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
    @Deprecated
    public static  List<StringDoublePair> tClosenessCheck(Collection<Resource> entitiesOfInterest, List<Property> quasiIdentifiable, List<Property> sensibleAttributes, Model graph){
        return anonymityCheck(entitiesOfInterest, quasiIdentifiable, sensibleAttributes, graph).gettCloseness();
    }


}
