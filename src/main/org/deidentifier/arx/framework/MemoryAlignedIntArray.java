package org.deidentifier.arx.framework;

/**
 * A basic implementation of memory with an int-array.
 *  
 * @author Prasser, Kohlmayer
 */
public class MemoryAlignedIntArray implements IMemory{

	private final int[][] memory;
	private final int numCols;

	public MemoryAlignedIntArray(byte[] fieldSizes, int numRows){
		this.numCols = fieldSizes.length;
		this.memory = new int[numRows][numCols];
	}
	
	@Override
	public boolean equals(IMemory other, int row){
        // TODO: Potentially expensive cast here
        for (int i = 0; i < numCols; i++) {
            if (this.memory[row][i]!=((MemoryAlignedIntArray)other).memory[row][i]) return false;
        }
        return true;
	}

	@Override
	public int hashcode(int row){
        int result = 23;
        int[] array = memory[row];
        for (int i = 0; i < array.length; i++) {
            result = (37 * result) + array[i];
        }
        return result;
	}
	
	@Override
	public int get(int row, int col){
		return memory[row][col];
	}
	
	@Override
	public void set(int row, int col, int val){
		memory[row][col] = val;
	}

	@Override
    public long getByteSize() {
	    return memory.length * numCols * 4;
    }
}
