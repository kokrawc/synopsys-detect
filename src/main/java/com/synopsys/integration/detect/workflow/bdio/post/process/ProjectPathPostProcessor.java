package com.synopsys.integration.detect.workflow.bdio.post.process;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.dependency.ProjectDependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;

// Removes the sourcePath from all ProjectDependency nodes that include it.
public class ProjectPathPostProcessor extends DependencyGraphPostProcessor {
    private final File sourcePath;

    public ProjectPathPostProcessor(File sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    protected void visitDependency(DependencyGraph graph, @Nullable Dependency parentDependency, Dependency visitedDependency) {
        if (visitedDependency instanceof ProjectDependency) {
            String source = sourcePath.getAbsolutePath();
            if (visitedDependency.getName().contains(source)) {
                Set<Dependency> visitedDependencyChildren = graph.getChildrenForParent(visitedDependency);

                // Change the ExternalId to relativize the path
                String newName = StringUtils.remove(visitedDependency.getName(), source);
                newName = StringUtils.removeStart(newName, "/");
                newName = StringUtils.replace(newName, ".", "_");

                Forge forge = visitedDependency.getExternalId().getForge();
                ExternalId newExternalId = ExternalId.FACTORY.createPathExternalId(forge, newName);

                visitedDependency.setName(newName);
                visitedDependency.setExternalId(newExternalId);

                // Restore relationships with new ExternalId
                graph.addParentWithChildren(visitedDependency, visitedDependencyChildren);
                if (parentDependency != null) {
                    graph.addParentWithChild(parentDependency, visitedDependency);
                }
                // TODO: Currently no way to remove the stale relationships and dependencies from the graph.
                //      So the orphaned Dependencies and Relationships will exist in memory until the graph is done being used.

                // TODO: Does the above mean orphaned components are still written to the BDIO2 document?
            }
        }
    }

}
