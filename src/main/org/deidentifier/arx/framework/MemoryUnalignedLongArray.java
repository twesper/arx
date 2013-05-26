package org.deidentifier.arx.framework;

/**
 * Implementation of memory with a long array. 
 * Fields have exact bit sizes and do not cross 8-byte boundaries. 
 * Each row is 8 byte (long) aligned to allow for fast equals() and fast hashcode().
 *  
 * @author Prasser, Kohlmayer
 */
public class MemoryUnalignedLongArray implements IMemory{

	private final long[] memory;
	private final int rowSizeInLongs;
	private final int rowSizeInBits;
	private final int numRows;
	private final byte[] shifts;
	private final byte[] offsets;
	private final int[] masks;
	
	public MemoryUnalignedLongArray(byte[] fieldSizes, int numRows){
	    
	    // General properties
		this.numRows = numRows;
		int sizeInBits = 0;
		for (byte size : fieldSizes){
			sizeInBits+=size;
		}
		this.rowSizeInBits = sizeInBits;
		this.rowSizeInLongs = (int)Math.ceil((double)rowSizeInBits / 64d);
		this.memory = new long[this.numRows * this.rowSizeInLongs];
		
		// Per field properties
		this.shifts = new byte[fieldSizes.length];
		this.masks = new int[fieldSizes.length];
		this.offsets = new byte[fieldSizes.length];
		int index = 0;
		int offset = 0;
		for (int i=0; i<fieldSizes.length; i++){
		    if (offset+fieldSizes.length>64){
		        offset=0;
		        index++;
		    } 
		    shifts[i] = (byte)(64 - offset - fieldSizes[i]);
            offsets[i] = (byte)index;
            masks[i] = 0xffffffff >>> (64-fieldSizes[i]);
            offset+=fieldSizes.length;
		}
	}
	
	@Override
	public boolean equals(IMemory other, int row){
	    // TODO: Potentially expensive cast here
        int sIdx = row * rowSizeInLongs;
        int eIdx = sIdx + rowSizeInLongs;
        for (int i = sIdx; i < eIdx; i++) {
            if (this.memory[i]!=((MemoryUnalignedLongArray)other).memory[i]) return false;
        }
        return true;
	}

	@Override
	public int hashcode(int row){
        int result = 23;
        int sIdx = row * rowSizeInLongs;
        int eIdx = sIdx + rowSizeInLongs;
        for (int i = sIdx; i < eIdx; i++) {
            result = (37 * result) + (int)(memory[i] >>> 32);
            result = (37 * result) + (int)(memory[i]);
        }
        return result;
	}
	
	@Override
	public int get(int row, int col){
	    int index = row * rowSizeInLongs + offsets[col];
	    byte shift = shifts[col];
	    int mask = masks[col];
	    return (int)(memory[index] >>> shift) & mask;
	}
	
	@Override
	public void set(int row, int col, int val){
	    int index = row * rowSizeInLongs + offsets[col];
        byte shift = shifts[col];
        int mask = masks[col];
        
        // Erase and set
        memory[index] &= ~((long)mask << shift);
        memory[index] |= ((long)val << shift);
	}
	
    @Override
    public long getByteSize() {
        return memory.length * 8;
    }
}
