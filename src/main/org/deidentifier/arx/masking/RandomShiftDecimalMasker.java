package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * A masker that shifts decimal numbers randomly according to a given probability
 * distribution.
 * <p>
 * The shift is calculated by sampling from the provided distribution. Optionally, a shift
 * constant can be added to the sampled value as to allow a quick and very basic modification
 * of the distribution.
 * 
 * @author Wesper
 *
 */
public class RandomShiftDecimalMasker extends AbstractInstBasedDictMasker<Double> {

	private RealDistribution	distribution;
	private double				shiftConstant = 0.0d;
	
	public RandomShiftDecimalMasker(RealDistribution distribution) {
		this(distribution, 0.0d);
	}
	
	public RandomShiftDecimalMasker(RealDistribution distribution, double shiftConstant) {
		this.distribution = distribution;
		this.shiftConstant = shiftConstant;
	}
	
	/**
	 * Masks the input by adding a random value sampled from the given probability
	 * distribution.
	 */
	@Override
	public Double mask(Double input) {
		return input + distribution.sample() + shiftConstant;
	}

}
