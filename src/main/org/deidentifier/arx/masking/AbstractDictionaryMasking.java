package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;

/**
 * TEST
 * @author Tobi
 *
 * @param <T>
 */
public abstract class AbstractDictionaryMasking<T extends DataType<?>> {
	
	public abstract String[] mask(String[] input);
}
