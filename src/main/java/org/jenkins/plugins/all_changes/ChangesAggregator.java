package org.jenkins.plugins.all_changes;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import java.util.List;

public abstract class ChangesAggregator implements ExtensionPoint {
    public abstract List<AbstractBuild> aggregateBuildsWithChanges(AbstractBuild build);

    public static ExtensionList<ChangesAggregator> all() {
        return Jenkins.getInstance().getExtensionList(ChangesAggregator.class);
    }

}
