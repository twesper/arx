package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * A masker that generates random decimals by sampling from a given probability distribution.
 * <p>
 * Optionally, a shift constant can be supplied to easily modify the sampled values.
 * 
 * @author Wesper
 *
 */
public class GenerateRandomDecimalMasker extends AbstractReplaceInstMasker<Double> {

	protected RealDistribution	distribution;
	protected double			shiftConstant = 0.0d;
	
	public GenerateRandomDecimalMasker(RealDistribution distribution) {
		this(distribution, 0.0d);
	}
	
	public GenerateRandomDecimalMasker(RealDistribution distribution, double shiftConstant) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
	}

	@Override
	public Double createReplacement() {
		return distribution.sample() + shiftConstant;
	}
	
	

}
