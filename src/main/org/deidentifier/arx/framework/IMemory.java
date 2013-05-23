package org.deidentifier.arx.framework;

public interface IMemory {
	
	public boolean equals(IMemory memory, int row);
	public int fastHashcode(int row);
	public int hashcode(int row);
	public int get(int row, int col);
	public int set(int row, int col);
}
