package org.jenkins.plugins.all_changes;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import hudson.util.RunList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wolfs
 */
@Extension
public class SubProjectChangesAggregator extends ChangesAggregator {
    @Override
    public List<AbstractBuild> aggregateBuildsWithChanges(AbstractBuild build) {
        AbstractProject project = build.getProject();
        List<AbstractProject<?,?>> subProjects = new ArrayList<AbstractProject<?, ?>>();
        if(project instanceof FreeStyleProject) {
            FreeStyleProject proj = (FreeStyleProject) project;
            List<Builder> builders = proj.getBuilders();
            for (Builder builder : builders) {
                if (builder instanceof TriggerBuilder) {
                    TriggerBuilder tBuilder = (TriggerBuilder) builder;
                    for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {
                        for (AbstractProject<?,?> abstractProject : config.getProjectList()) {
                            if (config.getBlock() != null) {
                                subProjects.add( abstractProject);
                            }
                        }
                    }
                }
            }
        }
        ArrayList<AbstractBuild> builds = new ArrayList<AbstractBuild>();
        for (AbstractProject<?, ?> subProject : subProjects) {
            RunList<? extends AbstractBuild<?,?>> subBuildsDuringBuild = subProject.getBuilds().byTimestamp(build.getTimeInMillis(), build.getTimeInMillis() + build.getDuration());
            for (AbstractBuild<?, ?> subBuild : subBuildsDuringBuild) {
                List<Cause.UpstreamCause> upstreamCauses = new ArrayList<Cause.UpstreamCause>();
                List<Cause> causes = subBuild.getCauses();
                for (Cause cause : causes) {
                    if (cause instanceof Cause.UpstreamCause) {
                        Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                        if (upstreamCause.pointsTo(build)) {
                            builds.add(subBuild);
                        }
                    }
                }
            }
        }

        return builds;
    }
}
