package org.deidentifier.arx.masking;

import org.deidentifier.arx.IDataParser;

/**
 * Performs data masking on 
 * 
 * @author Tobias Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractInstanceMasker<T> {
	
	public String maskString(String input, IDataParser<T> parser) {
		return parser.toString(mask(parser.fromString(input)));
	}
	
	public abstract T mask(T input);
}
