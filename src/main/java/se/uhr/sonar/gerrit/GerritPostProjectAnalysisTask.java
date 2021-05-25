package se.uhr.sonar.gerrit;

import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
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
						vote(projectAnalysis, r, name);
					} else {
						LOGGER.debug("Gerrit pull request votes are disabled");
					}
				})));
			}
		});
	}

	private void vote(ProjectAnalysis projectAnalysis, String revision, String pullRequestId) {
		QualityGate qualityGate = projectAnalysis.getQualityGate();

		if (qualityGate != null) {
			gerritClient.vote(projectAnalysis.getProject(), revision, pullRequestId,
					qualityGate.getStatus() == Status.OK ? Score.OK : Score.ERROR);

		} else {
			LOGGER.error("No quality gate present for pull request: {}", pullRequestId);
		}
	}
}
