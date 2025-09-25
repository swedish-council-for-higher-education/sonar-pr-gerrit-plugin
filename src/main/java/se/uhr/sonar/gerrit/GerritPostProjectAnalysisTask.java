package se.uhr.sonar.gerrit;

import static org.sonar.api.ce.posttask.QualityGate.EvaluationStatus.NO_VALUE;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.posttask.Analysis;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.ce.posttask.QualityGate.Condition;
import org.sonar.api.ce.posttask.QualityGate.Status;
import org.sonar.api.config.Configuration;

import se.uhr.sonar.gerrit.GerritClient.Score;

public class GerritPostProjectAnalysisTask implements PostProjectAnalysisTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(GerritPostProjectAnalysisTask.class);

	private final GerritClient gerritClient;

	private final Configuration configuration;

	public GerritPostProjectAnalysisTask(GerritClient gerritClient, Configuration configuration) {
		this.gerritClient = gerritClient;
		this.configuration = configuration;
	}

	@Override
	public void finished(Context context) {
		ProjectAnalysis projectAnalysis = context.getProjectAnalysis();

		boolean enabled = configuration.getBoolean(Properties.GERRIT_VOTES_ENABLED.key()).orElse(false);

		projectAnalysis.getBranch().ifPresent(branch -> {
			if (branch.getType() == Branch.Type.PULL_REQUEST) {
				branch.getName().ifPresent(name -> projectAnalysis.getAnalysis().flatMap(Analysis::getRevision).ifPresent(r -> {
					if (enabled) {
						vote(projectAnalysis, r, name, Objects.requireNonNull(projectAnalysis.getQualityGate()).getConditions());
					} else {
						LOGGER.debug("Gerrit pull request votes are disabled");
					}
				}));
			}
		});
	}

	private void vote(ProjectAnalysis projectAnalysis, String revision, String pullRequestId, Collection<Condition> conditions) {
		QualityGate qualityGate = projectAnalysis.getQualityGate();

		if (qualityGate != null) {
			Project project = projectAnalysis.getProject();

			Map<String, String> variables = conditions.stream()
					.filter(c -> c.getStatus() != NO_VALUE)
					.collect(Collectors.toMap(Condition::getMetricKey, Condition::getValue));

			variables.put("project.key", project.getKey());
			variables.put("project.name", project.getName());
			variables.put("pullrequest.key", pullRequestId);

			variables.forEach((key, value) -> LOGGER.debug("variable {}={}", key, value));

			gerritClient.vote(projectAnalysis.getProject(), revision, pullRequestId,
					qualityGate.getStatus() == Status.OK ? Score.OK : Score.ERROR, variables);

		} else {
			LOGGER.error("No quality gate present for pull request: {}", pullRequestId);
		}
	}

}
