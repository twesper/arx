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
public class MemoryUnsafe3 implements IMemory {

    private final long   address;    // In bytes
    private final long[] clearMasks;
    private final long[] fieldOffset; // In bytes
    private final long[] getMasks;
    private final long   rowSize;    // In bytes
    private final byte[] shifts;
    private final long   size;       // In bytes
    private final Unsafe unsafe;     // The unsafe

    public MemoryUnsafe3(final byte[] fieldSizes, final int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        // Access unsafe
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);

        // Field properties
        fieldOffset = new long[fieldSizes.length];
        shifts = new byte[fieldSizes.length];
        getMasks = new long[fieldSizes.length];
        clearMasks = new long[fieldSizes.length];

        int currentlyUsedBits = 0;
        int offsetInCurrentLong = 0;
        int curLong = 1;
        for (int field = 0; field < fieldSizes.length; field++) {
            final int currFieldSize = fieldSizes[field];

            // If it doesn't fit in current long, align
            if ((currentlyUsedBits + currFieldSize) > (curLong * 64)) {
                currentlyUsedBits = curLong * 64;
                curLong++;
                offsetInCurrentLong = 0;
            }

            shifts[field] = (byte) (64 - offsetInCurrentLong - currFieldSize);

            long mask = 0L;
            for (int i = 0; i < currFieldSize; i++) {
                mask |= (1L << (64 - offsetInCurrentLong - i));
            }
            getMasks[field] = mask;
            clearMasks[field] = ~mask;

            fieldOffset[field] = (curLong - 1);
            currentlyUsedBits += currFieldSize;
            offsetInCurrentLong += currFieldSize;
        }
        rowSize = (int) Math.ceil(currentlyUsedBits / 64d);

        // Allocate
        size = rowSize * numRows;
        address = unsafe.allocateMemory(size);
    }

    @Override
    public boolean equals(final IMemory other, final int row) {
        // TODO: Potentially expensive cast here
        final MemoryUnsafe3 o = (MemoryUnsafe3) other;
        final long startAdress = address + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getAddress(base) != o.unsafe.getAddress(base)) { return false; }
        }
        return true;
    }

    @Override
    public int get(final int row, final int col) {
        final long base = address + (row * rowSize) + fieldOffset[col];
        long result = unsafe.getAddress(base);
        result = ((result & getMasks[col]) >>> shifts[col]);
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

        final long startAdress = address + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            final long element = unsafe.getAddress(base);
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
        final long base = address + (row * rowSize) + fieldOffset[col];
        long result = unsafe.getAddress(base);
        // clear previous bits
        result &= clearMasks[col];

        // set new bits
        result |= (((long) val) << shifts[col]);

        unsafe.putAddress(base, result);
    }
}
