package se.uhr.sonar.gerrit;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.ce.posttask.Project;

public class Message {

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([\\w\\.]+)\\}");

	private Message() {

	}

	public static String evaluate(String message, Project project, String pullRequestId) {
		Map<String, String> variables =
				Map.of("project.key", project.getKey(), "project.name", project.getName(), "pullrequest.key", pullRequestId);

		Matcher matcher = VARIABLE_PATTERN.matcher(message);

		StringBuilder buffer = new StringBuilder();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, variables.get(matcher.group(1)));
		}
		matcher.appendTail(buffer);

		return buffer.toString();
	}
}
