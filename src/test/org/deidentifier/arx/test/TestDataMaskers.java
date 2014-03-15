package org.deidentifier.arx.test;

import java.util.Arrays;

import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.masking.ConstantShiftDateMasker;
import org.deidentifier.arx.masking.ConstantShiftDecimalMasker;
import org.deidentifier.arx.masking.ShuffleMasker.ShuffleStringMasker;
import org.deidentifier.arx.masking.SplitAndReplaceStringMasker;
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
		ShuffleStringMasker masker = new ShuffleStringMasker();
		masker.maskArray(dict);
		
		//System.out.println();
		//System.out.print(Arrays.toString(dict));
	}
	
	@Test
	public void testStringSplittingAndJoining() {
		String input = "john.doe@email.com";
		SplitAndReplaceStringMasker masker =
				new SplitAndReplaceStringMasker("[@.]", "*", 2, true);
		
		String correctOutput = "john.doe@*****.com";
		String observedOutput = masker.mask(input);
		
		System.out.println(masker.mask(input));
		assertTrue(correctOutput.equals(observedOutput));
		
		
		masker.setRegEx("@");
		masker.setReplacementString("[REDACTED]");
		masker.setReplaceGroup(0);
		masker.setReplacingEachCharacter(false);
		
		correctOutput = "[REDACTED]@email.com";
		observedOutput = masker.mask(input);
		
		System.out.println(masker.mask(input));
		assertTrue(correctOutput.equals(observedOutput));
	}

}
