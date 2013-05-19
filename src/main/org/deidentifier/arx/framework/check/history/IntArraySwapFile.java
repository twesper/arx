package org.deidentifier.arx.framework.check.history;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class IntArraySwapFile {

    class SnapshotLocationOnDisk {
        final long startOffset;
        final long stopOffset;

        public SnapshotLocationOnDisk(final long startOffset, final long stopOffset) {
            this.startOffset = startOffset;
            this.stopOffset = stopOffset;
        }

    }

    /** The filechannel to which the snapshots are getting persitsted */
    private FileChannel                                    channel;

    private RandomAccessFile                               file;
    /** The last offset of the swapfile */
    private long                                           lastOffset = 0;

    /** The map for the offsets for the swapped objects*/
    private final HashMap<Integer, SnapshotLocationOnDisk> locations;

    private File                                           temp;

    public IntArraySwapFile(String fileNamePrefix) {
        locations = new HashMap<Integer, SnapshotLocationOnDisk>();
        try {
            temp = File.createTempFile(fileNamePrefix, ".tmp", new File("."));
            temp.deleteOnExit();
            file = new RandomAccessFile(temp, "rw");
            channel = file.getChannel();
            channel.truncate(0);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        locations.clear();
        try {
            channel.truncate(0);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            channel.close();
            file.close();
            if (temp.exists()) {
                temp.delete();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public boolean containsSnapshot(final Integer swapedObjectID) {
        return locations.containsKey(swapedObjectID);
    }

    public int getObjectSize(final Integer swapedObjectID) {
        final SnapshotLocationOnDisk loc = locations.get(swapedObjectID);
        return (int) ((loc.stopOffset - loc.startOffset) / 4);
    }

    public int[] read(final Integer swapedObjectID) {
        final SnapshotLocationOnDisk location = locations.get(swapedObjectID);
        final int[] snapshot = new int[(int) ((location.stopOffset - location.startOffset) / 4)];
        try {
            final IntBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, location.startOffset, 4 * snapshot.length).asIntBuffer();
            buf.get(snapshot);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return snapshot;
    }

    public void write(final Integer swapedObjectID, final int[] snapshot) {

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
        locations.put(swapedObjectID, new SnapshotLocationOnDisk(startOffset, lastOffset));
    }

}
