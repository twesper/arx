package org.deidentifier.arx.framework.data;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * A basic implementation of memory with an Unsafe
 * 
 * @author Prasser, Kohlmayer
 */
public class FastMemory {

    private final Unsafe unsafe;          // The unsafe
    private final long   baseAddress;     // The base address
    private final long   rowSizeInBytes;  // Row size in bytes
    public final int     rowSizeInDWords; // In longs
    private final long   size;            // Total size in bytes

    public FastMemory(int rows, int cols) throws SecurityException,
                                     NoSuchFieldException,
                                     IllegalArgumentException,
                                     IllegalAccessException {
        
        if (cols>12) {
            throw new IndexOutOfBoundsException("Not more than 12 columns supported!");
        }

        // Access unsafe
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        this.unsafe = (Unsafe) f.get(null);

        // Align
        final int bytes = (cols * 4);
        if (bytes % 8 == 0) {
            this.rowSizeInBytes = bytes;
        } else {
            this.rowSizeInBytes = (bytes + 4);
        }

        // Allocate
        this.rowSizeInDWords = (int) (rowSizeInBytes / 8);
        this.size = rows * rowSizeInBytes;
        this.baseAddress = this.unsafe.allocateMemory(size);
    }

    public boolean equals(FastMemory other, int row) {
        final Unsafe otherUnsafe = other.unsafe;
        final long base = baseAddress + (row * rowSizeInBytes);
        final long end = base + rowSizeInBytes;
        for (long address = base; address < end; address += 8) {
            if (unsafe.getAddress(address) != otherUnsafe.getAddress(address)) { return false; }
        }
        return true;
    }

    public int hashCode(int row) {
        long temp = 1125899906842597L;
        final long base = baseAddress + (row * rowSizeInBytes);
        switch (rowSizeInDWords) {
        case 6:
            temp = (31 * temp) + unsafe.getAddress(base + 40);
        case 5:
            temp = (31 * temp) + unsafe.getAddress(base + 32);
        case 4:
            temp = (31 * temp) + unsafe.getAddress(base + 24);
        case 3:
            temp = (31 * temp) + unsafe.getAddress(base + 16);
        case 2:
            temp = (31 * temp) + unsafe.getAddress(base + 8);
        case 1:
            temp = (31 * temp) + unsafe.getAddress(base);
            break;
        default:
            throw new RuntimeException("Invalid bytes per row!");
        }
        return (int) (31 * temp) * (int) (temp >>> 32);
    }

    public int get(int row, int col) {
        final long base = baseAddress + (row * rowSizeInBytes) + (col << 2);
        return unsafe.getInt(base);
    }

    public void set(int row, int col, int val) {
        final long base = baseAddress + (row * rowSizeInBytes) + (col << 2);
        unsafe.putInt(base, val);
    }

    public void free() {
        unsafe.freeMemory(baseAddress);
    }
}
