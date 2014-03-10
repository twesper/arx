package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.RealDistribution;

public class GenerateRandomDecimalMasker extends AbstractGenerateMasker<Double> {

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
	public Double generate() {
		return distribution.sample() + shiftConstant;
	}
	
	

}
