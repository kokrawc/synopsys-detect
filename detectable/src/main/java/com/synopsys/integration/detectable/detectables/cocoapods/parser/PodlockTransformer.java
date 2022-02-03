package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.LazyExternalIdDependencyGraphBuilder;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependencyid.DependencyId;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectables.cocoapods.model.Pod;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodSource;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodfileLock;

public class PodlockTransformer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExternalIdFactory externalIdFactory;

    public PodlockTransformer(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph transform(PodfileLock podfileLock) throws IOException, MissingExternalIdException {
        LazyExternalIdDependencyGraphBuilder lazyBuilder = new LazyExternalIdDependencyGraphBuilder();

        Set<PodName> knownPods = podfileLock.getPods().stream()
            .map(Pod::getPodName)
            .collect(Collectors.toSet());

        for (Pod pod : podfileLock.getPods()) {
            logger.trace(String.format("Processing pod %s", pod.getPodName().getValue()));
            Forge forge = podfileLock.getExternalSources().stream()
                .filter(source -> source.getPodName().equals(pod.getPodName()))
                .map(PodSource::getForge)
                .findFirst()
                .orElse(Forge.COCOAPODS);

            DependencyId dependencyId = pod.getPodName().toId();
            ExternalId externalId = externalIdFactory.createNameVersionExternalId(forge, pod.getPodName().getValue(), pod.getPodVersion());
            lazyBuilder.setDependencyInfo(dependencyId, externalId.getName(), externalId.getVersion(), externalId);

            for (PodName child : pod.getDependencies()) {
                if (knownPods.contains(child)) {
                    lazyBuilder.addParentWithChild(dependencyId, child.toId());
                }
            }
        }

        for (PodName podName : podfileLock.getDependencies()) {
            logger.trace(String.format("Processing pod dependency from pod lock file %s", podName));
            lazyBuilder.addChildToRoot(podName.toId());
        }

        return lazyBuilder.build();
    }
}
