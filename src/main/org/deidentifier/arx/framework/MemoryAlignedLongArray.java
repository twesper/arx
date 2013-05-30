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
                mask |= (1L << (63 - offsetInCurrentLong - i));
            }
            masksGet[field] = mask;
            masksSet[field] = ~mask;

            // System.out.println("colum: " + field + "-getMask:" + Long.toBinaryString(masksGet[field]) + "-setMask:" + Long.toBinaryString(masksSet[field]) + "-Length:" + currFieldSize);

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
        // clear previous bits
        memory[idx] &= masksSet[col];
        // set new bits
        memory[idx] |= (((long) val) << shifts[col]);
    }

    private final int getIndex(final int row, final int col) {
        return (row * rowSizeinLong) + offsets[col];
    }

    public static void main(String[] args) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // byte[] sizes = { 2 };
        byte[] sizes = { 2, 3 };

        int[][] data = new int[2][sizes.length];
        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < sizes.length; col++) {
                data[row][col] = col;
            }
        }

        // Check that everything works
        MemoryAlignedIntArray int0 = new MemoryAlignedIntArray(sizes, data.length);
        MemoryAlignedLongArray long0 = new MemoryAlignedLongArray(sizes, data.length);
        MemoryUnsafe unsafe0 = new MemoryUnsafe(sizes, data.length);
        for (int row = 0; row < data.length; row++) {
            for (int col = 0; col < sizes.length; col++) {
                int0.set(row, col, data[row][col]);
                long0.set(row, col, data[row][col]);
                unsafe0.set(col, data[row][col]);
                if (int0.get(row, col) != long0.get(row, col)) { throw new RuntimeException("Mismatch!"); }
                if (int0.get(row, col) != unsafe0.get(col)) { throw new RuntimeException("Mismatch!"); }
            }
            unsafe0.base += unsafe0.rowSize;
        }

    }
}
