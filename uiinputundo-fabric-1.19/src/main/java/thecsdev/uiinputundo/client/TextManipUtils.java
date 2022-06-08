package thecsdev.uiinputundo.client;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class TextManipUtils
{
	// ==================================================
	private TextManipUtils() {}
	// ==================================================
	public static String reverseText(String s) { return new StringBuilder(s).reverse().toString(); }
	
	public static String reverseWords(String input)
	{
		return replaceWords(input, word -> new StringBuilder(word).reverse().toString());
	}
	public static String capitalizeAllWords(String input)
	{
		return replaceWords(input, word -> StringUtils.capitalize(word));
	}
	// ==================================================
	public static String replaceWords(String input, Function<String, String> replacement)
	{
		//compile regex
		Matcher wordMatcher = Pattern.compile("[A-z]*").matcher(input);
		StringBuilder output = new StringBuilder(input);
		
		//iterate all found groups
		while(wordMatcher.find())
		{
			//ignore null and empty groups
			if(StringUtils.isBlank(wordMatcher.group())) continue;
			
			//reverse
			String rWord = replacement.apply(wordMatcher.group());
			output.replace(wordMatcher.start(), wordMatcher.end(), rWord);
		}
		
		//return
		return output.toString();
	}
	// ==================================================
}
