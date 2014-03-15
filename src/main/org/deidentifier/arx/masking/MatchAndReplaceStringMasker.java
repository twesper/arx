package org.deidentifier.arx.masking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MatchAndReplaceStringMasker extends AbstractInstBasedDictMasker<String> {

	private Pattern regEx;
	private String	replacementString;
	private boolean replacingAllMatches = true;
	private boolean replacingEachCharacter = false;
	
	/**
	 * Compiles the given regex string and constructs a new masker using the compiled pattern.
	 * The masker will replace all matches of the given regex pattern in the input strings with
	 * the replacement string.
	 * @param regEx The string to be compiled into a regex pattern.
	 * @param replacementString The string by which the masker will replace all matches.
	 * @throws PatternSyntaxException if compilation of the regex string fails.
	 */
	public MatchAndReplaceStringMasker(String regEx, String replacementString)
										throws PatternSyntaxException {
		this(regEx, replacementString, true, false);
	}
	
	/**
	 * Compiles the given regex string and constructs a new masker using the compiled pattern.
	 * @param regEx The string to be compiled into a regex pattern.
	 * @param replacementString The string by which the masker will replace the first or all
	 * matches.
	 * @param replaceAllMatches True if only the first (i.e. leftmost) match of the regular
	 * expression should be replaced.
	 * @param replaceEachCharacter True if each single character of the found matches should be
	 * replaced by the replacement string.
	 * @throws PatternSyntaxException if compilation of the regex string fails.
	 */
	public MatchAndReplaceStringMasker(String regEx, String replacementString,
										boolean replaceAllMatches,
										boolean replaceEachCharacter) {
		this(Pattern.compile(regEx), replacementString, replaceAllMatches, replaceEachCharacter);
	}
	
	/**
	 * Creates a new masker using the given pattern.
	 * @param regEx The regex pattern used to find matches in the input strings.
	 * @param replacementString The string by which the masker will replace the first or all
	 * matches.
	 * @param replaceAllMatches True if only the first (i.e. leftmost) match of the regular
	 * expression should be replaced.
	 * @param replaceEachCharacter True if each single character of the found matches should be
	 * replaced by the replacement string.
	 */
	public MatchAndReplaceStringMasker(Pattern regEx, String replacementString,
										boolean replaceAllMatches,
										boolean replaceEachCharacter) {
		this.regEx					= regEx;
		this.replacementString		= replacementString;
		this.replacingAllMatches	= replaceAllMatches;
		this.replacingEachCharacter	= replaceEachCharacter;
	}
	
	@Override
	public String mask(String input) {
		Matcher matcher = regEx.matcher(input);
		
		
		if(!replacingEachCharacter) {	// In this case, Matcher's methods can be used:
			if(replacingAllMatches)
				return matcher.replaceAll(replacementString);
			else
				return matcher.replaceFirst(replacementString);
		}
		
		
		else {	// In this case, the masked string must be built:
			
			StringBuilder maskedString = new StringBuilder();
			int currentIndex = 0; // The current position in the input string.
			
			while (matcher.find()) {	// Find the next matching.
				maskedString.append(input.substring(		// Append the substring before the
						currentIndex, matcher.start()));	// current match.
				int matchedSubstringLength = matcher.end() - matcher.start();
				
				for (int i = 0; i < matchedSubstringLength; ++i)// Add replacementString for 
					maskedString.append(replacementString);		// each character in the match.
				
				currentIndex = matcher.end();	// Update index position.
				
				if(!replacingAllMatches) break;	// Break out if we only want the first match.
			}
			maskedString.append(				// Append the remaining input substring.
					input.substring(currentIndex));
			
			return maskedString.toString();
		}
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

	public boolean isReplacingAllMatches() {
		return replacingAllMatches;
	}

	public void setReplacingAllMatches(boolean replaceAllMatches) {
		this.replacingAllMatches = replaceAllMatches;
	}

	public boolean isReplacingEachCharacter() {
		return replacingEachCharacter;
	}

	public void setReplacingEachCharacter(boolean replacingEachCharacter) {
		this.replacingEachCharacter = replacingEachCharacter;
	}
	
	

}
