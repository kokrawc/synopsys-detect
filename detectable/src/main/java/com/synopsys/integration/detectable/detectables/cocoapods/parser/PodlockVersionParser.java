package com.synopsys.integration.detectable.detectables.cocoapods.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PodlockVersionParser {
    private static final List<String> fuzzyVersionIdentifiers = new ArrayList<>(Arrays.asList(">", "<", "~>", "="));

    public Optional<String> parseVersion(String podText) {
        String[] segments = podText.split(" ");
        if (segments.length > 1) {
            String version = segments[1];
            version = version.replace("(", "").replace(")", "").trim();
            if (!isVersionFuzzy(version)) {
                return Optional.of(version);
            }
        }

        return Optional.empty();
    }

    private boolean isVersionFuzzy(String versionName) {
        for (String identifier : fuzzyVersionIdentifiers) {
            if (versionName.contains(identifier)) {
                return true;
            }
        }

        return false;
    }
}
