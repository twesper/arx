package org.deidentifier.arx;

/**
 * Implementing classes are able to convert data of type T from and to a String representation.
 * The implementing class should ensure that the mapping from text to data is sound and
 * converting back and forth does not change the data.
 * 
 * @author Tobias Wesper
 *
 * @param <T> The type of the parsed data.
 */
public interface IDataParser<T> {
	
	public abstract T 		fromString(String s);
	public abstract String	toString(T t);
	
}
