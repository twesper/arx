package org.deidentifier.arx.masking;

import org.deidentifier.arx.IDataParser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Performs data masking on a whole dictionary of Strings representing data of type T.
 * The exact masking implementation must be provided in the {@code maskInternal} method.
 * 
 * @author Tobias Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractDictionaryMasker<T> {
	
	/**
	 * Converts the input String array to data of type T, performs the masking and returns the
	 * results in a new array.
	 * @param input The array containing the dictionary.
	 * @param parser The object used to parse the data - usually of the DataType<T> class.
	 * @return An array of masked data in String representation.
	 */
	public String[] maskStrings(String[] input, IDataParser<T> parser) {
		
		// Convert input strings to data and store in a Vector.
		List<String> inputList = Arrays.asList(input);
		Vector<T> data = new Vector<T>();
		for (String item : inputList)
			data.add(parser.fromString(item));
		
		// Perform the masking.
		maskList(data);
		
		// Convert back to String array.
		String[] output = new String[input.length];
		for (int i = 0; i < data.size(); ++i)
			output[i] = parser.toString(data.elementAt(i));
		
		return output;
	}
	
	public void mask(T[] input) {
		maskList(Arrays.asList(input));
	}
	
	/**
	 * The exact implementation of the masking should be specified here.
	 * @param data The list on which the masking is performed.
	 */
	public abstract void maskList(List<T> data);
}
