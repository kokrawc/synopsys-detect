package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;

public class PodlockNameParser {

    public Optional<PodName> parsePodName(String podText) {
        // due to the way the KB deals with subspecs we should use the super name if it exists as this pod's name.
        Optional<String> podName = parseRawPodName(podText);
        if (podName.isPresent()) {
            Optional<String> superPodName = parseSuperPodName(podName.get());
            if (superPodName.isPresent()) {
                return superPodName.map(PodName::of);
            } else {
                return podName.map(PodName::of);
            }
        }

        return Optional.empty();
    }

    private Optional<String> parseSuperPodName(String podName) {
        if (podName.contains("/")) {
            return Optional.of(podName.split("/")[0].trim());
        }

        return Optional.empty();
    }

    private Optional<String> parseRawPodName(String podText) {
        if (StringUtils.isNotBlank(podText)) {
            return Optional.of(podText.split(" ")[0].trim());
        }

        return Optional.empty();
    }
}
