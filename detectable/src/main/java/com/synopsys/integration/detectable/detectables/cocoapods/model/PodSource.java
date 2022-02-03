package com.synopsys.integration.detectable.detectables.cocoapods.model;

import java.util.Optional;

import com.synopsys.integration.bdio.model.Forge;

public class PodSource {
    private final PodName podName;
    private final Forge forge;

    public PodSource(PodName podName, Forge forge) {
        this.podName = podName;
        this.forge = forge;
    }

    public static PodSource of(PodName podName, Forge forge) {
        return new PodSource(podName, forge);
    }

    public static Optional<PodSource> ofOptional(PodName podName, Forge forge) {
        return Optional.of(new PodSource(podName, forge));
    }

    public PodName getPodName() {
        return podName;
    }

    public Forge getForge() {
        return forge;
    }
}
