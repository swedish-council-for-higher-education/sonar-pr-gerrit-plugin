package se.uhr.sonar.gerrit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.config.Configuration;

import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gerrit.extensions.api.changes.RevisionApi;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritRestApi;

import se.uhr.sonar.gerrit.GerritClient.Score;

@ExtendWith(MockitoExtension.class)
class GerritClientTest {

	private static final String REVISION = "9e322da0856b0d938c4db214e53bb8d848585406";
	private static final String PR_ID = "41704";

	@Mock
	Project project;

	@Mock
	GerritRestApi gerritRestApi;

	@Mock
	Configuration configuration;

	@InjectMocks
	GerritClient gerritClient;

	@Test
	void shouldVoteMinusOneOnFailure() throws RestApiException {

		when(configuration.get(Properties.GERRIT_REVIEW_LABEL.key())).thenReturn(Optional.of("SONAR_LABEL"));
		when(configuration.get(Properties.GERRIT_REVIEW_SUCCESS_MESSAGE.key())).thenReturn(Optional.of("SUCCESS!"));
		when(configuration.get(Properties.GERRIT_REVIEW_FAILED_MESSAGE.key())).thenReturn(Optional.of("FAILED!"));
		when(configuration.get(Properties.SONAR_BASE_URL.key())).thenReturn(Optional.of("http://mysonar.org"));

		Changes changes = mock(Changes.class);
		when(gerritRestApi.changes()).thenReturn(changes);
		ChangeApi change = mock(ChangeApi.class);
		when(changes.id(PR_ID)).thenReturn(change);
		RevisionApi revision = mock(RevisionApi.class);
		when(change.revision(REVISION)).thenReturn(revision);
		ReviewResult result = new ReviewResult();
		result.labels = new HashMap<String, Short>();
		result.labels.put("SONAR_LABEL", (short) -1);
		ArgumentCaptor<ReviewInput> review = ArgumentCaptor.forClass(ReviewInput.class);
		when(revision.review(review.capture())).thenReturn(result);

		gerritClient.vote(project, REVISION, PR_ID, Score.ERROR);

		assertThat(review.getValue().labels).containsOnly(entry("SONAR_LABEL", (short) -1));
		assertThat(review.getValue().message).isEqualTo("FAILED!");
	}

	@Test
	void shouldVotePlusOneOnSuccess() throws RestApiException {

		when(configuration.get(Properties.GERRIT_REVIEW_LABEL.key())).thenReturn(Optional.of("SONAR_LABEL"));
		when(configuration.get(Properties.GERRIT_REVIEW_SUCCESS_MESSAGE.key())).thenReturn(Optional.of("SUCCESS!"));
		when(configuration.get(Properties.GERRIT_REVIEW_FAILED_MESSAGE.key())).thenReturn(Optional.of("FAILED!"));
		when(configuration.get(Properties.SONAR_BASE_URL.key())).thenReturn(Optional.of("http://mysonar.org"));

		Changes changes = mock(Changes.class);
		when(gerritRestApi.changes()).thenReturn(changes);
		ChangeApi change = mock(ChangeApi.class);
		when(changes.id(PR_ID)).thenReturn(change);
		RevisionApi revision = mock(RevisionApi.class);
		when(change.revision(REVISION)).thenReturn(revision);
		ReviewResult result = new ReviewResult();
		result.labels = new HashMap<String, Short>();
		result.labels.put("SONAR_LABEL", (short) 1);
		ArgumentCaptor<ReviewInput> review = ArgumentCaptor.forClass(ReviewInput.class);
		when(revision.review(review.capture())).thenReturn(result);

		gerritClient.vote(project, REVISION, PR_ID, Score.OK);

		assertThat(review.getValue().labels).containsOnly(entry("SONAR_LABEL", (short) 1));
		assertThat(review.getValue().message).isEqualTo("SUCCESS!");
	}
}
