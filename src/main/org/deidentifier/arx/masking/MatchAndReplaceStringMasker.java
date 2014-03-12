package org.deidentifier.arx.masking;

import java.util.regex.Pattern;

public class MatchAndReplaceStringMasker extends AbstractInstBasedDictMasker<String> {

	Pattern regEx;
	String	replacementString;
	
	public MatchAndReplaceStringMasker(String regEx, String replacementString) {
		// TODO implement
	}
	
	@Override
	public String mask(String input) {
		// TODO implement
		return null;
	}

}
