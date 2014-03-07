package org.deidentifier.arx.masking;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Masks a dictionary of data by shuffling its values around.
 * 
 * @author Wesper
 *
 * @param <T> The type of the data to be masked.
 */
public class ShuffleDictMasker<T> extends AbstractDictionaryMasker<T> {

	@Override
	public void maskList(List<T> data) {
		Collections.shuffle(data, new Random());
	}
	
	public static class ShuffleDecimalDictMasker	extends ShuffleDictMasker<Double>	{ };
	public static class ShuffleDateDictMasker		extends ShuffleDictMasker<Date>		{ };
	public static class ShuffleStringDictMasker		extends ShuffleDictMasker<String>	{ };
}