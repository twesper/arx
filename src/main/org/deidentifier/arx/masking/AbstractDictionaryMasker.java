package org.deidentifier.arx.masking;

import org.deidentifier.arx.IDataParser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Performs data masking on whole dictionaries of data of type T.
 * The exact masking implementation must be provided in the {@code maskList} method.
 * 
 * @author Wesper
 *
 * @param <T> The type of data to be masked.
 */
public abstract class AbstractDictionaryMasker<T> implements IDictionaryMasker<T> {
	
	/**
	 * Converts the input String array to data of type T, performs the masking and writes the
	 * results into the input array.
	 * 
	 * @param dataStrings The array of strings containing the dictionary.
	 * @param parser The object used to parse the data - usually of the DataType<T> class.
	 */
	public void maskStrings(String[] dataStrings, IDataParser<T> parser) {
		
		// Convert input strings to T and store in a Vector.
		List<String> stringList = Arrays.asList(dataStrings);
		Vector<T> dataVector = new Vector<T>();
		for (String item : stringList)
			dataVector.add(parser.fromString(item));
		
		// Perform the masking.
		maskList(dataVector);
		
		// Write back to input array.
		for (int i = 0; i < dataVector.size(); ++i)
			dataStrings[i] = parser.toString(dataVector.elementAt(i));
	}
	
	/**
	 * Masks the given array of data elements.
	 * 
	 * @param input The input array containing the dictionary of data to be masked.
	 */
	public void maskArray(T[] input) {
		maskList(Arrays.asList(input));
	}
	
	/**
	 * Masks the given list of data elements.
	 * 
	 * @param data The list containing the dictionary on which the masking is performed.
	 */
	@Override
	public abstract void maskList(List<T> data);
}
