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
public class MemoryUnsafe2 implements IMemory {

    private final long   address;    // In bytes
    private final long[] fieldOffset; // In bytes
    private final int[]  fieldSize;  // In bytes
    private final long   rowSize;    // In bytes
    private final long   size;       // In bytes
    private final Unsafe unsafe;     // The unsafe

    private final long[] masks;
    private final long[] shifts;

    public MemoryUnsafe2(final byte[] fieldSizes, final int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        // Access unsafe
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);

        // Field properties
        fieldSize = new int[fieldSizes.length];
        fieldOffset = new long[fieldSizes.length];
        shifts = new long[fieldSizes.length];
        masks = new long[fieldSizes.length];
        int offset = 0;
        int currentLong = 1;
        for (int field = 0; field < fieldSizes.length; field++) {
            final int size = fieldSizes[field];
            if (size <= 8) { // Byte
                fieldSize[field] = 1;
            } else if (size <= 16) { // Char
                fieldSize[field] = 2;
            } else if (size <= 32) { // Int
                fieldSize[field] = 4;
            } else {
                throw new RuntimeException("Unexpected field size: " + size);
            }

            // If it doesn't fit in current long, align
            if ((offset + size) > (currentLong * 64)) {
                offset += ((currentLong * 64) - offset);
                currentLong++;
            }
            fieldOffset[field] = offset;
            offset += fieldSize[field];
        }
        rowSize = (int) Math.ceil((double) offset / (double) 8) * 8;

        // Allocate
        size = rowSize * numRows;
        address = unsafe.allocateMemory(size);
    }

    @Override
    public boolean equals(final IMemory other, final int row) {
        // TODO: Potentially expensive cast here
        final MemoryUnsafe2 o = (MemoryUnsafe2) other;
        final long startAdress = address + (row * rowSize);
        final long endAdress = startAdress + rowSize;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getAddress(base) != o.unsafe.getAddress(base)) { return false; }
        }
        return true;
    }

    @Override
    public int get(final int row, final int col) {
        final long base = address + ((long)row * rowSize) + fieldOffset[col];

        // unsafe.putAddress(allocateMemory, value);

        switch (fieldSize[col]) {
        case 1:
            return unsafe.getByte(base);
        case 2:
            return unsafe.getChar(base);
        case 4:
            return unsafe.getInt(base);
        default:
            throw new RuntimeException("Invalid field size!");
        }
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
        final long base = address + ((long)row * rowSize) + fieldOffset[col];
        switch (fieldSize[col]) {
        case 1:
            unsafe.putByte(base, (byte) val);
            break;
        case 2:
            unsafe.putChar(base, (char) val);
            break;
        case 4:
            unsafe.putInt(base, val);
            break;
        default:
            throw new RuntimeException("Invalid field size!");
        }
    }
}
