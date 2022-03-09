package com.synopsys.integration.detectable.detectables.packrat.parse;

import java.util.List;

import com.synopsys.integration.detectable.util.OptionalNameOptionalVersion;

public class PackratDescriptionFileParser {
    private static final String PACKAGE_TOKEN = "Package:";
    private static final String VERSION_TOKEN = "Version:";

    public OptionalNameOptionalVersion getProjectNameVersion(List<String> descriptionFileLines) {
        String name = null;
        String version = null;

        for (String rawLine : descriptionFileLines) {
            String line = rawLine.trim();

            if (line.startsWith(PACKAGE_TOKEN)) {
                name = line.replace(PACKAGE_TOKEN, "").trim();
            } else if (line.startsWith(VERSION_TOKEN)) {
                version = line.replace(VERSION_TOKEN, "").trim();
            }
        }

        return new OptionalNameOptionalVersion(name, version);
    }
}
