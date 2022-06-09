package com.synopsys.integration.detect.workflow.bdio.post.process;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;

public abstract class DependencyGraphPostProcessor {

    protected abstract void visitDependency(DependencyGraph graph, @Nullable Dependency parentDependency, Dependency visitedDependency);

    public void applyPostProcessing(DependencyGraph graph) {
        walkGraph(graph, (parent, dependency) -> visitDependency(graph, parent, dependency));
    }

    private void walkGraph(DependencyGraph graph, DependencyVisitor dependencyVisitor) {
        Set<Dependency> visited = new HashSet<>();
        Set<Dependency> rootDependencies = graph.getRootDependencies();
        rootDependencies.forEach(rootDependency -> visitDependencyAndChildren(graph, null, rootDependency, visited, dependencyVisitor));
    }

    private void visitDependencyAndChildren(
        DependencyGraph graph,
        @Nullable Dependency parent,
        Dependency dependency,
        Set<Dependency> visited,
        DependencyVisitor dependencyVisitor
    ) {
        if (visited.contains(dependency)) {
            return;
        }
        dependencyVisitor.visit(parent, dependency);
        visited.add(dependency);
        Set<Dependency> childrenForParent = graph.getChildrenForParent(dependency);
        childrenForParent.forEach(child -> visitDependencyAndChildren(graph, parent, child, visited, dependencyVisitor));
    }

}
