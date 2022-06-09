package com.synopsys.integration.detect.workflow.bdio.post.process;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.model.dependency.Dependency;

public interface DependencyVisitor {
    void visit(@Nullable Dependency parentDependency, Dependency visitedDependency);
}
