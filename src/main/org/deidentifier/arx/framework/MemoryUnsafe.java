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
public class MemoryUnsafe implements IMemory {

    private Unsafe unsafe;     // The unsafe
    private long   address;    // In bytes
    private long   size;       // In bytes
    private int[]  fieldSize;  // In bytes
    private int[]  fieldOffset;// In bytes
    private int    rowSize;    // In bytes

    public MemoryUnsafe(byte[] fieldSizes, int numRows) throws SecurityException,
                                                       NoSuchFieldException,
                                                       IllegalArgumentException,
                                                       IllegalAccessException {

        // Access unsafe
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        this.unsafe = (Unsafe) f.get(null);

        // Field properties
        this.fieldSize = new int[fieldSizes.length];
        this.fieldOffset = new int[fieldSizes.length];
        int offset = 0;
        for (int field = 0; field < fieldSizes.length; field++) {
            int size = fieldSizes[field];
            if (size <= 8) { // Byte
                this.fieldSize[field] = 1;
            } else if (size <= 16) { // Char
                this.fieldSize[field] = 2;
            } else if (size <= 32) { // Int
                this.fieldSize[field] = 4;
            } else {
                throw new RuntimeException("Unexpected field size: " + size);
            }
            // TODO: Might be unaligned and cross word boundaries
            this.fieldOffset[field] = offset;
            offset += this.fieldSize[field];
        }
        this.rowSize = (int) Math.ceil((double) offset / (double) 8) * 8;

        // Allocate
        this.size = rowSize * numRows;
        this.address = this.unsafe.allocateMemory(size);
    }

    @Override
    public boolean equals(IMemory other, int row) {
        // TODO: Potentially expensive cast here
        long startAdress = address + row * rowSize;
        long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (this.unsafe.getLong(base) != ((MemoryUnsafe) other).unsafe.getLong(base)) return false;
        }
        return true;
    }

    @Override
    public int hashcode(int row) {
        int result = 23;
        long startAdress = address + row * rowSize;
        long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            result = (37 * result) + unsafe.getInt(base);
            result = (37 * result) + unsafe.getInt(base + 4);
        }
        return result;
    }

    @Override
    public int get(int row, int col) {
        long base = address + row * rowSize + fieldOffset[col];
        switch (fieldSize[col]) {
        case 1:
            return this.unsafe.getByte(base);
        case 2:
            return this.unsafe.getChar(base);
        case 4:
            return this.unsafe.getInt(base);
        default:
            throw new RuntimeException("Invalid field size!");
        }
    }

    @Override
    public void set(int row, int col, int val) {
        long base = address + row * rowSize + fieldOffset[col];
        switch (fieldSize[col]) {
        case 1: 
            this.unsafe.putByte(base, (byte)val); 
            break;
        case 2:
            this.unsafe.putChar(base, (char)val); 
            break;
        case 4:
            this.unsafe.putInt(base, (int)val); 
            break;
        default:
            throw new RuntimeException("Invalid field size!");
        }
    }

    @Override
    public long getByteSize() {
        return size;
    }
}
