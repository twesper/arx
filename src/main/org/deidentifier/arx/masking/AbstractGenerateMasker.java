package org.deidentifier.arx.masking;

/**
 * Performs data masking by generating values independent of the data to be masked.  
 * @author Wesper
 *
 * @param <T>
 */
public abstract class AbstractGenerateMasker<T> extends AbstractInstBasedDictMasker<T> {

	@Override
	public T mask(T input) {
		return generate();
	}
	
	public abstract T generate();

}
