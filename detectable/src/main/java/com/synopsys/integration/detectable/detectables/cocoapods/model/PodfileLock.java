package com.synopsys.integration.detectable.detectables.cocoapods.model;

import java.util.List;

public class PodfileLock {
    private final List<PodName> dependencies;
    private final List<PodSource> externalSources;
    private final List<Pod> pods;

    public PodfileLock(List<PodName> dependencies, List<PodSource> externalSources, List<Pod> pods) {
        this.dependencies = dependencies;
        this.externalSources = externalSources;
        this.pods = pods;
    }

    public List<PodName> getDependencies() {
        return dependencies;
    }

    public List<PodSource> getExternalSources() {
        return externalSources;
    }

    public List<Pod> getPods() {
        return pods;
    }
}
