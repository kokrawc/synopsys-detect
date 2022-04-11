package com.synopsys.integration.detect.artifact;

public class ArtifactoryConstants {
    public static final String ARTIFACTORY_URL = "https://sig-repo.synopsys.com/";
    public static final String VERSION_PLACEHOLDER = "<VERSION>";

    public static final String GRADLE_INSPECTOR_MAVEN_REPO = ARTIFACTORY_URL + "bds-integration-public-cache/";

    private static final String NUGET_VERSION_SUFFIX = ".nupkg";

    public static final String NUGET_DOTNET5_INSPECTOR_REPO = "bds-integrations-nuget-release/NugetDotnet5Inspector";
    public static final String NUGET_DOTNET5_INSPECTOR_PROPERTY = "NUGET_DOTNET5_INSPECTOR_LATEST_1";
    public static final String NUGET_DOTNET5_INSPECTOR_VERSION_OVERRIDE = "/NugetDotnet5Inspector." + ArtifactoryConstants.VERSION_PLACEHOLDER + NUGET_VERSION_SUFFIX;

    public static final String NUGET_DOTNET3_INSPECTOR_REPO = "bds-integrations-nuget-release/NugetDotnet3Inspector";
    public static final String NUGET_DOTNET3_INSPECTOR_PROPERTY = "NUGET_DOTNET3_INSPECTOR_LATEST_1";
    public static final String NUGET_DOTNET3_INSPECTOR_VERSION_OVERRIDE = "/NugetDotnet3Inspector." + ArtifactoryConstants.VERSION_PLACEHOLDER + NUGET_VERSION_SUFFIX;

    public static final String NUGET_INSPECTOR_REPO = "bds-integrations-nuget-release/BlackduckNugetInspector";
    public static final String NUGET_INSPECTOR_PROPERTY = "NUGET_INSPECTOR_LATEST_1";
    public static final String NUGET_INSPECTOR_VERSION_OVERRIDE = "/BlackduckNugetInspector." + ArtifactoryConstants.VERSION_PLACEHOLDER + NUGET_VERSION_SUFFIX;

    public static final String CLASSIC_NUGET_INSPECTOR_REPO = "bds-integrations-nuget-release/IntegrationNugetInspector";
    public static final String CLASSIC_NUGET_INSPECTOR_PROPERTY = "NUGET_INSPECTOR_LATEST_3";
    public static final String CLASSIC_NUGET_INSPECTOR_VERSION_OVERRIDE = "/IntegrationNugetInspector." + ArtifactoryConstants.VERSION_PLACEHOLDER + NUGET_VERSION_SUFFIX;

    public static final String DOCKER_INSPECTOR_REPO = "bds-integrations-release/com/synopsys/integration/blackduck-docker-inspector";
    public static final String DOCKER_INSPECTOR_PROPERTY = "DOCKER_INSPECTOR_LATEST_9";
    public static final String DOCKER_INSPECTOR_AIR_GAP_PROPERTY = "DOCKER_INSPECTOR_AIR_GAP_LATEST_9";
    public static final String DOCKER_INSPECTOR_VERSION_OVERRIDE =
        "/" + ArtifactoryConstants.VERSION_PLACEHOLDER + "/blackduck-docker-inspector-" + ArtifactoryConstants.VERSION_PLACEHOLDER + ".jar";

    public static final String PROJECT_INSPECTOR_REPO = "bds-integrations-release/com/synopsys/integration/synopsys-detect";
    public static final String PROJECT_INSPECTOR_MAC_PROPERTY = "PROJECT_INSPECTOR_MAC_LATEST_1";
    public static final String PROJECT_INSPECTOR_LINUX_PROPERTY = "PROJECT_INSPECTOR_LINUX_LATEST_1";
    public static final String PROJECT_INSPECTOR_WINDOWS_PROPERTY = "PROJECT_INSPECTOR_WINDOWS_LATEST_1";

    public static final String FONTS_REPO = "bds-integrations-release/com/synopsys/integration/synopsys-detect";
    public static final String FONTS_PROPERTY = "DETECT_FONT_BUNDLE_LATEST_7";
}
