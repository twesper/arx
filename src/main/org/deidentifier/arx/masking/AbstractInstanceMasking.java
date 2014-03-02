package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;

/**
 * 
 * @author Tobi
 *
 * @param <T>
 * @param <S>
 * Requires both parameters because of type erasure
 */
public abstract class AbstractInstanceMasking<T extends DataType<S>, S> {
	
	public String mask(String input) {
		
		
	}
	
	public abstract S maskInternal(S input);
}
