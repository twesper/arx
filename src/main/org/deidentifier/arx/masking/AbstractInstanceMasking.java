package org.deidentifier.arx.masking;

import org.deidentifier.arx.DataType;

public abstract class AbstractInstanceMasking<T extends DataType<?>> {

	public abstract String mask(String input);
}
