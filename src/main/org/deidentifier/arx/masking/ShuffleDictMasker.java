package org.deidentifier.arx.masking;

import java.util.Collections;
import java.util.List;

public class ShuffleDictMasker<T> extends AbstractDictionaryMasker<T> {

	@Override
	public void maskList(List<T> data) {
		Collections.shuffle(data, new Random());
	}

}
