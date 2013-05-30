package org.deidentifier.arx.framework;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * A basic implementation of memory with an Unsafe
 *  
 * @author Prasser, Kohlmayer
 */
public class MemoryAlignedUnsafeIntArray implements IMemory {

    private final Unsafe unsafe;        // The unsafe
    private final long   baseAddress;   // The base address

    private final long   rowSize;       // Row size in bytes
    public final int     rowSizeInLongs; // In longs

    private final long   size;          // Total size in bytes

    public MemoryAlignedUnsafeIntArray(byte[] fieldSizes, int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

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
        final MemoryAlignedUnsafeIntArray o = (MemoryAlignedUnsafeIntArray) other;
        final long startAdress = baseAddress + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getAddress(base) != o.unsafe.getAddress(base)) { return false; }
        }
        return true;
    }

    @Override
    public int hashcode(int row) {
        int result = 1;

        final long startAdress = baseAddress + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            final long element = unsafe.getAddress(base);
            final int elementHash = (int) (element ^ (element >>> 32));
            result = (31 * result) + elementHash;
        }

        return result;

        // Minimal performance gain...
        // final long startAdress = baseAddress + (row * rowSize);
        //
        // int hashcode = 1;
        // long element = 0;
        // switch (rowSizeInLongs) {
        // case 6:
        // element = unsafe.getLong(startAdress + 40);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // case 5:
        // element = unsafe.getLong(startAdress + 32);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // case 4:
        // element = unsafe.getLong(startAdress + 24);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // case 3:
        // element = unsafe.getLong(startAdress + 16);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // case 2:
        // element = unsafe.getLong(startAdress + 8);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // case 1:
        // element = unsafe.getLong(startAdress);
        // hashcode = 31 * hashcode + (int) (element ^ (element >>> 32));
        // break;
        // default:
        // throw new RuntimeException("Invalid bytes per row!");
        // }
        // return hashcode;
    }

    @Override
    public int get(int row, int col) {
        final long base = baseAddress + (row * rowSize) + (col * 4);
        return unsafe.getInt(base);
    }

    @Override
    public void set(int row, int col, int val) {
        final long base = baseAddress + (row * rowSize) + (col * 4);
        unsafe.putInt(base, val);
    }

    @Override
    public long getByteSize() {
        return size;
    }
}
