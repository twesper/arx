package org.deidentifier.arx;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * This class provides a rudimentary interface to the internal ARX data structures
 * @author Fabian Prasser
 *
 */
public class ARXInterface {

    /** The data manager */
    private final DataManager      manager;
    /** The buffer */
    private final int[][]          buffer;
    /** The config */
    private final ARXConfiguration config;

    /**
     * Creates a new interface to the internal ARX data structures
     * @param data
     * @param config
     * @throws IOException
     */
    public ARXInterface(final Data data, ARXConfiguration config) throws IOException {

        // Check simplifying assumptions
        if (config.getMaxOutliers()>0d) {
            throw new UnsupportedOperationException("Outliers are not supported");
        }
        
        if (config.getCriteria().size()!=1) {
            throw new UnsupportedOperationException("Only exactly one criterion is supported");
        }
        
        if (!(config.getCriteria().iterator().next() instanceof KAnonymity)){
            throw new UnsupportedOperationException("Only the k-anonymity criterion is supported");
        }
        
        // Check
        checkBeforeEncoding(data, config);

        // Encode data
        DataHandle handle = data.getHandle();
        String[] header = ((DataHandleInput) handle).header;
        int[][] dataArray = ((DataHandleInput) handle).data;
        Dictionary dictionary = ((DataHandleInput) handle).dictionary;
        manager = new DataManager(header, dataArray, dictionary, handle.getDefinition(), config.getCriteria());
        
        // Initialize
        this.config = config;
        config.initialize(manager);

        // Check
        checkAfterEncoding(config, manager);
        
        // Build buffer
        int[][] array = getData();
        buffer = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            buffer[i] = new int[array[0].length];
        }
    }

    /**
     * Returns the input data array
     * @return
     */
    public int[][] getData(){
        return manager.getDataQI().getArray();
    }
    
    /**
     * Returns the output buffer
     * @return
     */
    public int[][] getBuffer(){
        return buffer;
    }
    
    /**
     * Returns the hierarchy for the attribute at the given index
     * @param index
     * @return
     */
    public int[][] getHierarchy(int index){
        return manager.getHierarchies()[index].getArray();
    }
    
    /**
     * Returns the name of the attribute at the given index
     * @param index
     * @return
     */
    public String getAttribute(int index){
        return manager.getDataQI().getHeader()[index];
    }
    
    /**
     * Returns the number of quasi-identifying attributes
     * @return
     */
    public int getNumAttributes(){
        return buffer[0].length;
    }
    
    /**
     * Returns the parameter 'k', as in k-anonymity
     * @return
     */
    public int getK(){
        return config.getMinimalGroupSize();
    }

    /**
     * Performs some sanity checks.
     * 
     * @param manager
     *            the manager
     */
    private void checkAfterEncoding(final ARXConfiguration config, final DataManager manager) {

        if (config.containsCriterion(KAnonymity.class)){
            KAnonymity c = config.getCriterion(KAnonymity.class);
            if ((c.getK() > manager.getDataQI().getDataLength()) || (c.getK() < 1)) { 
                throw new IllegalArgumentException("Parameter k (" + c.getK() + ") musst be positive and less or equal than the number of rows (" + manager.getDataQI().getDataLength()+")"); 
            }
        }
        if (config.containsCriterion(LDiversity.class)){
            for (LDiversity c : config.getCriteria(LDiversity.class)){
                if ((c.getL() > manager.getDataQI().getDataLength()) || (c.getL() < 1)) { 
                    throw new IllegalArgumentException("Parameter l (" + c.getL() + ") musst be positive and less or equal than the number of rows (" + manager.getDataQI().getDataLength()+")"); 
                }
            }
        }
        
        // Check whether all hierarchies are monotonic
        for (final GeneralizationHierarchy hierarchy : manager.getHierarchies()) {
            if (!hierarchy.isMonotonic()) { throw new IllegalArgumentException("The hierarchy for the attribute '" + hierarchy.getName() + "' is not monotonic!"); }
        }

        // check min and max sizes
        final int[] hierarchyHeights = manager.getHierachyHeights();
        final int[] minLevels = manager.getMinLevels();
        final int[] maxLevels = manager.getMaxLevels();

        for (int i = 0; i < hierarchyHeights.length; i++) {
            if (minLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               minLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (minLevels[i] < 0) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "' has to be positive!"); }
            if (maxLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid maximum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               maxLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (maxLevels[i] < minLevels[i]) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() +
                                                                                  "' has to be lower than or requal to the defined maximum!"); }
        }
    }

    /**
     * Performs some sanity checks.
     * 
     * @param data
     *            the allowed maximal number of outliers
     * @param config
     *            the configuration
     */
    private void checkBeforeEncoding(final Data data, final ARXConfiguration config) {


        // Lots of checks
        if (data == null) { throw new NullPointerException("Data cannot be null!"); }
        if (config.containsCriterion(LDiversity.class) ||
            config.containsCriterion(TCloseness.class)){
            if (data.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }
        }
        for (String attr : data.getDefinition().getSensitiveAttributes()){
            boolean found = false;
            for (LDiversity c : config.getCriteria(LDiversity.class)) {
                if (c.getAttribute().equals(attr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (TCloseness c : config.getCriteria(TCloseness.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No criterion defined for sensitive attribute: '"+attr+"'!");
            }
        }
        for (LDiversity c : config.getCriteria(LDiversity.class)) {
            if (data.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("L-Diversity criterion defined for non-sensitive attribute '"+c.getAttribute()+"'!");
            }
        }
        for (TCloseness c : config.getCriteria(TCloseness.class)) {
            if (data.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("T-Closeness criterion defined for non-sensitive attribute '"+c.getAttribute()+"'!");
            }
        }

        // Obtain handle
        final DataHandle handle = data.getHandle();
        
        // Check handle
        if (!(handle instanceof DataHandleInput)) { throw new IllegalArgumentException("Invalid data handle provided!"); }

        // Check if all defines are correct
        Set<String> attributes = new HashSet<String>();
        for (int i=0; i<handle.getNumColumns(); i++){
            attributes.add(handle.getAttributeName(i));
        }
        for (String attribute : data.getDefinition().getSensitiveAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Sensitive attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : data.getDefinition().getInsensitiveAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Insensitive attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : data.getDefinition().getIdentifyingAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Identifying attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : data.getDefinition().getQuasiIdentifyingAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Quasi-identifying attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        
        // Perform sanity checks
        Map<String, String[][]> hierarchies = handle.getDefinition().getHierarchies();
        if ((config.getMaxOutliers() < 0d) || (config.getMaxOutliers() >= 1d)) { throw new IllegalArgumentException("Suppression rate " + handle + "must be in [0,1["); }
        if (hierarchies.size() > 15) { throw new IllegalArgumentException("The curse of dimensionality strikes. Too many quasi-identifiers: " + hierarchies.size()); }
        if (hierarchies.size() == 0) { throw new IllegalArgumentException("You need to specify at least one quasi-identifier"); }
    }
}
