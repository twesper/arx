package org.deidentifier.arx.masking;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Masks a Date instance by adding a specified number of time units.
 * 
 * @author Tobias Wesper
 *
 */
public class ConstantShiftDateInstMasker extends AbstractInstanceMasker<Date> {

	/** The amount of time the input date shall be shifted, in milliseconds. */
	protected long shiftDistance;
	
	/**
	 * Creates a constant shift masker that shifts the input date by the specified amount of
	 * time.
	 * 
	 * @param shiftDistance The shift in milliseconds.
	 */
	public ConstantShiftDateInstMasker(long shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	/**
	 * Creates a constant shift masker that shifts the input by {@code shiftDistance} units of
	 * time. The time unit is specified by the second argument as one of the static fields of
	 * {@code java.util.Calendar}, e.g. {@code DAY_OF_MONTH}, {@code MONTH} or {@code YEAR}.
	 *     
	 * @param shiftDistance The amount of units of time that the input date will be shifted. 
	 * @param timeUnit One of the unit field numbers specified by {@link java.util.Calendar}.
	 */
	public ConstantShiftDateInstMasker(int shiftDistance, int timeUnit) {
		setShiftDistance(shiftDistance, timeUnit);
	}
	
	@Override
	protected Date mask(Date input) {
		return new Date(input.getTime() + shiftDistance);
	}

	public long getShiftDistance() {
		return shiftDistance;
	}

	public void setShiftDistance(long shiftDistance) {
		this.shiftDistance = shiftDistance;
	}
	
	
	/**
	 * Sets the time interval by which the masker will shift dates.
	 * <p>
	 * Note: {@link java.util.Calendar} unit fields are used instead of the
	 * {@link java.util.concurrent.TimeUnit} enum. Even though an enum would be strongly
	 * prefered, {@code TimeUnit} doesn't encompass time intervals greater than a day and
	 * and its conversion methods cannot take DST, leap years, etc. into account.  
	 * 
	 * @param shiftDistance The amount of time units of time the dates will be shifted.
	 * @param timeUnit A unit field number as specified by {@link java.util.Calendar}.
	 */
	public void setShiftDistance(int shiftDistance, int timeUnit) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(0);
		calendar.add(timeUnit, shiftDistance);
		
		this.shiftDistance = calendar.getTimeInMillis();
	}

}
