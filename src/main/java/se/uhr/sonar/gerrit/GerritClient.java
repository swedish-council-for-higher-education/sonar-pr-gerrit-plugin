package se.uhr.sonar.gerrit;

import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApi;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;

@ServerSide
@ComputeEngineSide
public class GerritClient {

	private static final Logger LOGGER = Loggers.get(GerritClient.class);

	private final Configuration configuration;
	private final GerritRestApi gerritApi;

	public enum Score {
		OK,
		ERROR
	}

	public GerritClient(Configuration configuration) {
		this(configuration,
				new GerritRestApiFactory().create(new GerritAuthData.Basic(configuration.get(Properties.GERRIT_URL.key()).orElseThrow(),
						configuration.get(Properties.GERRIT_USER.key()).orElseThrow(),
						configuration.get(Properties.GERRIT_PASSWD.key()).orElseThrow())));
	}

	GerritClient(Configuration configuration, GerritRestApi gerritApi) {
		this.configuration = configuration;
		this.gerritApi = gerritApi;
	}

	public void vote(Project project, String revision, String pullRequestId, Score score) {
		String reviewLabel = configuration.get(Properties.GERRIT_REVIEW_LABEL.key()).orElseThrow();
		String reviewSuccessMessage = configuration.get(Properties.GERRIT_REVIEW_SUCCESS_MESSAGE.key()).orElseThrow();
		String reviewFailureMessage = configuration.get(Properties.GERRIT_REVIEW_FAILED_MESSAGE.key()).orElseThrow();
		String sonarBaseUrl = configuration.get(Properties.SONAR_BASE_URL.key()).orElseThrow();

		ReviewInput reviewInput = new ReviewInput();

		String pullRequestUrl = Utils.createPullRequestUrl(sonarBaseUrl, project.getKey(), pullRequestId);

		String message = score == Score.OK ? reviewSuccessMessage : reviewFailureMessage;

		reviewInput.message(Utils.evaluateMessage(message, pullRequestUrl)).label(reviewLabel, score == Score.OK ? 1 : -1);

		try {
			ReviewResult result = gerritApi.changes().id(pullRequestId).revision(revision).review(reviewInput);

			LOGGER.debug("PR: {}, project {}, vote: {}, reversion: {}, link: {}", pullRequestId, project.getName(), score, revision,
					pullRequestUrl);

			if (result.labels == null) {
				LOGGER.error("Review on PR {} rejected", pullRequestId);
			}

		} catch (RestApiException e) {
			LOGGER.error("Could not submit review", e);
		}
	}
}
