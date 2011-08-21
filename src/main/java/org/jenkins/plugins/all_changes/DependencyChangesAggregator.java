package org.jenkins.plugins.all_changes;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.List;
import java.util.Map;

/**
 * @author wolfs
 */
@Extension
public class DependencyChangesAggregator extends ChangesAggregator {
    @Override
    public List<AbstractBuild> aggregateBuildsWithChanges(AbstractBuild build) {
        ImmutableList.Builder<AbstractBuild> builder = ImmutableList.<AbstractBuild>builder();
        Map<AbstractProject,AbstractBuild.DependencyChange> depChanges = build.getDependencyChanges((AbstractBuild) build.getPreviousBuild());
        for (AbstractBuild.DependencyChange depChange : depChanges.values()) {
            builder.addAll(depChange.getBuilds());
        }
        return builder.build();
    }
}
