package se.uhr.sonar.gerrit;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.ce.posttask.QualityGate.Condition;
import org.sonar.api.ce.posttask.QualityGate.Status;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import se.uhr.sonar.gerrit.GerritClient.Score;

public class GerritPostProjectAnalysisTask implements PostProjectAnalysisTask {

	private static final Logger LOGGER = Loggers.get(GerritPostProjectAnalysisTask.class);

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
				branch.getName().ifPresent(name -> projectAnalysis.getAnalysis().ifPresent(a -> a.getRevision().ifPresent(r -> {
					if (enabled) {
						vote(projectAnalysis, r, name, projectAnalysis.getQualityGate().getConditions());
					} else {
						LOGGER.debug("Gerrit pull request votes are disabled");
					}
				})));
			}
		});
	}

	private void vote(ProjectAnalysis projectAnalysis, String revision, String pullRequestId, Collection<Condition> conditions) {
		QualityGate qualityGate = projectAnalysis.getQualityGate();

		if (qualityGate != null) {
			Project project = projectAnalysis.getProject();

			Map<String, String> variables = conditions.stream().collect(Collectors.toMap(Condition::getMetricKey, Condition::getValue));

			variables.put("project.key", project.getKey());
			variables.put("project.name", project.getName());
			variables.put("pullrequest.key", pullRequestId);

			variables.entrySet().stream().forEach(e -> LOGGER.debug("variable {}={}", e.getKey(), e.getValue()));

			gerritClient.vote(projectAnalysis.getProject(), revision, pullRequestId,
					qualityGate.getStatus() == Status.OK ? Score.OK : Score.ERROR, variables);

		} else {
			LOGGER.error("No quality gate present for pull request: {}", pullRequestId);
		}
	}
}
