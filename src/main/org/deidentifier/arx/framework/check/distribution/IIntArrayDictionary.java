package org.deidentifier.arx.framework.check.distribution;

public interface IIntArrayDictionary {

    /**
     * Clears the dictionary.
     */
    public abstract void clear();

    /**
     * Removes a element from the dictionary
     * 
     * @param element
     */
    public abstract void decrementRefCount(int index);

    /**
     * Returns the according entry
     * 
     * @param index
     * @return
     */
    public abstract int[] get(int index);

    /**
     * Probes the dictionary and either inserts a new entry index or returns the
     * corresponding entry index.
     * 
     * @param key
     *            the key
     */
    public abstract int probe(int[] key);

    /**
     * Returns the element count of the dictionary
     * 
     * @return the int
     */
    public abstract int size();

}
