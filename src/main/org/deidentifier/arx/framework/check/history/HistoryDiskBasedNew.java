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

package org.deidentifier.arx.framework.check.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * The Class History.
 * 
 * @author Prasser, Kohlmayer
 */
public class HistoryDiskBasedNew implements IHistory {

    // class NodeToID {
    // final Node node;
    //
    // final int snapshotID;
    //
    // public NodeToID(final Node node, final int snapshotID) {
    // this.snapshotID = snapshotID;
    // this.node = node;
    // }
    // }

    class SnapshotLocationOnDisk {
        final long startOffset;
        final long stopOffset;

        public SnapshotLocationOnDisk(final long startOffset, final long stopOffset) {
            this.startOffset = startOffset;
            this.stopOffset = stopOffset;
        }

    }

    /** The actual buffer. */
    private final MRUCache<Node>                           cache;

    /** The filechannel to which the snapshots are getting persitsted */
    private FileChannel                                    channel;

    /** Current config */
    private final Configuration                            config;

    /** The dictionary for frequencies of the distributions */
    private final IntArrayDictionary                       dictionarySensFreq;

    // TODO: also the Dictionary should be diskbased!!
    /** The dictionary for values of the distributions */
    private final IntArrayDictionary                       dictionarySensValue;

    // last offset
    private long                                           lastOffset = 0;

    // file positions
    private final HashMap<Integer, SnapshotLocationOnDisk> locations;

    /** Maximal number of entries. */
    private final int                                      maxSize;

    /** A map with all nodes for which snapshots exists. */
    private HashMap<Node, Integer>                         nodeToID   = null;

    /** A map from snapshotID to snapshots. */
    private final HashMap<Integer, int[]>                  nodeToSnapshot;

    /** The node backing the last returned snapshot */
    private Node                                           resultNode;
    /** The maximal size for a new snapshot. */
    private final long                                     snapshotSizeDataset;

    /** The minimum required reduction of a snapshot before a new snaphsot is created. */
    private final double                                   snapshotSizeSnapshot;

    /**
     * Creates a new history.
     * 
     * @param rowCount
     *            the row count
     * @param maxSize
     *            the max size
     * @param snapshotSizeDataset
     *            the snapshotSizeDataset
     */
    public HistoryDiskBasedNew(final int rowCount,
                               final int maxSize,
                               final double snapshotSizeDataset,
                               final double snapshotSizeSnapshot,
                               final Configuration config,
                               final IntArrayDictionary dictionarySensValue,
                               final IntArrayDictionary dictionarySensFreq) {
        this.snapshotSizeDataset = (long) (rowCount * snapshotSizeDataset);
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
        nodeToID = new HashMap<Node, Integer>();
        this.dictionarySensFreq = dictionarySensFreq;
        this.dictionarySensValue = dictionarySensValue;
        this.config = config;

        // TODO: fixed size make it configurable!
        this.maxSize = 10;
        nodeToSnapshot = new HashMap<Integer, int[]>();
        cache = new MRUCache<Node>(maxSize);
        locations = new HashMap<Integer, SnapshotLocationOnDisk>();
        try {
            final File temp = File.createTempFile("arxSnapShots", ".tmp", new File("."));
            temp.deleteOnExit();
            channel = new RandomAccessFile(temp, "rw").getChannel();
            channel.truncate(0);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a snapshot.
     * 
     * @param node
     *            the node
     * @return the int[]
     */
    @Override
    public int[] get(final Node node) {

        int rDataLength = Integer.MAX_VALUE;
        Integer rsnapshotID = null;
        Node rNode = null;

        // Iterate over nodes with snapshots
        final Iterator<Entry<Node, Integer>> it = nodeToID.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<Node, Integer> pairs = it.next();
            final Node cNode = pairs.getKey();

            // check if level of node is lower than current node, otherwise it couldnt be used as snapshot at all
            if (cNode.getLevel() < node.getLevel()) {
                final Integer cSnapshotID = pairs.getValue();
                final int cSnapshotLength = getSnapShotLength(cSnapshotID);
                if ((rNode == null) || (cSnapshotLength < rDataLength)) {
                    boolean synergetic = true;
                    for (int i = 0; i < cNode.getTransformation().length; i++) {
                        if (node.getTransformation()[i] < cNode.getTransformation()[i]) {
                            synergetic = false;
                            break;
                        }
                    }
                    if (synergetic) {
                        rNode = cNode;
                        rsnapshotID = cSnapshotID;
                        rDataLength = cSnapshotLength;
                    }
                }
            }
        }

        // no snapshot found
        if (rNode == null) { return null; }

        resultNode = rNode;
        int[] rData = nodeToSnapshot.get(rsnapshotID);
        if (rData == null) {
            rData = readFromDiskAndCache(rNode, rsnapshotID);
        }
        return rData;
    }

    @Override
    public IntArrayDictionary getDictionarySensFreq() {
        return dictionarySensFreq;
    }

    @Override
    public IntArrayDictionary getDictionarySensValue() {
        return dictionarySensValue;
    }

    /**
     * Returns the node backing the last returned snapshot
     * 
     * @return
     */
    @Override
    public Node getNode() {
        return resultNode;
    }

    /**
     * Clears the history.
     */
    @Override
    public void reset() {
        nodeToID.clear();
        nodeToSnapshot.clear();
        cache.clear();
        locations.clear();
        try {
            channel.truncate(0);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int size() {
        // returns the number of all snapshots
        return nodeToID.size();
    }

    /**
     * Stores a snapshot.
     * 
     * @param node
     *            the node
     * @param g
     *            the g
     */
    @Override
    public boolean store(final Node node, final IHashGroupify g, final int[] usedSnapshot) {

        if ((node.isAnonymous() || (g.size() > snapshotSizeDataset) || canPrune(node))) { return false; }

        // Store only if significantly smaller
        if (usedSnapshot != null) {
            final double percentSize = (g.size() / ((double) usedSnapshot.length / config.getCriterionSpecificSnapshotLength()));
            if (percentSize > snapshotSizeSnapshot) { return false; }
        }

        // Create the snapshot
        int[] data = null;

        switch (config.getCriterion()) {
        case K_ANONYMITY:
            data = createSnapshot(g);
            break;

        case L_DIVERSITY:
        case T_CLOSENESS:

            data = createSnapshotFrequencySet(g);
            break;

        case D_PRESENCE:
            data = createSnapshotDPresence(g);
            break;

        default:
            throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
        }

        // if cache size is to large purge
        if (cache.size() >= maxSize) {
            purgeCache();
        }
        final Integer newSnapshotID = nodeToID.size() + 1;
        nodeToID.put(node, newSnapshotID);
        cache(node, newSnapshotID, data);
        return true;
    }

    private void cache(final Node node, final Integer snapshotID, final int[] data) {
        cache.append(node);
        nodeToSnapshot.put(snapshotID, data);
    }

    /**
     * Can a node be pruned.
     * 
     * @param node
     *            the node
     * @return true, if successful
     */
    private final boolean canPrune(final Node node) {
        boolean prune = true;
        switch (config.getHistoryPruning()) {
        case ANONYMOUS:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isAnonymous()) {
                    prune = false;
                    break;
                }
            }
            break;
        case CHECKED:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isChecked()) {
                    prune = false;
                    break;
                }
            }
            break;
        case K_ANONYMOUS:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isKAnonymous()) {
                    prune = false;
                    break;
                }
            }
            break;
        }
        return prune;
    }

    /**
     * Creates the snapshot.
     * 
     * @param g
     *            the g
     * @return the int[]
     */
    private final int[] createSnapshot(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 2];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            index += 2;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * Creates the snapshot for d presence including the subset count.
     * 
     * @param g
     *            the g
     * @return the int[]
     */
    private final int[] createSnapshotDPresence(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 3];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            data[index + 1] = m.pcount;
            index += 3;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * Creates the snapshot including the dictionary encoded frequency set
     * 
     * @param g
     * @return
     */
    private final int[] createSnapshotFrequencySet(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 4];
        int index = 0;

        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {

            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            final Distribution fSet = m.distribution;
            fSet.pack();
            data[index + 2] = dictionarySensValue.probe(fSet.getElements());
            data[index + 3] = dictionarySensFreq.probe(fSet.getFrequency());
            index += 4;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * returns the length of the snapshot represented by this number
     * @param snapshotNumber
     * @return
     */
    private int getSnapShotLength(final Integer snapshotNumber) {
        final int[] snapshot = nodeToSnapshot.get(snapshotNumber);
        if (snapshot != null) {
            return snapshot.length;
        } else {
            final SnapshotLocationOnDisk loc = locations.get(snapshotNumber);
            return (int) ((loc.stopOffset - loc.startOffset) / 4);
        }
    }

    /**
     * Remove least recently used from cache and index.
     */
    private final void purgeCache() {
        int purged = 0;

        // Purge prunable nodes
        final Iterator<Node> it = cache.iterator();
        while (it.hasNext()) {
            final Node node = it.next();
            if (canPrune(node)) {
                purged++;
                it.remove();
                removeHistoryEntry(node, false);
            }
        }

        // Purge LRU
        if (purged == 0) {
            final Node node = cache.removeHead();
            removeHistoryEntry(node, true);
        }
    }

    private int[] readFromDiskAndCache(final Node node, final Integer snapshotNumber) {

        final SnapshotLocationOnDisk location = locations.get(snapshotNumber);
        final int[] snapshot = new int[(int) ((location.stopOffset - location.startOffset) / 4)];
        try {
            final IntBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, location.startOffset, 4 * snapshot.length).asIntBuffer();
            buf.get(snapshot);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // if cache size is to large purge
        if (cache.size() >= maxSize) {
            purgeCache();
        }

        cache(node, snapshotNumber, snapshot);
        return snapshot;
    }

    private final void removeHistoryEntry(final Node node, boolean persist) {

        final Integer snapshotID = nodeToID.get(node);
        final int[] snapshot = nodeToSnapshot.remove(snapshotID);

        // persist snapshots on disk if not already done
        if (persist & !locations.containsKey(snapshotID)) {
            storeToDisk(snapshotID, snapshot);
        }

        // dictionary entries could not be purged, as they maybe needed, if snapshots were read from disk
        // // in case of l-diversity/t-closeness clear freqdictionary
        // switch (config.getCriterion()) {
        // case L_DIVERSITY:
        // case T_CLOSENESS:
        // for (int i = 0; i < snapshot.length; i += 4) {
        // dictionarySensValue.decrementRefCount(snapshot[i + 2]);
        // dictionarySensFreq.decrementRefCount(snapshot[i + 3]);
        // }
        // break;
        // default:
        // break;
        // }
    }

    private final void storeToDisk(final Integer snapshotNumber, final int[] snapshot) {
        final long startOffset = lastOffset;
        try {
            final MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, startOffset, 4 * snapshot.length);
            for (final int j : snapshot) {
                buf.putInt(j);
            }
            // buf.force();
            lastOffset = channel.position();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // save location of snapshot
        locations.put(snapshotNumber, new SnapshotLocationOnDisk(startOffset, lastOffset));

    }
}
