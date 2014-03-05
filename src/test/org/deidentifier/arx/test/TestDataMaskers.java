package org.deidentifier.arx.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;

import static java.util.Calendar.*; // For month and weekday names, etc.

import java.util.GregorianCalendar;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.masking.ConstantShiftDateDictMasker;
import org.deidentifier.arx.masking.ConstantShiftDoubleDictMasker;
import org.junit.Test;

public class TestDataMaskers {

	@Test
	public void testConstantShiftMaskers() {
		
		/*
		 * Double data type
		 */
		
		// Generate test data:
		double doubleShift = 1.0;
		String[] inputArray = { "1.0",
								"10.0",
								"-1.5",
								"999999",
								"-1000000" };
		// Generate result data:
		Double[] correctResults = { 1.0 + doubleShift,
									10.0 + doubleShift,
									-1.5 + doubleShift,
									999999 + doubleShift,
									-1000000 + doubleShift };
		String[] correctArray = new String[5];
		
		for (int i = 0; i < correctResults.length; ++i)
			correctArray[i] = Double.toString(correctResults[i]);
		
		// Create output data using masker class:
		ConstantShiftDoubleDictMasker dataMasker =
				new ConstantShiftDoubleDictMasker(doubleShift);
		String[] outputArray = dataMasker.maskStrings(inputArray, DataType.DECIMAL);
		

		assertTrue(Arrays.deepEquals(correctArray, outputArray));
		
		
		// Modify shift and test anew:
		doubleShift = -2.5;
		dataMasker.setShiftDistance(doubleShift);
		
		// Generate results:
		Double[] correctResults2 = { 1.0 + doubleShift,
				10.0 + doubleShift,
				-1.5 + doubleShift,
				999999 + doubleShift,
				-1000000 + doubleShift };
		
		for (int i = 0; i < correctResults2.length; ++i)
			correctArray[i] = Double.toString(correctResults2[i]);
		
		// Create output:
		outputArray = dataMasker.maskStrings(inputArray, DataType.DECIMAL);
		
		
		assertTrue(Arrays.deepEquals(correctArray, outputArray));
		
		
		/*
		 * Date data type
		 */

		int dateShift = 3;
		int dateUnit = DAY_OF_MONTH;
		
		Calendar[] calendarArray ={	new GregorianCalendar(1980, JULY, 2),
									new GregorianCalendar(1890, JULY, 3),
									new GregorianCalendar(2020, JULY, 4),
									new GregorianCalendar(0, JANUARY, 1),
									new GregorianCalendar(1970, JANUARY, 1) };
		
		Date[] dateArray = new Date[5];
		for (int i = 0; i < calendarArray.length; ++i)
			dateArray[i] = calendarArray[i].getTime();
		
		Calendar[] correctCalArray = {	new GregorianCalendar(1980, JULY, 5),
										new GregorianCalendar(1890, JULY, 6),
										new GregorianCalendar(2020, JULY, 7),
										new GregorianCalendar(0, JANUARY, 4),
										new GregorianCalendar(1970, JANUARY, 4) };
		Date[] correctDateArray = new Date[5];
		for (int i = 0; i < correctCalArray.length; ++i)
			correctDateArray[i] = correctCalArray[i].getTime();
		
		ConstantShiftDateDictMasker dateMasker =
				new ConstantShiftDateDictMasker(dateShift, dateUnit);
		dateMasker.maskArray(dateArray);
		
		
		assertTrue(Arrays.deepEquals(correctDateArray, dateArray));
	}

}
