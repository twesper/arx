package org.deidentifier.arx.masking;

import org.deidentifier.arx.IDataParser;

/**
 * Performs data masking on single instances of data of type T.
 * The implementation for the masking must be provided in the {@code mask} method.
 * 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractInstanceMasker<T> implements IInstanceMasker<T> {
	
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
	
	/**
	 * Masks the input data.
	 * 
	 * @param input The data instance.
	 * @return The masked data.
	 */
	@Override
	public abstract T mask(T input);
}
