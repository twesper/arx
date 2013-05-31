package org.deidentifier.arx.framework;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * A basic implementation of memory with an Unsafe
 * 
 * @author Prasser, Kohlmayer
 */
public class MemoryAlignedUnsafeIntArray2 implements IMemory {

    private final Unsafe unsafe;        // The unsafe
    private final long   baseAddress;   // The base address

    private final long   rowSize;       // Row size in bytes
    public final int     rowSizeInLongs; // In longs

    private final long   size;          // Total size in bytes

    public MemoryAlignedUnsafeIntArray2(byte[] fieldSizes, int numRows) throws SecurityException,
                                                                      NoSuchFieldException,
                                                                      IllegalArgumentException,
                                                                      IllegalAccessException {

        // Access unsafe
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        this.unsafe = (Unsafe) f.get(null);

        final int byteSize = (fieldSizes.length * 4);
        // check if row aligns with long
        if (byteSize % 8 == 0) {
            // the row is already aligned with long
            this.rowSize = byteSize;
        } else {
            // need to add another int size
            this.rowSize = (byteSize + 4);
        }

        this.rowSizeInLongs = (int) (rowSize / 8);

        // Allocate
        this.size = numRows * rowSize;
        this.baseAddress = this.unsafe.allocateMemory(size);

    }

    @Override
    public boolean equals(IMemory other, int row) {
        final Unsafe o = ((MemoryAlignedUnsafeIntArray2) other).unsafe;
        final long startAdress = baseAddress + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getLong(base) != o.getLong(base)) { return false; }
        }
        return true;
    }

    @Override
    public int hashcode(int row) {
        long temp = 1125899906842597L;
        final long startAdress = baseAddress + (row * rowSize);
        switch (rowSizeInLongs) {
        case 6:
            temp = (31 * temp) + unsafe.getLong(startAdress+40);
        case 5:
            temp = (31 * temp) + unsafe.getLong(startAdress+32);
        case 4:
            temp = (31 * temp) + unsafe.getLong(startAdress+24);
        case 3:
            temp = (31 * temp) + unsafe.getLong(startAdress+16);
        case 2:
            temp = (31 * temp) + unsafe.getLong(startAdress+8);
        case 1:
            temp = (31 * temp) + unsafe.getLong(startAdress);
            break;
        default:
            throw new RuntimeException("Invalid bytes per row!");
        }
        return (int) (31 * temp) * (int) (temp >>> 32);

    }

    @Override
    public int get(int row, int col) {
        final long base = baseAddress + (row * rowSize) + (col << 2);
        return unsafe.getInt(base);
    }

    @Override
    public void set(int row, int col, int val) {
        final long base = baseAddress + (row * rowSize) + (col << 2);
        unsafe.putInt(base, val);
    }

    @Override
    public long getByteSize() {
        return size;
    }
}
