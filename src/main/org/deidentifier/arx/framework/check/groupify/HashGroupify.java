/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.framework.check.groupify;

import org.deidentifier.arx.ARXConfiguration.Criterion;
import org.deidentifier.arx.framework.BitSetCompressed;
import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.Memory;

/**
 * A hash groupify operator.
 * 
 * @author Prasser, Kohlmayer
 */
public class HashGroupify implements IHashGroupify {

    /** The current outliers. */
    private int                    currentOutliers;

    /** Current number of elements. */
    private int                    elementCount;

    /** The entry array. */
    private HashGroupifyEntry[]    buckets;

    /** The first entry. */
    private HashGroupifyEntry      firstEntry;

    private final Configuration    config;

    /** The last entry. */
    private HashGroupifyEntry      lastEntry;

    /** Load factor. */
    private final float            loadFactor = 0.75f;

    /**
     * maximum number of elements that can be put in this map before having to
     * rehash.
     */
    private int                    threshold;

    private final double           logL;

    /** kanonK-anonymity config */
    private final int              k;

    /** l-diversity */
    private final double           ldivC;

    /** t-closeness */
    private final double           tcloseT;

    /** t-closeness */
    private final int              tCloseExtraStart;

    /** t-closeness */
    private final double[]         tCloseInitialDistribution;

    /** t-closeness */
    private final int[]            tCloseTree;

    /** t-closeness */
    private final int[]            tCloseEmpty;

    /** t-closeness */
    private final int              absoluteMaxOutliers;

    /** d-presence */
    private final BitSetCompressed researchSubset;

    /** d-presence */
    private final double           dMin;

    /** d-presence */
    private final double           dMax;
    
    /** The memory*/
    private final Memory       memory;

    /**
     * Constructs a new hash groupify for kanonK anonymity.
     * 
     * @param capacity
     *            the capacity
     * @param config
     *            the config
     */
    public HashGroupify(Memory memory, int capacity, final Configuration config) {

        // Set capacity
        capacity = HashTableUtil.calculateCapacity(capacity);
        this.elementCount = 0;
        this.buckets = new HashGroupifyEntry[capacity];
        this.threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
        this.memory = memory;

        this.config = config;
        this.currentOutliers = 0;
        this.absoluteMaxOutliers = config.getAbsoluteMaxOutliers();
        this.ldivC = config.getC();
        this.tcloseT = config.getT();
        this.dMax = config.getDmax();
        this.dMin = config.getDmin();

        // Set params
        switch (config.getCriterion()) {
        case T_CLOSENESS:
            logL = 0d;
            k = config.getK();
            switch (config.getTClosenessCriterion()) {
            case EMD_HIERARCHICAL:
                tCloseTree = config.getTClosenessTree();
                tCloseExtraStart = tCloseTree[1] + 3;
                tCloseEmpty = new int[tCloseTree[1]];
                tCloseInitialDistribution = null;
                researchSubset = null;
                break;
            case EMD_EQUAL:
                tCloseInitialDistribution = config.getInitialDistribution();
                tCloseTree = null;
                tCloseExtraStart = -1;
                tCloseEmpty = null;
                researchSubset = null;
                break;
            default:
                throw new RuntimeException("Invalid configuration!");
            }
            break;

        case L_DIVERSITY:
            logL = Math.log(config.getL()) / Math.log(2);
            tCloseEmpty = null;
            tCloseTree = null;
            tCloseInitialDistribution = null;
            tCloseExtraStart = -1;
            k = config.getL();
            researchSubset = null;
            break;

        case K_ANONYMITY:
            logL = 0d;
            tCloseEmpty = null;
            tCloseTree = null;
            tCloseInitialDistribution = null;
            tCloseExtraStart = -1;
            k = config.getK();
            researchSubset = null;
            break;

        case D_PRESENCE:
            logL = 0d;
            tCloseEmpty = null;
            tCloseTree = null;
            tCloseInitialDistribution = null;
            tCloseExtraStart = -1;
            researchSubset = config.getResearchSubset();
            k = config.getK();
            break;

        default:
            throw new RuntimeException("Invalid configuration!");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#add(int[],
     * int, int)
     */
    @Override
    public void add(final int line, final int value) {
        final int hash = memory.hashCode(line);
        addInternal(line, value, hash, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#addD(int[], int, int, int)
     */
    @Override
    public void addD(final int line, final int value, final int pvalue) {
        final int hash = memory.hashCode(line);
        if (researchSubset.get(line)) {
            addInternal(line, value, hash, pvalue);
        } else {
            addInternal(line, 0, hash, pvalue);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#add(int[],
     * int, int,
     * org.deidentifier.ARX.framework.check.distribution.Distribution)
     */
    @Override
    public void add(final int line, final int value, final Distribution frequencySet) {
        final int hash = memory.hashCode(line);
        final HashGroupifyEntry entry = addInternal(line, value, hash, 0);
        if (entry.distribution == null) {
            entry.distribution = frequencySet;
        } else {
            entry.distribution.merge(frequencySet);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#add(int[],
     * int, int, int)
     */
    @Override
    public void add(final int line, final int value, final int sensitiveValue) {
        final int hash = memory.hashCode(line);
        final HashGroupifyEntry entry = addInternal(line, value, hash, 0);
        if (entry.distribution == null) {
            entry.distribution = new Distribution();
        }
        entry.distribution.add(sensitiveValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#add(int[],
     * int, int, int[], int[])
     */
    @Override
    public void add(final int line, final int value, final int[] sensitiveElements, final int[] sensitiveFrequencies) {
        final int hash = memory.hashCode(line);
        final HashGroupifyEntry entry = addInternal(line, value, hash, 0);
        if (entry.distribution == null) {
            entry.distribution = new Distribution(sensitiveElements, sensitiveFrequencies);
        } else {
            entry.distribution.merge(sensitiveElements, sensitiveFrequencies);
        }
    }

    /**
     * Adds the internal.
     * 
     * @param key
     *            the key
     * @param line
     *            the line
     * @param value
     *            the value
     * @param hash
     *            the hash
     * @return the hash groupify entry
     */
    private final HashGroupifyEntry addInternal(final int line, final int value, final int hash, final int pvalue) {

        // Add entry
        int index = hash & (buckets.length - 1);
        HashGroupifyEntry entry = findEntry(line, index, hash);
        if (entry == null) {
            if (++elementCount > threshold) {
                rehash();
                index = hash & (buckets.length - 1);
            }
            entry = createEntry(index, hash, line);
        }
        entry.count += value;

        // indirectly check if we are in d-presence mode
        if (researchSubset != null) {
            entry.pcount += pvalue;
            if (value > 0) { // this is a research subset line
                // reset representant, necessary for rollup / history (otherwise researchSubset.get(line) would potentially be false)
                entry.representant = line;
            }
        }

        // Compute current outliers
        if (entry.count >= k) {
            if (!entry.isNotOutlier) {
                entry.isNotOutlier = true;
                currentOutliers -= (entry.count - value);
            }
        } else {
            currentOutliers += value;
        }

        return entry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#clear()
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            HashTableUtil.nullifyArray(buckets);
            currentOutliers = 0;
            firstEntry = null;
            lastEntry = null;
        }
    }

    /**
     * Creates a new entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param hash
     *            the hash
     * @param line
     *            the line
     * @return the hash groupify entry
     */
    private HashGroupifyEntry createEntry(final int index, final int hash, final int line) {
        final HashGroupifyEntry entry = new HashGroupifyEntry(line, hash);
        entry.next = buckets[index];
        buckets[index] = entry;
        if (firstEntry == null) {
            firstEntry = entry;
            lastEntry = entry;
        } else {
            lastEntry.nextOrdered = entry;
            lastEntry = entry;
        }
        return entry;
    }

    /**
     * Returns the according entry.
     * 
     * @param key
     *            the key
     * @param index
     *            the index
     * @param keyHash
     *            the key hash
     * @return the hash groupify entry
     */
    private final HashGroupifyEntry findEntry(final int line, final int index, final int keyHash) {
        HashGroupifyEntry m = buckets[index];
        while ((m != null) && ((m.hashcode != keyHash) || !memory.equals(line, m.representant))) {
            m = m.next;
        }
        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#getFirstEntry
     * ()
     */
    @Override
    public HashGroupifyEntry getFirstEntry() {
        return firstEntry;
    }

    @Override
    public int getGroupOutliersCount() {
        // Iterate over all groups
        // TODO: Could be more efficient
        int result = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final boolean anonymous = isAnonymous(entry);
            if (!anonymous) {
                result++;
            }
            entry = entry.nextOrdered;
        }
        return result;
    }

    @Override
    public int getTupleOutliersCount() {

        // Iterate over all groups
        // TODO: Could be more efficient
        int result = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final boolean anonymous = isAnonymous(entry);
            if (!anonymous) {
                result += entry.count;
            }
            entry = entry.nextOrdered;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.groupify.IHashGroupify#isAnonymous
     * ()
     */
    @Override
    public boolean isAnonymous() {

        if (config.getCriterion() == Criterion.K_ANONYMITY) { return isKAnonymous(); }

        // Check for k-anonymity
        if (!isKAnonymous()) { return false; }

        // Iterate over all groups
        currentOutliers = 0;
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final boolean anonymous = isAnonymous(entry);
            if (!anonymous) {
                currentOutliers += entry.count;
                if (currentOutliers > absoluteMaxOutliers) { return false; }
            }
            entry.isNotOutlier = anonymous;
            entry = entry.nextOrdered;
        }
        return true;
    }

    /**
     * Checks whether an equivalence class is anonymous
     * 
     * @param entry
     * @return
     */
    private boolean isAnonymous(final HashGroupifyEntry entry) {
        switch (config.getCriterion()) {
        case L_DIVERSITY:
            switch (config.getLDiversityCriterion()) {
            case RECURSIVE:
                return entry.distribution.isRecursiveCLDiverse(ldivC, k);
            case DISTINCT:
                return entry.distribution.isDistinctLDiverse(k);
            case ENTROPY:
                return entry.distribution.isEntropyLDiverse(logL, k);
            default:
                throw new UnsupportedOperationException(config.getLDiversityCriterion() + ": currently not supported");
            }
        case T_CLOSENESS:
            switch (config.getTClosenessCriterion()) {
            case EMD_EQUAL:
                return entry.distribution.isTCloseEqualDist(tcloseT, tCloseInitialDistribution);
            case EMD_HIERARCHICAL:
                System.arraycopy(tCloseEmpty, 0, tCloseTree, tCloseExtraStart, tCloseEmpty.length);
                return entry.distribution.isTCloseHierachical(tcloseT, tCloseTree);
            default:
                throw new UnsupportedOperationException(config.getLDiversityCriterion() + ": currently not supported");
            }
        case K_ANONYMITY:
            return entry.count >= k;

        case D_PRESENCE:
            // checks if the group is part of the research subset; should be the same as researchSubset.get(entry.representant) but more efficient
            if (entry.count > 0) {
                double dCurrent = (double) entry.count / (double) entry.pcount;
                // current_delta has to be between delta_min and delta_max
                return (dCurrent >= dMin) && (dCurrent <= dMax);
            } else {
                return true;
            }

        default:
            throw new UnsupportedOperationException(config.getLDiversityCriterion() + ": currently not supported");
        }
    }

    /**
     * Is the current transformation k-anonymous? CAUTION: Call before
     * isAnonymous()!
     * 
     * @return
     */
    @Override
    public boolean isKAnonymous() {
        if (currentOutliers > absoluteMaxOutliers) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void markOutliers(final Memory data) {
        for (int row = 0; row < data.getLength(); row++) {
            final int hash = data.hashCode(row);
            final int index = hash & (buckets.length - 1);
            HashGroupifyEntry m = buckets[index];
            while ((m != null) && ((m.hashcode != hash) || !data.equalsIgnoreOutliers(row, m.representant))) {
                m = m.next;
            }
            if (m == null) { throw new RuntimeException("Invalid state! Groupify the data before marking outliers!"); }
            if (!m.isNotOutlier) {
                data.set(row, 0, (data.get(row, 0) | Data.OUTLIER_MASK));
            }
        }
    }

    /**
     * Rehashes this operator.
     */
    private void rehash() {

        final int length = HashTableUtil.calculateCapacity((buckets.length == 0 ? 1 : buckets.length << 1));
        final HashGroupifyEntry[] newData = new HashGroupifyEntry[length];
        HashGroupifyEntry entry = firstEntry;
        while (entry != null) {
            final int index = entry.hashcode & (length - 1);
            entry.next = newData[index];
            newData[index] = entry;
            entry = entry.nextOrdered;
        }
        buckets = newData;
        threshold = HashTableUtil.calculateThreshold(buckets.length, loadFactor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.framework.check.groupify.IHashGroupify#size()
     */
    @Override
    public int size() {
        return elementCount;
    }
}
