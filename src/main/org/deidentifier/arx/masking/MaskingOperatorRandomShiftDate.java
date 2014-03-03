package org.deidentifier.arx.masking;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Example for a non-generic masking operator on instance level
 */
public class MaskingOperatorRandomShiftDate extends AbstractInstanceMaskingDate {

	@Override
	protected Date maskInternal(Date input) {
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(input);
		// TODO complete
		
		/*
		 * RETURN RANDOM SHIFTED DATE
		 */
		return null;
	}

}
