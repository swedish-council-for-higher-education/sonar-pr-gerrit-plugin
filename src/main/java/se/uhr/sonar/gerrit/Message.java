package se.uhr.sonar.gerrit;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([\\w.]+)}");

	private Message() {

	}

	public static String evaluate(String message, Map<String, String> variables) {
		Matcher matcher = VARIABLE_PATTERN.matcher(message);

		StringBuilder buffer = new StringBuilder();

		while (matcher.find()) {
			String replacement = variables.getOrDefault(matcher.group(1), "");
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}
}
