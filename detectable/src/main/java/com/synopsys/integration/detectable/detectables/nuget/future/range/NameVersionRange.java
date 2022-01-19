package com.synopsys.integration.detectable.detectables.nuget.future.range;

public class NameVersionRange {
    private final String name;
    private final NuGetVersionRange versionRange;

    public NameVersionRange(String name, NuGetVersionRange versionRange) {
        this.name = name;
        this.versionRange = versionRange;
    }

    public String getName() {
        return name;
    }

    public NuGetVersionRange getVersionRange() {
        return versionRange;
    }
}
