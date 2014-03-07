package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.IntegerDistribution;

/**
 * Example for a generic abstract operator on instance level 
 */
public abstract class AbstractRandomizeInstMasker<T> extends AbstractInstanceMasker<T>{

	protected IntegerDistribution distribution;
	
	public AbstractRandomizeInstMasker(IntegerDistribution distribution) {
		this.distribution = distribution;
	}
}
