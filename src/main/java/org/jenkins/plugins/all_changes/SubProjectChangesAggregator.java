/*
 * The MIT License
 *
 * Copyright (c) 2011, Stefan Wolf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
        List<AbstractProject<?, ?>> subProjects = new ArrayList<AbstractProject<?, ?>>();
        if (project instanceof FreeStyleProject) {
            FreeStyleProject proj = (FreeStyleProject) project;
            List<Builder> builders = proj.getBuilders();
            for (Builder builder : builders) {
                if (builder instanceof TriggerBuilder) {
                    TriggerBuilder tBuilder = (TriggerBuilder) builder;
                    for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {
                        for (AbstractProject<?, ?> abstractProject : config.getProjectList()) {
                            if (config.getBlock() != null) {
                                subProjects.add(abstractProject);
                            }
                        }
                    }
                }
            }
        }
        ArrayList<AbstractBuild> builds = new ArrayList<AbstractBuild>();
        for (AbstractProject<?, ?> subProject : subProjects) {
            RunList<? extends AbstractBuild<?, ?>> subBuildsDuringBuild = subProject.getBuilds().byTimestamp(build.getTimeInMillis(), build.getTimeInMillis() + build.getDuration());
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
