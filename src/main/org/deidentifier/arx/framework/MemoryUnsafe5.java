package org.deidentifier.arx.framework;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Implementation of memory with Unsafe. 
 * Fields are either 1 (byte), 2 (short) or 4 (int) bytes in size. 
 * Each row is 8 byte (long) aligned, to allow for fast equals() and rather fast hashcode().
 *  
 * @author Prasser, Kohlmayer
 */
public class MemoryUnsafe5 implements IMemory {
    private static final int A_POWER     = 2;
    private static final int B_NUM_LONGS = 1 << A_POWER;

    private final long       baseAddress;               // In bytes

    // bit positions --> fieldSize in bytes, offset
    private final long[]     bitPos;                    // all aligned in one long array to enable efficient caching in one cache line
    private final long       rowSizeInBytes;            // In bytes

    private final long       size;                      // In bytes
    private final Unsafe     unsafe;                    // The unsafe

    public MemoryUnsafe5(final byte[] fieldSizes, final int numRows) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        // Access unsafe
        final Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe) f.get(null);

        // Field properties
        bitPos = new long[fieldSizes.length * B_NUM_LONGS];

        int currentlyUsedBits = 0;
        int curLong = 1;
        int idx = 0;
        for (int field = 0; field < fieldSizes.length; field++) {
            final int currFieldSize = fieldSizes[field];

            // If it doesn't fit in current long, align
            if ((currentlyUsedBits + currFieldSize) > (curLong * 64)) {
                currentlyUsedBits = curLong * 64;
                curLong++;
            }

            // bitpos [0] --> fieldsize

            if (currFieldSize <= 7) { // Byte
                bitPos[idx] = 1;
            } else if (currFieldSize <= 15) { // Short
                bitPos[idx] = 2;
            } else if (currFieldSize <= 31) { // Int
                bitPos[idx] = 4;
            } else {
                throw new RuntimeException("Unexpected field size: " + currFieldSize);
            }

            // bitpos [1] --> offset
            bitPos[idx + 1] = currentlyUsedBits;

            idx += B_NUM_LONGS;

            currentlyUsedBits += currFieldSize;
        }

        // make multiple of 8
        rowSizeInBytes = curLong * 8;

        // Allocate
        size = rowSizeInBytes * numRows;
        baseAddress = unsafe.allocateMemory(size);

        // precompute the offset
        for (int i = 0; i < bitPos.length; i += B_NUM_LONGS) {
            bitPos[i + 1] += baseAddress;
        }
    }

    @Override
    public boolean equals(final IMemory other, final int row) {
        final Unsafe o = ((MemoryUnsafe5) other).unsafe;
        final long startAdress = baseAddress + (row * rowSizeInBytes);
        final long endAdress = startAdress + rowSizeInBytes;
        for (long base = startAdress; base < endAdress; base += 8) {
            if (unsafe.getLong(base) != o.getLong(base)) { return false; }
        }
        return true;
    }

    @Override
    public int get(final int row, final int col) {
        final int idx = (col << A_POWER);

        if (bitPos[idx] == 1L) {
            return unsafe.getByte(bitPos[idx + 1] + (row * rowSizeInBytes));
        } else if (bitPos[idx] == 2L) {
            return unsafe.getShort(bitPos[idx + 1] + (row * rowSizeInBytes));
        } else if (bitPos[idx] == 4L) {
            return unsafe.getInt(bitPos[idx + 1] + (row * rowSizeInBytes));
        } else {
            throw new RuntimeException("Invalid field size!");
        }

        // final int fieldSize = (int) bitPos[idx];
        // switch (fieldSize) {
        // case 1:
        // return this.unsafe.getByte(bitPos[idx + 1]);
        // case 2:
        // return this.unsafe.getShort(bitPos[idx + 1]);
        // case 4:
        // return this.unsafe.getInt(bitPos[idx + 1]);
        // default:
        // throw new RuntimeException("Invalid field size!");
        // }
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
            final long element = unsafe.getLong(base);
            final int elementHash = (int) (element ^ (element >>> 32));
            result = (31 * result) + elementHash;
        }
        return result;
    }

    @Override
    public void set(final int row, final int col, final int val) {
        final int idx = (col << A_POWER);

        if (bitPos[idx] == 1L) {
            unsafe.putByte(bitPos[idx + 1] + (row * rowSizeInBytes), (byte) val);
        } else if (bitPos[idx] == 2L) {
            unsafe.putShort(bitPos[idx + 1] + (row * rowSizeInBytes), (short) val);
        } else if (bitPos[idx] == 4L) {
            unsafe.putInt(bitPos[idx + 1] + (row * rowSizeInBytes), val);
        } else {
            throw new RuntimeException("Invalid field size!");
        }

        // final int fieldSize = (int) bitPos[idx];
        // switch (fieldSize) {
        // case 1:
        // this.unsafe.putByte(bitPos[idx + 1], (byte) val);
        // break;
        // case 2:
        // this.unsafe.putShort(bitPos[idx + 1], (short) val);
        // break;
        // case 4:
        // this.unsafe.putInt(bitPos[idx + 1], val);
        // break;
        // default:
        // throw new RuntimeException("Invalid field size!");
        // }
    }
}
