package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.detectable.detectables.cocoapods.data.PodData;
import com.synopsys.integration.detectable.detectables.cocoapods.data.PodfileLockData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.Pod;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodSource;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodfileLock;

public class PodlockDataTransformer {
    private final PodSourceParser podSourceParser;
    private final PodlockNameParser podlockNameParser;
    private final PodDataTransformer podDataTransformer;

    public PodlockDataTransformer(PodSourceParser podSourceParser, PodlockNameParser podlockNameParser, PodDataTransformer podDataTransformer) {
        this.podSourceParser = podSourceParser;
        this.podlockNameParser = podlockNameParser;
        this.podDataTransformer = podDataTransformer;
    }

    public PodfileLock transform(PodfileLockData podfileLockData) throws IOException {
        List<PodName> dependencies = Collections.emptyList();
        if (podfileLockData.getDependencies() != null) {
            dependencies = podfileLockData.getDependencies().stream()
                .map(PodData::getName)
                .map(podlockNameParser::parsePodName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }
        List<PodSource> podSources = Collections.emptyList();
        if (podfileLockData.getExternalSources() != null && podfileLockData.getExternalSources().getSources() != null) {
            podSources = podfileLockData.getExternalSources().getSources().stream()
                .map(podSourceParser::parse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }
        List<Pod> pods = Collections.emptyList();
        if (podfileLockData.getPods() != null) {
            pods = podfileLockData.getPods().stream()
                .map(podDataTransformer::transform)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }

        return new PodfileLock(dependencies, podSources, pods);
    }

}
