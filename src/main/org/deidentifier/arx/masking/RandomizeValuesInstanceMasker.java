package org.deidentifier.arx.masking;

/**
 * Example for a generic abstract operator on instance level 
 */
public abstract class RandomizeValuesInstanceMasker<T> extends AbstractInstanceMasker<T>{

	protected final int[] distribution;
	
	public RandomizeValuesInstanceMasker(int[] distribution) {
		this.distribution = distribution;
	}
}
