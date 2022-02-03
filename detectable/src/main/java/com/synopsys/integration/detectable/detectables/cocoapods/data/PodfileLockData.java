package com.synopsys.integration.detectable.detectables.cocoapods.data;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PodfileLockData {
    @JsonProperty("PODS")
    private List<PodData> pods;

    @JsonProperty("DEPENDENCIES")
    private List<PodData> dependencies; //TODO: Different model, no dependencies.

    @JsonProperty("EXTERNAL SOURCES")
    private ExternalSourcesData externalSources;

    @Nullable //TODO: Optional return type.
    public List<PodData> getPods() {
        return pods;
    }

    @Nullable
    public List<PodData> getDependencies() {
        return dependencies;
    }

    @Nullable
    public ExternalSourcesData getExternalSources() {
        return externalSources;
    }
}
