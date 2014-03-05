package org.deidentifier.arx.masking;

import java.util.List;
import java.util.Date;

public class ConstantShiftDateDictMasker extends AbstractDictionaryMasker<Date> {

	protected final ConstantShiftDateInstMasker instanceMasker;
	
	public ConstantShiftDateDictMasker(long shiftDistance) {
		instanceMasker = new ConstantShiftDateInstMasker(shiftDistance);
	}
	
	public ConstantShiftDateDictMasker(int shiftDistance, int timeUnit) {
		instanceMasker = new ConstantShiftDateInstMasker(shiftDistance, timeUnit);
	}
	
	@Override
	public void maskList(List<Date> data) {
		for (int i = 0; i < data.size(); ++i)
			data.set(i, instanceMasker.mask(data.get(i)));
	}
	
	public long getShiftDistance() {
		return instanceMasker.getShiftDistance();
	}
	
	public void setShiftDistance(long shiftDistance) {
		instanceMasker.setShiftDistance(shiftDistance);
	}
	
	public void setShiftDistance(int shiftDistance, int timeUnit) {
		instanceMasker.setShiftDistance(shiftDistance, timeUnit);
	}

}
