package org.deidentifier.arx.masking;

import java.util.List;
import java.util.Date;

import org.joda.time.Period;

public class ConstantShiftDateDictMasker extends AbstractDictionaryMasker<Date> {

	protected final ConstantShiftDateInstMasker instanceMasker;
	
	public ConstantShiftDateDictMasker(int shiftDistance) {
		instanceMasker = new ConstantShiftDateInstMasker(shiftDistance);
	}
	
	public ConstantShiftDateDictMasker(Period shiftPeriod) {
		instanceMasker = new ConstantShiftDateInstMasker(shiftPeriod);
	}
	
	@Override
	public void maskList(List<Date> data) {
		for (int i = 0; i < data.size(); ++i)
			data.set(i, instanceMasker.mask(data.get(i)));
	}
	
	public long getShiftDistance() {
		return instanceMasker.getShiftDistance();
	}
	
	public void setShiftDistance(int shiftDistance) {
		instanceMasker.setShiftDistance(shiftDistance);
	}
	
	public void setShiftDistance(Period shiftPeriod) {
		instanceMasker.setShiftPeriod(shiftPeriod);
	}

}
