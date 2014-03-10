package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.IntegerDistribution;

/**
 * A masker that generates random integers in decimal format, using a given probability
 * distribution.
 * <p>
 * A shift constant can be added to each sampled value to allow quick and simple modification
 * of the distribution.
 * 
 * @author Wesper
 *
 */
public class GenerateRandomIntegerDecimalMasker extends
				AbstractReplaceInstMasker<Double> {

	IntegerDistribution	distribution;
	int					shiftConstant = 0;
	
	public GenerateRandomIntegerDecimalMasker(IntegerDistribution distribution) {
		this(distribution, 0);
	}
	
	public GenerateRandomIntegerDecimalMasker(IntegerDistribution distribution,
												int shiftConstant) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
	}

	@Override
	public Double createReplacement() {
		return Double.valueOf(distribution.sample() + shiftConstant);
	}

}
