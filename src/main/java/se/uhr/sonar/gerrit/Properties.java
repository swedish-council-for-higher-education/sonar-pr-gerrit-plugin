package se.uhr.sonar.gerrit;

public enum Properties {

	GERRIT_VOTES_ENABLED("enabled"),
	SONAR_BASE_URL("sonar.url"),
	GERRIT_URL("gerrit.url"),
	GERRIT_USER("gerrit.user"),
	GERRIT_PASSWD("gerrit.passwd"),
	GERRIT_REVIEW_LABEL("gerrit.label"),
	GERRIT_REVIEW_SUCCESS_MESSAGE("gerrit.success.message"),
	GERRIT_REVIEW_FAILED_MESSAGE("gerrit.failed.message");

	private final String key;

	private Properties(String key) {
		this.key = key;
	}

	public String key() {
		return "gerrit.pr." + key;
	}
}
