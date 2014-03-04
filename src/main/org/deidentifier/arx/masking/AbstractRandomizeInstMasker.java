package org.deidentifier.arx.masking;

/**
 * Example for a generic abstract operator on instance level 
 */
public abstract class AbstractRandomizeInstMasker<T> extends AbstractInstanceMasker<T>{

	protected final int[] distribution;
	
	public AbstractRandomizeInstMasker(int[] distribution) {
		this.distribution = distribution;
	}
}
