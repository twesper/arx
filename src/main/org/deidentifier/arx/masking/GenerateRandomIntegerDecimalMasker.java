package org.deidentifier.arx.masking;

import org.apache.commons.math3.distribution.IntegerDistribution;

public class GenerateRandomIntegerDecimalMasker extends
		AbstractGenerateMasker<Double> {

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
	public Double generate() {
		return Double.valueOf(distribution.sample() + shiftConstant);
	}

}
