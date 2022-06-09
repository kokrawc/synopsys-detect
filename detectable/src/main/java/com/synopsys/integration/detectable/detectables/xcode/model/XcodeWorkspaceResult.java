package com.synopsys.integration.detectable.detectables.xcode.model;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.BasicDependencyGraph;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.detectable.result.FailedDetectableResult;

public class XcodeWorkspaceResult {
    private final DependencyGraph dependencyGraph;
    private final List<FailedDetectableResult> failedDetectableResults;

    public static XcodeWorkspaceResult failure(List<FailedDetectableResult> failedDetectableResults) {
        return new XcodeWorkspaceResult(new BasicDependencyGraph(), failedDetectableResults);
    }

    public static XcodeWorkspaceResult success(DependencyGraph codeLocations) {
        return new XcodeWorkspaceResult(codeLocations, Collections.emptyList());
    }

    private XcodeWorkspaceResult(@Nullable DependencyGraph dependencyGraph, List<FailedDetectableResult> failedDetectableResults) {
        this.dependencyGraph = dependencyGraph;
        this.failedDetectableResults = failedDetectableResults;
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public List<FailedDetectableResult> getFailedDetectableResults() {
        return failedDetectableResults;
    }

    public boolean isFailure() {
        return CollectionUtils.isNotEmpty(getFailedDetectableResults());
    }
}
