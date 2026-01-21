package se.uhr.sonar.gerrit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester.newBranchBuilder;
import static org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester.newCeTaskBuilder;
import static org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester.newConditionBuilder;
import static org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester.newProjectBuilder;
import static org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester.newQualityGateBuilder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.posttask.PostProjectAnalysisTaskTester;

import se.uhr.sonar.gerrit.GerritClient.Score;

@ExtendWith(MockitoExtension.class)
class GerritPostProjectAnalysisTaskTest {

	private static final String PROJECT_NAME = "projname";
	private static final String PROJECT_KEY = "projkey";
	private static final String REVISION = "9e322da0856b0d938c4db214e53bb8d848585406";
	private static final String PR_ID = "41704";
	private static final String ANALYSIS_UUID = "uuid";

	private static final Date NOW = new Date();

	@Mock
	GerritClient gerritClient;

	@Mock
	Configuration configuration;

	@InjectMocks
	GerritPostProjectAnalysisTask postProjectAnalysisTask;

	@Test
	void shouldVoteEorrorOnFailure() {

		when(configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key())).thenReturn(Optional.of(true));

		PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
				.withCeTask(newCeTaskBuilder().setId("id").setStatus(CeTask.Status.SUCCESS).build())
				.withProject(newProjectBuilder().setUuid("uuid").setKey(PROJECT_KEY).setName(PROJECT_NAME).build())
				.at(NOW)
				.withAnalysisUuid(ANALYSIS_UUID)
				.withRevision(REVISION)
				.withBranch(newBranchBuilder().setName(PR_ID).setType(Branch.Type.PULL_REQUEST).build())
				.withQualityGate(newQualityGateBuilder().setId("id").setName("name").setStatus(QualityGate.Status.ERROR).build())
				.execute();

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

		verify(gerritClient).vote(projectCaptor.capture(), eq(REVISION), eq(PR_ID), eq(Score.ERROR), anyMap());

		assertThat(projectCaptor.getValue().getKey()).isEqualTo(PROJECT_KEY);
		assertThat(projectCaptor.getValue().getName()).isEqualTo(PROJECT_NAME);
	}

	@Test
	void shouldVoteOkOnSuccess() {

		when(configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key())).thenReturn(Optional.of(true));

		PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
				.withCeTask(newCeTaskBuilder().setId("id").setStatus(CeTask.Status.SUCCESS).build())
				.withProject(newProjectBuilder().setUuid("uuid").setKey(PROJECT_KEY).setName(PROJECT_NAME).build())
				.at(NOW)
				.withAnalysisUuid(ANALYSIS_UUID)
				.withRevision(REVISION)
				.withBranch(newBranchBuilder().setName(PR_ID).setType(Branch.Type.PULL_REQUEST).build())
				.withQualityGate(newQualityGateBuilder().setId("id").setName("name").setStatus(QualityGate.Status.OK).build())
				.execute();

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

		verify(gerritClient).vote(projectCaptor.capture(), eq(REVISION), eq(PR_ID), eq(Score.OK), anyMap());

		assertThat(projectCaptor.getValue().getKey()).isEqualTo(PROJECT_KEY);
		assertThat(projectCaptor.getValue().getName()).isEqualTo(PROJECT_NAME);
	}

	@Test
	void shouldFilterConditionsWithoutValue() {

		when(configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key())).thenReturn(Optional.of(true));

		PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
				.withCeTask(newCeTaskBuilder().setId("id").setStatus(CeTask.Status.SUCCESS).build())
				.withProject(newProjectBuilder().setUuid("uuid").setKey(PROJECT_KEY).setName(PROJECT_NAME).build())
				.at(NOW)
				.withAnalysisUuid(ANALYSIS_UUID)
				.withRevision(REVISION)
				.withBranch(newBranchBuilder().setName(PR_ID).setType(Branch.Type.PULL_REQUEST).build())
				.withQualityGate(newQualityGateBuilder().setId("id").setName("name").setStatus(QualityGate.Status.OK).
						add(newConditionBuilder().setMetricKey("key").setOperator(QualityGate.Operator.GREATER_THAN).setErrorThreshold("A").buildNoValue()).build())
				.execute();

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

		verify(gerritClient).vote(projectCaptor.capture(), eq(REVISION), eq(PR_ID), eq(Score.OK), anyMap());

		assertThat(projectCaptor.getValue().getKey()).isEqualTo(PROJECT_KEY);
		assertThat(projectCaptor.getValue().getName()).isEqualTo(PROJECT_NAME);
	}

	@Test
	void shouldNotVoteWhenDisabled() {

		when(configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key())).thenReturn(Optional.of(false));

		PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
				.withCeTask(newCeTaskBuilder().setId("id").setStatus(CeTask.Status.SUCCESS).build())
				.withProject(newProjectBuilder().setUuid("uuid").setKey(PROJECT_KEY).setName(PROJECT_NAME).build())
				.at(NOW)
				.withAnalysisUuid(ANALYSIS_UUID)
				.withRevision(REVISION)
				.withBranch(newBranchBuilder().setName(PR_ID).setType(Branch.Type.PULL_REQUEST).build())
				.withQualityGate(newQualityGateBuilder().setId("id").setName("name").setStatus(QualityGate.Status.OK).build())
				.execute();

		verify(gerritClient, times(0)).vote(any(Project.class), anyString(), anyString(), any(Score.class), anyMap());
	}

	@Test
	void shouldHandleNullConditions() {

		when(configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key())).thenReturn(Optional.of(true));

		QualityGate qualityGate = newQualityGateBuilder().setId("id").setName("name").setStatus(QualityGate.Status.OK).build();
		QualityGate mockedQualityGate = org.mockito.Mockito.spy(qualityGate);
		when(mockedQualityGate.getConditions()).thenReturn(null);

		PostProjectAnalysisTaskTester.of(postProjectAnalysisTask)
				.withCeTask(newCeTaskBuilder().setId("id").setStatus(CeTask.Status.SUCCESS).build())
				.withProject(newProjectBuilder().setUuid("uuid").setKey(PROJECT_KEY).setName(PROJECT_NAME).build())
				.at(NOW)
				.withAnalysisUuid(ANALYSIS_UUID)
				.withRevision(REVISION)
				.withBranch(newBranchBuilder().setName(PR_ID).setType(Branch.Type.PULL_REQUEST).build())
				.withQualityGate(mockedQualityGate)
				.execute();

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<String, String>> variablesCaptor = ArgumentCaptor.forClass(Map.class);

		verify(gerritClient).vote(projectCaptor.capture(), eq(REVISION), eq(PR_ID), eq(Score.OK), variablesCaptor.capture());

		assertThat(projectCaptor.getValue().getKey()).isEqualTo(PROJECT_KEY);
		assertThat(projectCaptor.getValue().getName()).isEqualTo(PROJECT_NAME);
		
		Map<String, String> variables = variablesCaptor.getValue();
		assertThat(variables).containsEntry("project.key", PROJECT_KEY);
		assertThat(variables).containsEntry("project.name", PROJECT_NAME);
		assertThat(variables).containsEntry("pullrequest.key", PR_ID);
		assertThat(variables).doesNotContainKey("key");
	}
}
