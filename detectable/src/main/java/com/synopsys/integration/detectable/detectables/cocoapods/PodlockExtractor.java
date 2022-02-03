package com.synopsys.integration.detectable.detectables.cocoapods;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.cocoapods.data.PodfileLockData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodfileLock;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockDataTransformer;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;

public class PodlockExtractor {
    private final PodlockDataTransformer podlockDataTransformer;
    private final PodlockTransformer podlockTransformer;

    public PodlockExtractor(PodlockDataTransformer podlockDataTransformer, PodlockTransformer podlockTransformer) {
        this.podlockDataTransformer = podlockDataTransformer;
        this.podlockTransformer = podlockTransformer;
    }

    public Extraction extract(File podlock) throws IOException {
        String podLockText = FileUtils.readFileToString(podlock, StandardCharsets.UTF_8);

        YAMLMapper mapper = new YAMLMapper();
        PodfileLockData podfileLockData = mapper.readValue(podLockText, PodfileLockData.class);

        try {
            PodfileLock podfileLock = podlockDataTransformer.transform(podfileLockData);
            DependencyGraph dependencyGraph = podlockTransformer.transform(podfileLock);
            CodeLocation codeLocation = new CodeLocation(dependencyGraph);
            return new Extraction.Builder().success(codeLocation).build();
        } catch (MissingExternalIdException e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

}
