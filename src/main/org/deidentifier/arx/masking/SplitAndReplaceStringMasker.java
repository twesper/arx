package org.deidentifier.arx.masking;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SplitAndReplaceStringMasker extends AbstractInstBasedDictMasker<String> {

	private Pattern	regEx;
	private String	replacementString;
	private int		replaceGroup = 0;	// Must be >= 0.
	private boolean	replacingEachCharacter = false;
	
	public SplitAndReplaceStringMasker(String regEx, String replacementString,
										int replaceGroup, boolean replaceEachCharacter)
											throws PatternSyntaxException {
		this(Pattern.compile(regEx), replacementString, replaceGroup, replaceEachCharacter);
	}
	
	public SplitAndReplaceStringMasker(Pattern regEx, String replacementString,
										int replaceGroup, boolean replaceEachCharacter) {
		if (replaceGroup < 0)
			throw new IllegalArgumentException("replaceGroup parameter must be >= 0!");
		this.regEx					= regEx;
		this.replacementString		= replacementString;
		this.replaceGroup			= replaceGroup;
		this.replacingEachCharacter	= replaceEachCharacter;
	}
		
	@Override
	public String mask(String input) {
		Matcher matcher = regEx.matcher(input);
		
		// This list will contain the substrings of the input string.  
		ArrayList<String> matchList = new ArrayList<String>();
		
		// This variable will contain the current position on the input string.
		int currentIndex = 0;
		
		while (matcher.find()) {			// Find the next regex match, then:
			
			matchList.add(input.substring(			// Add an even numbered position in the
				currentIndex, matcher.start()));	// matchList with the substring before
													// the new match.
			matchList.add(input.substring(			// Now add an odd numbered position in
				matcher.start(), matcher.end()));	// matchList with the matched substring,
													// i.e. the delimiter.
			currentIndex = matcher.end();	// Continue with the next match.
		}
		matchList.add(	// Finally, add the substring after the final matching.
			input.substring(currentIndex));
		
		// Return original input if there have not been found enough groups.
		if (replaceGroup * 2 >= matchList.size()) return input;
		
		if(replacingEachCharacter) {
			
			StringBuilder newGroup = new StringBuilder(matchList.get(replaceGroup * 2));
			int startingSize = newGroup.length();
			for (int i = startingSize; i > 0; --i)	// Go backwards through the characters and
				newGroup.replace(i - 1, i, replacementString);	// replace each of them.
			
			matchList.set(replaceGroup * 2, newGroup.toString());
			
		}
		else {
			matchList.set(replaceGroup * 2, replacementString);
		}
		
		StringBuilder maskedString = new StringBuilder();
		for (String s : matchList)
			maskedString.append(s);
		
		return maskedString.toString();
	}

	public Pattern getRegEx() {
		return regEx;
	}

	public void setRegEx(Pattern regEx) {
		this.regEx = regEx;
	}
	
	public void setRegEx(String regEx) throws PatternSyntaxException {
		this.regEx = Pattern.compile(regEx);
	}

	public String getReplacementString() {
		return replacementString;
	}

	public void setReplacementString(String replacementString) {
		this.replacementString = replacementString;
	}

	public int getReplaceGroup() {
		return replaceGroup;
	}

	public void setReplaceGroup(int replaceGroup) {
		this.replaceGroup = replaceGroup;
	}

	public boolean isReplacingEachCharacter() {
		return replacingEachCharacter;
	}

	public void setReplacingEachCharacter(boolean replacingEachCharacter) {
		this.replacingEachCharacter = replacingEachCharacter;
	}

}
