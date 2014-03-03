package org.deidentifier.arx.masking;

import org.deidentifier.arx.InterfaceDataParser;

/**
 * 
 * @author Tobias Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractInstanceMasking<T> {
	
	public String mask(String input, InterfaceDataParser<T> parser) {
		return parser.toString(maskInternal(parser.fromString(input)));
	}
	
	public abstract T maskInternal(T input);
}
