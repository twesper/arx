package org.deidentifier.arx.masking;

import java.util.List;

public class ConstantShiftDoubleDictionaryMasker extends
		AbstractDictionaryMasker<Double> {

	private double shiftDistance;
	
	public ConstantShiftDoubleDictionaryMasker(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	@Override
	public void maskList(List<Double> data) {
		
		ConstantShiftDoubleInstanceMasker instanceMasker =
				new ConstantShiftDoubleInstanceMasker(shiftDistance);
		
		for (int i = 0; i < data.size(); ++i)
			data.set(i, data.get(i) + shiftDistance);
		
	}

	public double getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(double shiftDistance) {
		this.shiftDistance = shiftDistance;
	}

}
