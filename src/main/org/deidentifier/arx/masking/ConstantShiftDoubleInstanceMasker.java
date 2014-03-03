package org.deidentifier.arx.masking;

/**
 * Masks an instance of a Double by adding a configurable constant value.
 * 
 * @author Tobias Wesper
 *
 */
public class ConstantShiftDoubleInstanceMasker extends
		AbstractInstanceMasker<Double> {

	private double shiftDistance;
	
	public ConstantShiftDoubleInstanceMasker(double shiftDistance) {
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
