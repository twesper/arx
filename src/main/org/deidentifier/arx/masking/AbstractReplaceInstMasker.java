package org.deidentifier.arx.masking;

/**
 * Performs data masking by replacing the data with new values for each data instance,
 * generated independently from the input.
 * @author Wesper
 *
 * @param <T> The type of data to be masked
 */
public abstract class AbstractReplaceInstMasker<T> extends AbstractInstBasedDictMasker<T> {

	@Override
	public T mask(T input) {
		return createReplacement();
	}
	
	/**
	 * Creates a new replacement value.
	 * @return The generated value.
	 */
	public abstract T createReplacement();

}
