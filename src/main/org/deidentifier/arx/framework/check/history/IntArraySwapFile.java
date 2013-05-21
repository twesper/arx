package org.deidentifier.arx.framework.check.history;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import org.xerial.snappy.Snappy;

public class IntArraySwapFile {

    private static final boolean USE_COMPRESSION = false;

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
        int[] snapshot = null;
        try {

            if (USE_COMPRESSION) {
                final int size = (int) (location.stopOffset - location.startOffset);
                final byte[] compressed = new byte[size];
                // final ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, location.startOffset, size);
                // buf.get(compressed);
                file.seek(location.startOffset);
                file.readFully(compressed);
                snapshot = Snappy.uncompressIntArray(compressed);
            } else {
                snapshot = new int[(int) ((location.stopOffset - location.startOffset) / 4)];
                final IntBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, location.startOffset, 4 * snapshot.length).asIntBuffer();
                buf.get(snapshot);

                // final byte[] buffer = new byte[(int) (location.stopOffset - location.startOffset)];
                // file.seek(location.startOffset);
                // file.readFully(buffer);
                // snapshot = convert(buffer);

            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return snapshot;
    }

    public final int[] convert(byte buf[]) {
        int intArr[] = new int[buf.length / 4];
        int offset = 0;
        for (int i = 0; i < intArr.length; i++) {
            intArr[i] = (buf[3 + offset] & 0xFF) | ((buf[2 + offset] & 0xFF) << 8) | ((buf[1 + offset] & 0xFF) << 16) | ((buf[0 + offset] & 0xFF) << 24);
            offset += 4;
        }
        return intArr;
    }

    public void write(final Integer swapedObjectID, final int[] snapshot) {

        final long startOffset = lastOffset;
        try {

            if (USE_COMPRESSION) {
                final byte[] compressed = Snappy.compress(snapshot);
                // final MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, startOffset, compressed.length);
                // buf.put(compressed);
                file.seek(startOffset);
                file.write(compressed);
                lastOffset = file.getFilePointer();
            } else {
                final IntBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, startOffset, 4 * snapshot.length).asIntBuffer();
                buf.put(snapshot);
                lastOffset = channel.position();
                // file.seek(startOffset);
                // for (int i : snapshot) {
                // file.write((byte) (i >> 24));
                // file.write((byte) (i >> 16));
                // file.write((byte) (i >> 8));
                // file.write((byte) (i));
                // // file.writeInt(i);
                // }
                // lastOffset = file.getFilePointer();

            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // save location of snapshot
        locations.put(swapedObjectID, new SnapshotLocationOnDisk(startOffset, lastOffset));
    }
}
