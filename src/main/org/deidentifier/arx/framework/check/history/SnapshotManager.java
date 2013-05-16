package org.deidentifier.arx.framework.check.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SnapshotManager {

    // cache
    private HashMap<Integer, int[]> cache        = null;
    private Queue<Integer>          queue        = null;
    private static final int        CACHE_SIZE   = 10;

    // file positions
    private HashMap<Integer, Long>  startOffsets = null;

    // last offset
    private long                    lastOffset   = 0;

    // private static final String snaphotTempFile = "snapshots.tmp";
    private FileChannel             channel      = null;

    @SuppressWarnings("resource")
    public SnapshotManager() {
        cache = new HashMap<Integer, int[]>();
        queue = new LinkedList<Integer>();
        startOffsets = new HashMap<Integer, Long>();
        try {
            File temp = File.createTempFile("arxSnapShots", ".tmp", new File("."));
            temp.deleteOnExit();
            channel = new RandomAccessFile(temp, "rw").getChannel();
            channel.truncate(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] get(int snapshotNumber) {
        if (cache.containsKey(snapshotNumber)) {
            return cache.get(snapshotNumber);
        } else {
            return readFromDiskAndCache(snapshotNumber);
        }
    }

    public void store(int snapshotNumber, int[] snapshot) {
        // remember the start position
        startOffsets.put(snapshotNumber, lastOffset);

        try {
            MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, lastOffset, 4 * snapshot.length);
            for (int j : snapshot) {
                buf.putInt(j);
            }
            // buf.force();
            lastOffset = channel.position();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] readFromDiskAndCache(int snapshotNumber) {

        if (queue.size() > CACHE_SIZE) {
            removeLIFO();
        }

        long startOffset = startOffsets.get(snapshotNumber);

        long stopOffset = lastOffset;
        if (startOffsets.containsKey(snapshotNumber + 1)) {
            stopOffset = startOffsets.get(snapshotNumber + 1);
        }

        int[] snapshot = new int[(int) ((stopOffset - startOffset) / 4)];

        try {
            IntBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, startOffset, 4 * snapshot.length).asIntBuffer();
            buf.get(snapshot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cache.put(snapshotNumber, snapshot);
        queue.add(snapshotNumber);

        return snapshot;
    }

    private final void removeLIFO() {
        final Integer ss = queue.poll();
        cache.remove(ss);
    }

    public int getSnapShotLength(int snapshotNumber) {
        long startOffset = startOffsets.get(snapshotNumber);
        long stopOffset = lastOffset;
        if (startOffsets.containsKey(snapshotNumber + 1)) {
            stopOffset = startOffsets.get(snapshotNumber + 1);
        }

        return (int) ((stopOffset - startOffset) / 4);
    }

    public void clear() {
        try {
            channel.truncate(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.clear();
        startOffsets.clear();
        queue.clear();
    }

}
