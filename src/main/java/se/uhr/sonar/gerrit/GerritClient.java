package se.uhr.sonar.gerrit;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;

import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApi;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;

@ServerSide
@ComputeEngineSide
public class GerritClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(GerritClient.class);

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

	public void vote(Project project, String revision, String pullRequestId, Score score, Map<String, String> variables) {
		String reviewLabel = configuration.get(Properties.GERRIT_REVIEW_LABEL.key()).orElseThrow();
		String reviewSuccessMessage = configuration.get(Properties.GERRIT_REVIEW_SUCCESS_MESSAGE.key()).orElseThrow();
		String reviewFailureMessage = configuration.get(Properties.GERRIT_REVIEW_FAILED_MESSAGE.key()).orElseThrow();

		ReviewInput reviewInput = new ReviewInput();

		String message = score == Score.OK ? reviewSuccessMessage : reviewFailureMessage;

		reviewInput.message(Message.evaluate(message, variables)).label(reviewLabel, score == Score.OK ? 1 : -1);

		try {
			ReviewResult result = gerritApi.changes().id(pullRequestId).revision(revision).review(reviewInput);

			LOGGER.debug("PR: {}, project {}, vote: {}, revision: {}", pullRequestId, project.getName(), score, revision);

			if (result.labels == null) {
				LOGGER.error("Review on PR {} rejected", pullRequestId);
			}

		} catch (RestApiException e) {
			LOGGER.error("Could not submit review", e);
		}
	}
}
