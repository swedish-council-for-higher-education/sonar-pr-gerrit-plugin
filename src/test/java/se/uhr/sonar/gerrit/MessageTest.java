package se.uhr.sonar.gerrit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MessageTest {

	private final static String message =
			"Sonar review success!, see http://myurl.com/dashboard?id=org.apache.maven:commons-math&pullRequest=42996";

	@Test
	void shouldEvaluateVariables() {
		assertThat(Message.evaluate(
				"Sonar review success!, see http://myurl.com/dashboard?id=${project.key}&pullRequest=${pullrequest.key}",
				Map.of("project.key", "org.apache.maven:commons-math", "pullrequest.key", "42996"))).isEqualTo(message);
	}

	@Test
	void shouldDoNothingWitnoutVariables() {
		assertThat(Message.evaluate(message, Map.of())).isEqualTo(message);
	}

	@Test
	void shouldHandleNonExistingVariable() {
		assertThat(Message.evaluate("start${nonexisting}end", Map.of())).isEqualTo("startend");
	}

	@Test
	void shouldEscapeReplacementValues() {
		assertThat(Message.evaluate("Value:${val}", Map.of("val", "a$b\\c"))).isEqualTo("Value:a$b\\c");
	}
}
