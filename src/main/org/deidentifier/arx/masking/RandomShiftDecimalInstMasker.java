package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.RealDistribution;

public class RandomShiftDecimalInstMasker extends AbstractInstanceMasker<Double> {

	private RealDistribution	distribution;
	private double				shiftConstant = 0.0d;
	
	public RandomShiftDecimalInstMasker(RealDistribution distribution) {
		this(distribution, 0.0d);
	}
	
	public RandomShiftDecimalInstMasker(RealDistribution distribution, double shiftConstant) {
		this.distribution = distribution;
		this.shiftConstant = shiftConstant;
	}
	
	@Override
	public Double mask(Double input) {
		return input + distribution.sample() + shiftConstant;
	}

}
