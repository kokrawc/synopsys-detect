package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.detectable.detectables.cocoapods.data.PodData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.Pod;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;

public class PodDataTransformer {
    private final PodlockNameParser nameParser;
    private final PodlockVersionParser versionParser;

    public PodDataTransformer(PodlockNameParser nameParser, PodlockVersionParser versionParser) {
        this.nameParser = nameParser;
        this.versionParser = versionParser;
    }

    public Optional<Pod> transform(PodData podData) {
        Optional<PodName> name = nameParser.parsePodName(podData.getName());
        Optional<String> version = versionParser.parseVersion(podData.getName());
        if (name.isPresent() && version.isPresent()) {
            List<PodName> dependencies = podData.getDependencies().stream()
                .map(nameParser::parsePodName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

            return Optional.of(new Pod(name.get(), version.get(), dependencies));
        }
        return Optional.empty();
    }
}
