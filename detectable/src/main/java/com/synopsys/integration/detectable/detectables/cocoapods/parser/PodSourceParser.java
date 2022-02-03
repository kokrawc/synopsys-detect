package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.util.Optional;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.detectables.cocoapods.data.PodSourceData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodSource;

public class PodSourceParser {
    private final PodlockNameParser nameParser;

    public PodSourceParser(PodlockNameParser nameParser) {
        this.nameParser = nameParser;
    }

    public Optional<PodSource> parse(PodSourceData podSourceData) {
        Optional<PodName> dependencyId = nameParser.parsePodName(podSourceData.getName());
        if (dependencyId.isPresent()) {
            PodName podName = dependencyId.get();
            if (null != podSourceData.getGit() && podSourceData.getGit().contains("github")) {
                return PodSource.ofOptional(podName, Forge.GITHUB);
            } else if (null != podSourceData.getPath() && podSourceData.getPath().contains("node_modules")) {
                return PodSource.ofOptional(podName, Forge.NPMJS);
            }
        }
        return Optional.empty();
    }
}
