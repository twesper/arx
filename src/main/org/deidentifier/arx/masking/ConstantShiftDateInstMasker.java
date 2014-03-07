package org.deidentifier.arx.masking;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Masks a Date instance by adding a specified number of time units.
 * 
 * @author Wesper
 *
 */
public class ConstantShiftDateInstMasker extends AbstractInstanceMasker<Date> {

	/** The amount of time the input date shall be shifted. */
	private Period shiftPeriod;
	
	/**
	 * Creates a constant shift masker that shifts the input date by the specified amount of
	 * time.
	 * 
	 * @param shiftDistance The shift in milliseconds.
	 */
	public ConstantShiftDateInstMasker(int shiftDistance) {
		setShiftDistance(shiftDistance);
	}
	
	/**
	 * Creates a constant shift masker that shifts the input by the given period of time.
	 * 
	 * @param shiftPeriod The amount of time the dates are shifted.
	 */
	public ConstantShiftDateInstMasker(Period shiftPeriod) {
		setShiftPeriod(shiftPeriod);
	}
	
	@Override
	public Date mask(Date input) {
		DateTime date = new DateTime(input);
		DateTime shiftedDate = date.plus(shiftPeriod);
		return shiftedDate.toDate();
	}

	public int getShiftDistance() {
		return shiftPeriod.getMillis();
	}

	public void setShiftDistance(int shiftDistance) {
		shiftPeriod = new Period(shiftDistance);
	}


	public Period getShiftPeriod() {
		return shiftPeriod;
	}

	/**
	 * Sets the time interval by which the masker will shift dates.
	 * 
	 * @param shiftPeriod The time period to be added to dates.
	 */
	public void setShiftPeriod(Period shiftPeriod) {
		this.shiftPeriod = shiftPeriod;
	}

}
