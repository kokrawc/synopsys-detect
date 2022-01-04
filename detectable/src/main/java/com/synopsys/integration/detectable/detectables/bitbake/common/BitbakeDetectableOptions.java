package com.synopsys.integration.detectable.detectables.bitbake.common;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class BitbakeDetectableOptions {
    private final String buildEnvName;
    private final List<String> sourceArguments;
    private final List<String> packageNames;
    private final Integer searchDepth;
    private final boolean followSymLinks;
    private final BitbakeDetectorAlgorithm bitbakeAlgorithm;
    private final String licenseManifestFilePath;

    public BitbakeDetectableOptions(String buildEnvName, List<String> sourceArguments, List<String> packageNames, Integer searchDepth, boolean followSymLinks,
        BitbakeDetectorAlgorithm bitbakeAlgorithm, String licenseManifestFilePath) {
        this.buildEnvName = buildEnvName;
        this.sourceArguments = sourceArguments;
        this.packageNames = packageNames;
        this.searchDepth = searchDepth;
        this.followSymLinks = followSymLinks;
        this.bitbakeAlgorithm = bitbakeAlgorithm;
        this.licenseManifestFilePath = licenseManifestFilePath;
    }

    public String getBuildEnvName() {
        return buildEnvName;
    }

    public List<String> getSourceArguments() {
        return sourceArguments;
    }

    public List<String> getPackageNames() {
        return packageNames;
    }

    public Integer getSearchDepth() {
        return searchDepth;
    }

    public boolean isFollowSymLinks() {
        return followSymLinks;
    }

    public BitbakeDetectorAlgorithm getBitbakeAlgorithm() {
        return bitbakeAlgorithm;
    }

    @Nullable
    public String getLicenseManifestFilePath() {
        return licenseManifestFilePath;
    }
}
