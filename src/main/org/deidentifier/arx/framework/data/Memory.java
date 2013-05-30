package org.deidentifier.arx.framework.data;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * A fast implementation of a two dimensional integer array based 
 * on unsafe memory accesses, which implements specialized methods
 * 
 * @author Prasser, Kohlmayer
 */
public class Memory  {

    private final Unsafe unsafe;          // The unsafe
    private final long   baseAddress;     // The base address
    private final long   rowSizeInBytes;  // Row size in bytes
    public final int     rowSizeInDWords; // In longs
    private final long   size;            // Total size in bytes
    private final int    length;          // Length in rows
    private final int    width;           // Length in cols
    private boolean      freed = false;

    public Memory(int rows, int cols) {
        
        // TODO: Get rid of this for SE and IS
        // Implement a second variant of Memory, that implements
        // for-loops and does not have this restriction
        if (cols>12) {
            throw new IndexOutOfBoundsException("Not more than 12 columns supported!");
        }
        
        this.length = rows;
        this.width = cols;

        // Access unsafe
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            this.unsafe = (Unsafe) f.get(null);
        } catch (Exception e){
            throw new RuntimeException("Error accessing unsafe memory!", e);
        }

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

    public boolean equals(int row1, int row2) {
        
        // TODO: Implement with offsets?
        final long base1 = baseAddress + (row1 * rowSizeInBytes);
        final long base2 = baseAddress + (row2 * rowSizeInBytes);
        final long end1 = base1 + rowSizeInBytes;
        long address2 = base2;
        for (long address1 = base1; address1 < end1; address1 += 8) {
            if (unsafe.getAddress(address1) != unsafe.getAddress(address2)) { return false; }
            address2 += 8;
        }
        return true;
    }
    
    public boolean equalsIgnoreOutliers(int row1, int row2) {
        
        // TODO: Implement with offsets?
        final long base1 = baseAddress + (row1 * rowSizeInBytes);
        final long base2 = baseAddress + (row2 * rowSizeInBytes);
        final long end1 = base1 + rowSizeInBytes;
        long address2 = base2;
        for (long address1 = base1; address1 < end1; address1 += 8) {
            if ((unsafe.getAddress(address1) & Data.REMOVE_OUTLIER_LONG_MASK)
               != (unsafe.getAddress(address2) & Data.REMOVE_OUTLIER_LONG_MASK)) { 
                return false; 
            }
            address2 += 8;
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
        freed = true;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public void swap(int row1, int row2) {

        // TODO: Implement with unsafe.copyMemory()
        long[] temp = new long[rowSizeInDWords];
        
        // Write from row2 to temp
        long base = baseAddress + (row2 * rowSizeInBytes);
        long end = base + rowSizeInBytes;
        int index = 0;
        for (long address = base; address < end; address += 8) {
            temp[index] = unsafe.getAddress(address);
            index++;
        }
        
        // Write from row1 to row2
        long base1 = baseAddress + (row1 * rowSizeInBytes);
        long end1 = base1 + rowSizeInBytes;
        long base2 = baseAddress + (row2 * rowSizeInBytes);
        long address2 = base2;
        for (long address1 = base1; address1 < end1; address1 += 8) {
            unsafe.putAddress(address2, unsafe.getAddress(address1));
            address2 += 8;
        }
        
        // Write from temp to row1
        base = baseAddress + (row1 * rowSizeInBytes);
        end = base + rowSizeInBytes;
        index = 0;
        for (long address = base; address < end; address += 8) {
            unsafe.putAddress(address, temp[index]);
            index++;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        // TODO: The call to free() should be required explicitly,
        // as there is no guarantee that finalize will be called!
        if (!freed) free();
    }
}
