package org.deidentifier.arx.framework.check.history;

import org.deidentifier.arx.framework.check.distribution.IIntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;

public interface IHistory {

    /**
     * Retrieves a snapshot.
     * 
     * @param node
     *            the node
     * @return the int[]
     */
    public abstract int[] get(Node node);

    public abstract IIntArrayDictionary getDictionarySensFreq();

    public abstract IIntArrayDictionary getDictionarySensValue();

    /**
     * Returns the node backing the last returned snapshot
     * 
     * @return
     */
    public abstract Node getNode();

    /**
     * Clears the history.
     */
    public abstract void reset();

    public abstract int size();

    /**
     * Stores a snapshot.
     * 
     * @param node
     *            the node
     * @param g
     *            the g
     */
    public abstract boolean store(Node node, IHashGroupify g, int[] usedSnapshot);

}
