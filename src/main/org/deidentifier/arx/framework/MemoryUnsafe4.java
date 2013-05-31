package org.deidentifier.arx.framework;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Implementation of memory with Unsafe. 
 * Fields are either 1 (byte), 2 (char) or 4 (int) bytes in size. 
 * Each row is 8 byte (long) aligned, to allow for fast equals() and rather fast hashcode().
 *  
 * @author Prasser, Kohlmayer
 */
public class MemoryUnsafe4 implements IMemory {

    private final long   baseAddress;   // In bytes
    // bit positions --> offset,getMask, clearMask,shift
    private final long[] bitPos;        // all aligned in one long array to enable efficient caching in one cache line
    private final long   rowSizeInBytes; // In bytes

    private final long   size;          // In bytes
    private final Unsafe unsafe;        // The unsafe

    public MemoryUnsafe4(final byte[] fieldSizes, final int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        // Access unsafe
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);

        // Field properties
        bitPos = new long[fieldSizes.length * 4];

        int currentlyUsedBits = 0;
        int offsetInCurrentLong = 0;
        int curLong = 1;
        int idx = 0;
        for (int field = 0; field < fieldSizes.length; field++) {
            final int currFieldSize = fieldSizes[field];

            // If it doesn't fit in current long, align
            if ((currentlyUsedBits + currFieldSize) > (curLong * 64)) {
                currentlyUsedBits = curLong * 64;
                curLong++;
                offsetInCurrentLong = 0;
            }

            final byte curShift = (byte) (64 - offsetInCurrentLong - currFieldSize);

            long mask = 0L;
            for (int i = 0; i < currFieldSize; i++) {
                mask |= (1L << (63 - offsetInCurrentLong - i));
            }

            // offset
            bitPos[idx] = (curLong - 1);
            // getMask
            bitPos[idx + 1] = mask;
            // clearmask
            bitPos[idx + 2] = mask;
            // shift
            bitPos[idx + 3] = curShift;
            idx += 4;

            currentlyUsedBits += currFieldSize;
            offsetInCurrentLong += currFieldSize;
        }

        // make multiple of 8
        rowSizeInBytes = curLong * 8;

        // Allocate
        size = rowSizeInBytes * numRows;
        baseAddress = unsafe.allocateMemory(size);
    }

    @Override
    public boolean equals(final IMemory other, final int row) {
        final Unsafe o = ((MemoryUnsafe4) other).unsafe;
        final long startAdress = baseAddress + (row * rowSizeInBytes);
        final long endAdress = startAdress + rowSizeInBytes;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getAddress(base) != o.getAddress(base)) { return false; }
        }
        return true;
    }

    @Override
    public int get(final int row, final int col) {
        final int idx = (col << 2);
        final long base = baseAddress + (row * rowSizeInBytes) + bitPos[idx];
        long result = unsafe.getAddress(base);
        result = ((result & bitPos[idx + 1]) >>> bitPos[idx + 3]);
        return (int) result;
    }

    @Override
    public long getByteSize() {
        return size;
    }

    @Override
    public int hashcode(final int row) {
        // hashcode borrowed from Arrays.hashCode(long a[])
        int result = 1;
        final long startAdress = baseAddress + (row * rowSizeInBytes);
        final long endAdress = startAdress + rowSizeInBytes;
        for (long base = startAdress; base < endAdress; base += 8) {
            final long element = unsafe.getAddress(base);
            final int elementHash = (int) (element ^ (element >>> 32));
            result = (31 * result) + elementHash;
        }
        return result;
    }

    @Override
    public void set(final int row, final int col, final int val) {
        final int idx = (col << 2);
        final long base = baseAddress + (row * rowSizeInBytes) + bitPos[idx];
        long result = unsafe.getAddress(base);

        // clear previous bits
        result &= bitPos[idx + 2];
        // set new bits
        result |= (((long) val) << bitPos[idx + 3]);

        unsafe.putAddress(base, result);
    }
}
