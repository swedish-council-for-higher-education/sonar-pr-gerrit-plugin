package se.uhr.sonar.gerrit;

import java.util.Arrays;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class GerritPlugin implements Plugin {

	private int index;

	@Override
	public void define(Context context) {
		PropertyDefinition enabled = PropertyDefinition.builder(Properties.GERRIT_VOTES_ENABLED.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.BOOLEAN)
				.defaultValue("true")
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT))
				.index(index++)
				.build();

		PropertyDefinition sonarBaseUrl = PropertyDefinition.builder(Properties.SONAR_BASE_URL.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.index(index++)
				.build();

		PropertyDefinition gerritUrl = PropertyDefinition.builder(Properties.GERRIT_URL.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.index(index++)
				.build();

		PropertyDefinition gerritUser = PropertyDefinition.builder(Properties.GERRIT_USER.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.index(index++)
				.build();

		PropertyDefinition gerritPasswd = PropertyDefinition.builder(Properties.GERRIT_PASSWD.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.PASSWORD)
				.index(index++)
				.build();

		PropertyDefinition reviewLabel = PropertyDefinition.builder(Properties.GERRIT_REVIEW_LABEL.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.defaultValue("Sonar-Verified")
				.index(index++)
				.build();

		PropertyDefinition reviewSuccessMessage = PropertyDefinition.builder(Properties.GERRIT_REVIEW_SUCCESS_MESSAGE.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.defaultValue("Sonar review passed, see ${sonar.pr.url}")
				.index(index++)
				.build();

		PropertyDefinition reviewFailMessage = PropertyDefinition.builder(Properties.GERRIT_REVIEW_FAILED_MESSAGE.key())
				.category(Constants.GERRIT_CATEGORY)
				.type(PropertyType.STRING)
				.defaultValue("Sonar review failed, see ${sonar.pr.url}")
				.index(index++)
				.build();

		context.addExtensions(GerritPostProjectAnalysisTask.class, GerritClient.class, enabled, sonarBaseUrl, gerritUrl, gerritUser,
				gerritPasswd, reviewLabel, reviewSuccessMessage, reviewFailMessage);
	}
}