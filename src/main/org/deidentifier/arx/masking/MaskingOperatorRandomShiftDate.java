package org.deidentifier.arx.masking;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Example for a non-generic masking operator on instance level
 */
public class MaskingOperatorRandomShiftDate extends AbstractInstanceMaskingDate{

	@Override
	protected Date maskInternal(Date input) {
		
		
		Calendar calendar = new GregorianCalendar();
		
		/*
		 * RETURN RANDOM SHIFTED DATE
		 */
		return null;
	}

}
