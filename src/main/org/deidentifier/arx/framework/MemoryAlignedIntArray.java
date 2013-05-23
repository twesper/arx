package org.deidentifier.arx.framework;

public class MemoryAlignedIntArray implements IMemory{

	private final int[][] memory;
	private final int numCols;
	private final int numRows;
	
	public MemoryAlignedIntArray(byte[] fieldSizes, int numRows){
		this.numRows = numRows;
		this.numCols = fieldSizes.length;
		this.memory = new int[numRows][numCols];
	}
	
	@Override
	public boolean equals(IMemory other, int row){
		return false;
	}

	@Override
	public int fastHashcode(int row){
		return 0;
	}
	
	@Override
	public int hashcode(int row){
		return 0;
	}
	
	@Override
	public int get(int row, int col){
		return 0;
	}
	
	@Override
	public void set(int row, int col, int val){
		
	}
}
