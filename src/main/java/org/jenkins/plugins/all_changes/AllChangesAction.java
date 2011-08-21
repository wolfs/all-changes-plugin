package org.jenkins.plugins.all_changes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.scm.ChangeLogSet;

import java.util.Set;

/**
 * Action to calculate all changes for a build
 * It uses ChangesAggregators to to so.
 * @author wolfs
 */
public class AllChangesAction implements Action {

    private AbstractProject<?, ?> project;

    AllChangesAction(AbstractProject<?, ?> project) {
        this.project = project;
    }
    public String getIconFileName() {
        return "notepad.png";
    }

    public String getDisplayName() {
        return Messages.AllChanges_allChanges();
    }

    public String getUrlName() {
        return "all-changes";
    }

    /**
     * Returns all changes which contribute to a build.
     * @param build
     * @return
     */
    public static Multimap<ChangeLogSet.Entry, AbstractBuild> getAllChanges(AbstractBuild build) {
        Set<AbstractBuild> builds = getContributingBuilds(build);
        Multimap<String,ChangeLogSet.Entry> changes = ArrayListMultimap.create();
        for (AbstractBuild changedBuild : builds) {
            ChangeLogSet<ChangeLogSet.Entry> changeSet = changedBuild.getChangeSet();
            for (ChangeLogSet.Entry entry : changeSet) {
                changes.put(entry.getCommitId()+entry.getMsgAnnotated()+entry.getTimestamp(), entry);
            }
        }
        Multimap<ChangeLogSet.Entry, AbstractBuild> change2Build = HashMultimap.create();
        for (String changeKey : changes.keySet()) {
            ChangeLogSet.Entry change = changes.get(changeKey).iterator().next();
            for (ChangeLogSet.Entry entry : changes.get(changeKey)) {
                change2Build.put(change, entry.getParent().build);
            }
        }
        return change2Build;
    }

    /**
     * Uses all ChangesAggregators to calculate the contributing builds
     * @return all changes which contribute to the given build
     */
    public static Set<AbstractBuild> getContributingBuilds(AbstractBuild build) {
        Set<AbstractBuild> builds = Sets.newHashSet();
        builds.add(build);
        int size = 0;
        // Saturate the build Set
        do {
            size = builds.size();
            Set<AbstractBuild> newBuilds = Sets.newHashSet();
            for (ChangesAggregator aggregator : ChangesAggregator.all()) {
                for (AbstractBuild abstractBuild : builds) {
                    newBuilds.addAll(aggregator.aggregateBuildsWithChanges(build));
                }
            }
            builds.addAll(newBuilds);
        } while (size < builds.size());
        return builds;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
