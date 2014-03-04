package org.deidentifier.arx.masking;

import java.util.List;

public class ConstantShiftDoubleDictMasker extends
		AbstractDictionaryMasker<Double> {

	private double shiftDistance;
	
	public ConstantShiftDoubleDictMasker(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	@Override
	public void maskList(List<Double> data) {
		
		ConstantShiftDoubleInstMasker instanceMasker =
				new ConstantShiftDoubleInstMasker(shiftDistance);
		
		for (int i = 0; i < data.size(); ++i)
			data.set(i, instanceMasker.mask(data.get(i)));
		
	}

	public double getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}

}
