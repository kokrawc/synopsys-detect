package com.synopsys.integration.detect.workflow.bdio.post.process;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.dependency.ProjectDependency;

// TODO: Add to list of post processors. Perhaps make Optional post processors like this configurable via an Enum property.
// Only needed for "upgraded" detectors, none of which exist at the moment.
// Stop-Gap measure for IDETECT-2779/HUB-34518. Deprecate when Black Duck can accurately render subproject graphs.
public class SubprojectToRootPostProcessor extends DependencyGraphPostProcessor {
    @Override
    protected void visitDependency(DependencyGraph graph, @Nullable Dependency parentDependency, Dependency visitedDependency) {
        if (visitedDependency instanceof ProjectDependency && graph.getDirectDependencies().stream().noneMatch(visitedDependency::equals)) {
            graph.addDirectDependency(visitedDependency);
        }
    }
}
