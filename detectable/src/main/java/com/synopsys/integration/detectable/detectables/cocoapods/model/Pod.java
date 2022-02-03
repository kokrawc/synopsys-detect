package com.synopsys.integration.detectable.detectables.cocoapods.model;

import java.util.List;

public class Pod {
    private final PodName podName;
    private final String podVersion;
    private final List<PodName> dependencies;

    public Pod(PodName podName, String podVersion, List<PodName> dependencies) {
        this.podName = podName;
        this.podVersion = podVersion;
        this.dependencies = dependencies;
    }

    public PodName getPodName() {
        return podName;
    }

    public String getPodVersion() {
        return podVersion;
    }

    public List<PodName> getDependencies() {
        return dependencies;
    }
}
