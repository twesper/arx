package org.deidentifier.arx.masking;

import java.util.List;

import org.deidentifier.arx.IDataParser;

/**
 * Performs data masking on a dictionary of values of type T by applying instance masking on
 * each value. Inheriting classes need only implement the {@code mask()} method for
 * instance-based data masking. {@code maskList()} is automatically provided and uses the
 * instance-based method for each element in the dictionary.
 * 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractInstBasedDictMasker<T>
					extends		AbstractDictionaryMasker<T>
					implements 	IInstanceMasker<T> {
	
	/**
	 * Masks the given dictionary one data instance at a time by calling the {@code mask}
	 * method for each element of the list. 
	 */
	@Override
	public void maskList(List<T> data) {
		for(int i = 0; i < data.size(); ++i)
			data.set(i, mask(data.get(i)));
	}
	
	/**
	 * Interprets the input string as a data instance of type T, performs data masking on it and
	 * returns the masked data, converted back to a string.
	 * @param input The string representing a data instance.
	 * @param parser The parser used to interpret the string as data - usually a DataType<T>.
	 * @return The string representing the masked data.
	 */
	public String maskString(String input, IDataParser<T> parser) {
		return parser.toString(mask(parser.fromString(input)));
	}
}
