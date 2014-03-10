package org.deidentifier.arx.test;

import java.util.Arrays;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.masking.ConstantShiftDateMasker;
import org.deidentifier.arx.masking.ConstantShiftDecimalMasker;
import org.deidentifier.arx.masking.ShuffleDictMasker.ShuffleStringDictMasker;
import org.joda.time.Period;
import org.junit.Test;

public class TestDataMaskers extends AbstractTest {
	
	private DataProviderMasking provider;

	public TestDataMaskers() {
		this.provider = new DataProviderMasking();
	}

	@Test
	public void testConstantShiftDecimalInstance() {
		
		double shift = 1.0d;
		DataHandle input = provider.getInputConstantShiftDecimal();
		DataHandle target = provider.getOutputConstantShiftDecimal(input, shift);
		
		ConstantShiftDecimalMasker masker = new ConstantShiftDecimalMasker(shift);
		DataHandle output = provider.getOutputGeneric(input, masker, DataType.DECIMAL);

		assertTrue(Arrays.deepEquals(iteratorToArray(target.iterator()), iteratorToArray(output.iterator())));
	}
		
	@Test
	public void testConstantShiftDateInstance() {

		DataHandle input = provider.getInputConstantShiftDate();
		DataHandle target = provider.getOutputConstantShiftDate();
		
		ConstantShiftDateMasker masker = new ConstantShiftDateMasker(Period.days(3));
		DataHandle output = provider.getOutputGeneric(input, masker, DataType.DATE);
		
		//super.printArray(iteratorToArray(target.iterator()));
		//super.printArray(iteratorToArray(output.iterator()));

		assertTrue(Arrays.deepEquals(iteratorToArray(target.iterator()), iteratorToArray(output.iterator())));
	}
	
	@Test
	public void testShuffleDictionary() {
		DataHandle input = provider.getInputShuffleDictionary();
		
		//super.printArray(iteratorToArray(input.iterator()));
		
		String[] dict = input.getDistinctValues(0);
		ShuffleStringDictMasker masker = new ShuffleStringDictMasker();
		masker.maskArray(dict);
		
		//System.out.println();
		//System.out.print(Arrays.toString(dict));
	}

}
