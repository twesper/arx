package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;

/**
 * Example for a non-generic instance-based operator
 */
public class MaskingOperatorInstanceConstantShift<T extends DataType<?>> extends AbstractInstanceMasking<T> {

	public MaskingOperatorInstanceConstantShift(int shiftDistance) {
		this.shiftDistance	= shiftDistance; 
	}
	
	@Override
	public String mask(String input) {
		
		if(
			return maskDecimal(input);
		if(dataType.toString().equals(DataType.DATE.toString()))
			throw new UnsupportedOperationException("Not yet implemented.");
		
		throw new IllegalStateException("Data type "+dataType.toString()+" not supported by shift masking operator.");
	}
	
	private String maskDecimal(String input)
	{
		
		return null;
	}

	protected int shiftDistance;
}
