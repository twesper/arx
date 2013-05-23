package org.deidentifier.arx.framework;

public class MemoryUnalignedLongArray implements IMemory{

	private final long[] memory;
	private final byte[] fieldSizes;
	private final int rowSizeInLongs;
	private final int rowSizeInBits;
	private final int rowSizeInFields;
	private final int numRows;
	
	public MemoryUnalignedLongArray(byte[] fieldSizes, int numRows){
		this.numRows = numRows;
		this.fieldSizes = fieldSizes;
		this.rowSizeInFields = fieldSizes.length;
		int sizeInBits = 0;
		for (byte size : fieldSizes){
			sizeInBits+=size;
		}
		this.rowSizeInBits = sizeInBits;
		this.rowSizeInLongs = (int)Math.ceil((double)rowSizeInBits / 64d);
		this.memory = new long[this.numRows * this.rowSizeInLongs];
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
