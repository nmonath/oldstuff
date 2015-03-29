package edu.umass.cs.data_fusion.algorithm;

 
import edu.umass.cs.data_fusion.data_structures.*;
import edu.umass.cs.data_fusion.util.Functions;

import java.util.*;

public class TruthFinder extends Algorithm{
    
    private double initialTrustworthiness;
    private double delta;
    private double rho;
    private double gamma;

    final private double MAX_ITERATIONS = 10;

    private Source source = new Source(this.getName());
    
    public TruthFinder() {
        super("TruthFinder");
        this.initialTrustworthiness = 0.8;
        this.delta = 0.9;
        this.rho = 0.5;
        this.gamma = 0.1;
    }
    
    public TruthFinder(double initialTrustworthiness, double delta, double rho, double gamma) {
        super("TruthFinder");
        this.initialTrustworthiness = initialTrustworthiness;
        this.delta = delta;
        this.rho = rho;
        this.gamma = gamma;
    }

    public ArrayList<Result> execute(RecordCollection collection) {

        /*
        Each unique <Entity, AttributeValue> pair is assigned a confidence. Note that if two sources
        present the same value for an attribute this pair will have the same confidence for both sources
        and so we only store it once. 
         */
        HashMap<Entity,HashMap<String,HashMap<Attribute,Double>>> confidence = new HashMap<Entity, HashMap<String,HashMap<Attribute,Double>>>();
        
        /*
        The algorithm works iteratively checking for convergence by measuring the cosine similarity 
        between the trustworthiness scores (stored in an arbitrarily ordered vector) 
        of the current round and the previous round
         */
        HashMap<Source, Double> previousTrustworthiness = new HashMap<Source, Double>();


        /*
        The sources which provide the data items  
         */
        Set<Source> sources = collection.getSources();
        
        /*
         Initialize source trustworthiness
         */
        for (Source s : sources) {
            previousTrustworthiness.put(s,initialTrustworthiness);
        }

        /*
        If the algorithm has converged
         */
        boolean converged = false;
        
        /*
        The number of iterators completed so far
         */
        int numIterations = 0;
        
        /*
        Repeat this process until convergence or the number of iterations exceeds the limit
         */
        while (!converged && numIterations < MAX_ITERATIONS) {
            
            
            /*
            The algorithm operates over pairs of <Entity, Attribute>
             */
            for (Entity entity : collection.getEntities()) {
                
                /*
                Reset the confidence values for this entity; confidence values are calculated at each iteration.
                 */
                confidence.put(entity, new HashMap<String, HashMap<Attribute, Double>>());
                
                /*
                All of the attribute names of the entities attributes
                 */
                Set<String> attributeNames = collection.getAttributes(entity);
                
                /*
                All of the records corresponding to this entity
                 */
                ArrayList<Record> recordsForEntity = collection.getRecords(entity);
                
                
                /*
                Iterate over each attributeName for this entity
                 */
                for (String attributeName : attributeNames) {
                    
                    /*
                    Reset the confidence values for this attribute
                     */
                    confidence.get(entity).put(attributeName, new HashMap<Attribute, Double>());
                    
                    /*
                    A hash map storing the sigma values for each attribute value as described in the paper
                     */
                    HashMap<Attribute, Double> sigma = new HashMap<Attribute, Double>();
                    
                    /*
                    All of the different values for a given named attribute
                     */
                    Set<Attribute> valuesForGivenAttribute = valuesForAttribute(recordsForEntity, attributeName);
                    
                    /*
                    Calculated the sigma for each value 
                     */
                    for (Attribute v : valuesForGivenAttribute) {
                        // sigma_v <- - sum_{s in SourcesProvidingV} ln(1-Trustworthiness(s)) 
                        Set<Source> sourcesWithValue = sourcesWithValue(recordsForEntity, v);
                        double sigma_v = 0.0;
                        for (Source s : sourcesWithValue) {
                            sigma_v += -Math.log(1 - previousTrustworthiness.get(s));
                        }
                        sigma.put(v, sigma_v);
                    }
                    
                    /*
                    Calculate the sigmaStar and confidence for each attribute value
                     */
                    for (Attribute v : valuesForGivenAttribute) {
                        // sigmaStar_v <- sigma_v + rho * (sum_{v' in valuesForGivenAttribute, v' != v} sigma_v' * (similarity(v,v')))
                        double sigmaStar_v = sigma.get(v);
                        for (Attribute vPrime : valuesForGivenAttribute) {
                            if (!v.equals(vPrime)) {
                                sigmaStar_v += rho * sigma.get(vPrime) * similarity(v, vPrime);
                            }
                        }
                        // confidence_v <- 1 / (1 + exp(-gamma*sigmaStar_v)
                        double confidence_v = 1 / (1 + Math.exp(-gamma * sigmaStar_v));
                        confidence.get(entity).get(attributeName).put(v, confidence_v);
                    }
                }
            }
            /*
            Calculate the trustworthiness values of the sources for this round.
             */
            HashMap<Source, Double> currentTrustworthiness = new HashMap<Source, Double>();
            for (Source s : sources) {
                // trustworthiness(s) <- sum_{v in ValuesGivenBySource} confidence(v)/NumberOfValuesGivenBySource
                ArrayList<Record> recordsForSource = collection.getRecords(s);
                int numValuesForSource = 0;
                double trustworthiness = 0.0;
                for (Record r : recordsForSource) {
                    for (Attribute a: r.getAttributes().values()) {
                        trustworthiness += confidence.get(r.getEntity()).get(a.getName()).get(a);
                        numValuesForSource += 1;
                    }
                }
                currentTrustworthiness.put(s,trustworthiness/numValuesForSource);
            }
            /*
            Check for convergence
             */
            converged = converged(previousTrustworthiness,currentTrustworthiness,delta);
            previousTrustworthiness = currentTrustworthiness;
            numIterations += 1;
        }

        /*
        Assign a final value to each attribute of each entity  
         */
        Set<Entity> entities = collection.getEntities();
        ArrayList<Result> results = new ArrayList<Result>(entities.size());
        for (Entity entity: entities) {
            Result res = new Result(this.source, entity);
            Set<String> attributeNames = collection.getAttributes(entity);
            // The value for each attribute it is the one with the highest confidence
            for (String attributeName : attributeNames) {
                double max = Double.MIN_VALUE;
                Attribute bestAttr = null;
                for (Attribute a : confidence.get(entity).get(attributeName).keySet()) {
                    double conf = confidence.get(entity).get(attributeName).get(a);
                    if (conf > max) {
                        max = conf;
                        bestAttr = a;
                    }
                }
                res.addAttribute(bestAttr);
            }
            results.add(res);
        }
        return results;
    }
    
    
    public RecordCollection convert(ArrayList<Result> results) {
        ArrayList<Record> rec = new ArrayList<Record>(results.size());
        for (Result r : results) {
            rec.add(new Record(r.getSource(),r.getEntity(),r.getAttributes()));
        }
        return new RecordCollection(rec);
    }
    
    private Set<Source> sourcesWithValue(ArrayList<Record> records, Attribute value)  {
        Set<Source> sourcesWithValue = new HashSet<Source>();
        String attrName = value.getName();
        for (Record r : records) {
            if (r.hasAttribute(attrName))
                if (r.getAttribute(attrName).equals(value))
                    sourcesWithValue.add(r.getSource());
        }
        return sourcesWithValue;
    }
    
    private Set<Attribute> valuesForAttribute(ArrayList<Record> records, String attributeName) {
        Set<Attribute> valuesForAttribute = new HashSet<Attribute>();
        for (Record r: records) {
            Attribute attr = r.getAttribute(attributeName);
            if (attr != null)
                valuesForAttribute.add(attr);
        }
        return valuesForAttribute;
    }
        
    
    //TODO: How is this defined???
    public double similarity(Attribute attr1, Attribute attr2) {
        if (attr1 instanceof FloatAttribute && attr2 instanceof FloatAttribute)
            return -((FloatAttribute) attr1).getFloatValue() - ((FloatAttribute) attr2).getFloatValue();
        else if (attr1 instanceof StringAttribute && attr2 instanceof StringAttribute)
            return -1.0*Functions.editDistance(((StringAttribute) attr1).getStringValue(),((StringAttribute) attr2).getStringValue());
        return 0.0;
    }
    
    public boolean converged(HashMap<Source,Double> previous, HashMap<Source,Double> current, double delta) {
        assert previous.keySet().containsAll(current.keySet());
        double[] previousVec = new double[previous.size()];
        double[] currentVec = new double[previous.size()];
        int i = 0;
        for (Source s: previous.keySet()) {
            previousVec[i] = previous.get(s);
            currentVec[i] = current.get(s);
            i += 1;
        }
        return Functions.cosine(previousVec, currentVec) > delta;
    }

    
}