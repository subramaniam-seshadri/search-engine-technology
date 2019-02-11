package cecs429.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

/**
 * A AdvancedTokenProcessor creates terms from tokens by following the below
 * mentioned rules: 1. Removing all alpha-numeric characters from the beginning
 * and end of the token, but not the middle. 2. Remove all apostrophes or
 * quotation marks from anywhere in the string 3. Hyphen rule specified in the
 * documentation 4. Convert to lowercase.
 */

public class AdvancedTokenProcessor implements TokenProcessor {
	HashSet<Character> accentedCharacters = new HashSet<Character>();
	private Class stemClass;
	private static SnowballStemmer stemmer;

	public AdvancedTokenProcessor() throws Throwable {
		super();
		this.stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
		this.stemmer = (SnowballStemmer) stemClass.newInstance();
	}

	@Override
	public List<String> processToken(String token) {
		List<String> processedTokens = new ArrayList<String>();
		String stemmedToken = "";
		// Rule 1 & 4
		token = token.trim();
		token = token.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase();

		// Rule 2
		if (token.contains("\""))
			token = token.replace("\"", "");
		if (token.contains("'"))
			token = token.replace("\'", "");
		// Rule 3
		try {
			if (token.contains("-") && !token.startsWith("-")) { // token is a hyphenated word
				String[] parts = token.split("-");
				String combinedToken = "";
				for (String s : parts) {
					if (s != null && !s.isEmpty()) {
						String sToken = stemTokenJava(
								s.trim().replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase());
						processedTokens.add(sToken);
						combinedToken += sToken;
					}
				}
				processedTokens.add(combinedToken);
			} else { // token is not a hyphenated word.
				stemmedToken = stemTokenJava(token);
				processedTokens.add(stemmedToken);
			}
		} catch (Throwable e1) {
		}
		return processedTokens;
	}

	public static String stemTokenJava(String token) throws Throwable {
		stemmer.setCurrent(token);
		stemmer.stem();
		String stemmedToken = stemmer.getCurrent();
		return stemmedToken;
	}
}
