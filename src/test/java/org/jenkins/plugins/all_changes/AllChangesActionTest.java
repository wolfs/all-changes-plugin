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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import hudson.model.AbstractBuild;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author wolfs
 */
public class AllChangesActionTest {

    @Test
    public void getContributingBuildsShouldWorkTransitively() throws Exception {
        AllChangesAction changesAction = new AllChangesAction(null);
        ChangesAggregator aggregatorMock = mock(ChangesAggregator.class);
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractBuild build2 = mock(AbstractBuild.class);
        AbstractBuild build3 = mock(AbstractBuild.class);
        when(aggregatorMock.aggregateBuildsWithChanges(build)).thenReturn(ImmutableList.of(build2));
        when(aggregatorMock.aggregateBuildsWithChanges(build2)).thenReturn(ImmutableList.of(build3));

        changesAction.aggregators = Lists.newArrayList(aggregatorMock);

        Set<AbstractBuild> foundBuilds = changesAction.getContributingBuilds(build);

        assertTrue(foundBuilds.equals(ImmutableSet.of(build, build2, build3)));
    }

    @Test
    public void getContributingBuildsShouldWorkHandleCycles() throws Exception {
        AllChangesAction changesAction = new AllChangesAction(null);
        ChangesAggregator aggregatorMock = mock(ChangesAggregator.class);
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractBuild build2 = mock(AbstractBuild.class);
        AbstractBuild build3 = mock(AbstractBuild.class);
        when(aggregatorMock.aggregateBuildsWithChanges(build)).thenReturn(ImmutableList.of(build2));
        when(aggregatorMock.aggregateBuildsWithChanges(build2)).thenReturn(ImmutableList.of(build3));
        when(aggregatorMock.aggregateBuildsWithChanges(build3)).thenReturn(ImmutableList.of(build));

        changesAction.aggregators = Lists.newArrayList(aggregatorMock);

        Set<AbstractBuild> foundBuilds = changesAction.getContributingBuilds(build);

        assertTrue(foundBuilds.equals(ImmutableSet.of(build, build2, build3)));
    }

    @Test
    public void getContributingBuildsWorksWithMoreThanOneAggregator() throws Exception {
        AllChangesAction changesAction = new AllChangesAction(null);
        ChangesAggregator aggregatorMock = mock(ChangesAggregator.class);
        ChangesAggregator aggregatorMock2 = mock(ChangesAggregator.class);
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractBuild build2 = mock(AbstractBuild.class);
        AbstractBuild build3 = mock(AbstractBuild.class);
        when(aggregatorMock.aggregateBuildsWithChanges(build)).thenReturn(ImmutableList.of(build2));
        when(aggregatorMock2.aggregateBuildsWithChanges(build2)).thenReturn(ImmutableList.of(build3));

        changesAction.aggregators = Lists.newArrayList(aggregatorMock, aggregatorMock2);

        Set<AbstractBuild> foundBuilds = changesAction.getContributingBuilds(build);

        assertTrue(foundBuilds.equals(ImmutableSet.of(build, build2, build3)));
    }
}
