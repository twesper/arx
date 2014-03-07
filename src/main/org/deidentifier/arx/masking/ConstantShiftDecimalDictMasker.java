package org.deidentifier.arx.masking;

import java.util.List;

public class ConstantShiftDecimalDictMasker extends
		AbstractDictionaryMasker<Double> {

	protected final ConstantShiftDecimalInstMasker instanceMasker;
	
	public ConstantShiftDecimalDictMasker(double shiftDistance) {
		instanceMasker = new ConstantShiftDecimalInstMasker(shiftDistance);
	}
	
	@Override
	public void maskList(List<Double> data) {
		for (int i = 0; i < data.size(); ++i)
			data.set(i, instanceMasker.mask(data.get(i)));
	}

	public double getShiftDistance() {
		return instanceMasker.getShiftDistance();
	}

	public void setShiftDistance(double shiftDistance) {
		instanceMasker.setShiftDistance(shiftDistance);
	}

}
