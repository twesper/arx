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
public class MemoryUnsafe {

    private final Unsafe  unsafe;     // The unsafe
    private final long    address;    // In bytes
    private final long    size;       // In bytes
    private final int[]   fieldSize;  // In bytes
    private final long[]  fieldOffset;// In bytes
    public  final long    rowSize;    // In bytes
    public  final int     rowSizeInLongs; // In longs
    
    public long     base;       // In bytes

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
        this.fieldOffset = new long[fieldSizes.length];
        int offset = 0;
        int index = 1;
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

            // If it doesn't fit in current long, align
            if ((offset + size) > (index * 64)) {
                offset += ((index * 64) - offset);
                index++;
            }
            
            this.fieldOffset[field] = offset;
            offset += this.fieldSize[field];
        }
        this.rowSize = (int) Math.ceil((double) offset / (double) 8) * 8;
        this.rowSizeInLongs = (int)(rowSize / 8);

        // Allocate
        this.size = rowSize * numRows;
        this.address = this.unsafe.allocateMemory(size);
        this.resetRow();
    }

    public boolean equals(MemoryUnsafe other) {
    	switch (rowSizeInLongs){
    	case 4:
    		if (this.unsafe.getLong(base+24) != other.unsafe.getLong(base+32)) return false;
    	case 3:
    		if (this.unsafe.getLong(base+16) != other.unsafe.getLong(base+16)) return false;
    	case 2:
    		if (this.unsafe.getLong(base+8) != other.unsafe.getLong(base+8)) return false;
    	case 1:
    		if (this.unsafe.getLong(base) != other.unsafe.getLong(base)) return false;
    		break;
    	default: throw new RuntimeException("Invalid bytes per row!");
    	}
        return true;
    }

    public int hashcode() {

        int hashcode = 1;
        long element = 0;
    	switch (rowSizeInLongs){
    	case 4:
    		element = unsafe.getLong(base+24);
    		hashcode = 31 * hashcode + (int)(element ^ (element >>> 32));
    	case 3:
    		element = unsafe.getLong(base+16);
    		hashcode = 31 * hashcode + (int)(element ^ (element >>> 32));
    	case 2:
    		element = unsafe.getLong(base+8);
    		hashcode = 31 * hashcode + (int)(element ^ (element >>> 32));
    	case 1:
    		element = unsafe.getLong(base);
    		hashcode = 31 * hashcode + (int)(element ^ (element >>> 32));
    		break;
    	default: throw new RuntimeException("Invalid bytes per row!");
    	}
        return hashcode;
    }

    public int get(int col) {
    	switch (fieldSize[col]) {
        case 1:
            return this.unsafe.getByte(base + fieldOffset[col]);
        case 2:
            return this.unsafe.getChar(base + fieldOffset[col]);
        case 4:
            return this.unsafe.getInt(base + fieldOffset[col]);
        default:
            throw new RuntimeException("Invalid field size!");
        }
    }
    
    public void setRow(long row){
    	this.base = address + row * rowSize;
    }
    
    public void resetRow(){
    	this.base = address;
    }

    public void set(int col, int val) {
        switch (fieldSize[col]) {
        case 1: 
            this.unsafe.putByte(base + fieldOffset[col], (byte)val); 
            break;
        case 2:
            this.unsafe.putChar(base + fieldOffset[col], (char)val); 
            break;
        case 4:
            this.unsafe.putInt(base + fieldOffset[col], val); 
            break;
        default:
            throw new RuntimeException("Invalid field size!");
        }
    }

    public long getByteSize() {
        return size;
    }
}
