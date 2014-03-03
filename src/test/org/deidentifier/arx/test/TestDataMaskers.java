package org.deidentifier.arx.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.masking.ConstantShiftDoubleDictionaryMasker;
import org.junit.Test;

public class TestDataMaskers {

	@Test
	public void testConstantShiftMasker() {
		
		// Generate test data:
		double shift = 1.0;
		String[] inputArray = { "1.0",
								"10.0",
								"-1.5",
								"999999",
								"-1000000" };
		// Generate result data:
		Double[] correctResults = { 1.0 + shift,
									10.0 + shift,
									-1.5 + shift,
									999999 + shift,
									-1000000 + shift };
		String[] correctArray = new String[5];
		
		for (int i = 0; i < correctResults.length; ++i)
			correctArray[i] = Double.toString(correctResults[i]);
		
		// Create output data using masker class:
		ConstantShiftDoubleDictionaryMasker dataMasker =
				new ConstantShiftDoubleDictionaryMasker(shift);
		String[] outputArray = dataMasker.maskStrings(inputArray, DataType.DECIMAL);
		

		assertTrue(Arrays.deepEquals(correctArray, outputArray));
		
		
		// Modify shiftDistance and test anew:
		shift = -2.5;
		dataMasker.setShiftDistance(shift);
		
		// Generate results:
		Double[] correctResults2 = { 1.0 + shift,
				10.0 + shift,
				-1.5 + shift,
				999999 + shift,
				-1000000 + shift };
		
		for (int i = 0; i < correctResults2.length; ++i)
			correctArray[i] = Double.toString(correctResults2[i]);
		
		// Create output:
		outputArray = dataMasker.maskStrings(inputArray, DataType.DECIMAL);
		
		
		assertTrue(Arrays.deepEquals(correctArray, outputArray));
	}

}
