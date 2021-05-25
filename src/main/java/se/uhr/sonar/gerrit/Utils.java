package se.uhr.sonar.gerrit;

public class Utils {

	private Utils() {

	}

	public static String createPullRequestUrl(String base, String projectKey, String pullRequest) {
		return base + "/code?id=" + projectKey + "&pullRequest=" + pullRequest;
	}

	public static String evaluateMessage(String message, String pullRequestUrl) {
		return message.replace("${" + Constants.MESSAGE_PR_URL_SUBS + "}", pullRequestUrl);
	}
}
