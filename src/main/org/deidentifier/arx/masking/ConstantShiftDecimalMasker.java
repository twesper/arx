package org.deidentifier.arx.masking;

/**
 * Masks instances of decimal data by adding a constant value.
 * 
 * @author Wesper
 *
 */
public class ConstantShiftDecimalMasker extends AbstractInstBasedDictMasker<Double> {

	protected double shiftDistance;
	
	public ConstantShiftDecimalMasker(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	@Override
	public Double mask(Double input) {
		return input + shiftDistance;
	}

	public double getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}

}
