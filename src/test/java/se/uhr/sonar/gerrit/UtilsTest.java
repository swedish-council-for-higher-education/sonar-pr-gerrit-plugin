package se.uhr.sonar.gerrit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void shouldEvaluateMessage() throws Exception {
		assertThat(Utils.evaluateMessage("Sonar review success, see ${sonar.pr.url}", "http://myurl.com"))
				.endsWith("see http://myurl.com");
	}

	@Test
	void shouldCreateUrl() throws Exception {
		assertThat(Utils.createPullRequestUrl("http://myurl.com", "com.myorg:myproj", "41704"))
				.isEqualTo("http://myurl.com/code?id=com.myorg:myproj&pullRequest=41704");
	}
}
