package se.uhr.sonar.gerrit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.sonar.api.ce.posttask.Project;

class MessageTest {

	private final static String message =
			"Sonar review success!, see http://myurl.com/dashboard?id=org.apache.maven:commons-math&pullRequest=42996";

	@Test
	void shouldDoNothingWitnoutVariables() throws Exception {
		Project project = mock(Project.class);
		when(project.getKey()).thenReturn("");
		when(project.getName()).thenReturn("");

		assertThat(Message.evaluate(message, project, "41704")).isEqualTo(message);
	}

	@Test
	void shouldEvaluateVariables() throws Exception {
		Project project = mock(Project.class);
		when(project.getKey()).thenReturn("org.apache.maven:commons-math");
		when(project.getName()).thenReturn("commons-math");

		assertThat(Message.evaluate(
				"Sonar review success!, see http://myurl.com/dashboard?id=${project.key}&pullRequest=${pullrequest.key}", project,
				"42996")).isEqualTo(message);
	}
}
