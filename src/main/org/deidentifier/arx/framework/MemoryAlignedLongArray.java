package org.deidentifier.arx.framework;

public class MemoryAlignedLongArray implements IMemory {

    private final long[] masksGet;
    private final long[] masksSet;
    private final long[] memory;
    private final int[]  offsets;
    private final int    rowSizeinLong;

    private final byte[] shifts;
    private final long   sizeinByte;

    public MemoryAlignedLongArray(final byte[] fieldSizes, final int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        // Field properties
        offsets = new int[fieldSizes.length];
        shifts = new byte[fieldSizes.length];
        masksGet = new long[fieldSizes.length];
        masksSet = new long[fieldSizes.length];

        int currentlyUsedBits = 0;
        int offsetInCurrentLong = 0;
        int curLong = 1;
        for (int field = 0; field < fieldSizes.length; field++) {
            final int currFieldSize = fieldSizes[field];

            // If it doesn't fit in current long, align
            if ((offsetInCurrentLong + currFieldSize) > 64) {
                currentlyUsedBits = curLong * 64;
                curLong++;
                offsetInCurrentLong = 0;
            }

            shifts[field] = (byte) (64 - offsetInCurrentLong - currFieldSize);

            long mask = 0L;
            for (int i = 0; i < currFieldSize; i++) {
                mask |= (1L << (64 - offsetInCurrentLong - i));
            }
            masksGet[field] = mask;
            masksSet[field] = ~mask;

            offsets[field] = (curLong - 1);
            currentlyUsedBits += currFieldSize;
            offsetInCurrentLong += currFieldSize;
        }
        rowSizeinLong = (int) Math.ceil(currentlyUsedBits / 64d);
        memory = new long[numRows * rowSizeinLong];
        sizeinByte = memory.length * 8;

    }

    @Override
    public boolean equals(final IMemory other, final int row) {
        // TODO: Potentially expensive cast here
        final long[] otherMemory = ((MemoryAlignedLongArray) other).memory;
        final int sIdx = row * rowSizeinLong;
        final int eIdx = sIdx + rowSizeinLong;
        for (int i = sIdx; i < eIdx; i++) {
            if (memory[i] != otherMemory[i]) { return false; }
        }
        return true;
    }

    @Override
    public int get(final int row, final int col) {
        final int idx = getIndex(row, col);
        final long result = ((memory[idx] & masksGet[col]) >>> shifts[col]);
        return (int) result;
    }

    @Override
    public long getByteSize() {
        return sizeinByte;
    }

    @Override
    public int hashcode(final int row) {
        // hashcode borrowed from Arrays.hashCode(long a[])

        int result = 1;

        final int sIdx = row * rowSizeinLong;
        final int eIdx = sIdx + rowSizeinLong;
        for (int i = sIdx; i < eIdx; i++) {
            final long element = memory[i];
            final int elementHash = (int) (element ^ (element >>> 32));
            result = (31 * result) + elementHash;
        }

        return result;

        // long result = 23;
        // final long startAdress = address + (row * rowSize);
        // final long endAdress = startAdress + rowSize;
        // for (long base = startAdress; base < endAdress; base += 8) {
        // result = (37L * result) + unsafe.getInt(base);
        // result = (37L * result) + unsafe.getInt(base + 4);
        // }
        // return (int) result;
    }

    @Override
    public void set(final int row, final int col, final int val) {
        final int idx = getIndex(row, col);
        long result = memory[idx];
        // clear previous bits
        result &= masksSet[col];

        // set new bits
        result |= (((long) val) << shifts[col]);

        memory[idx] = result;
    }

    private final int getIndex(final int row, final int col) {
        return (row * rowSizeinLong) + offsets[col];
    }
}
