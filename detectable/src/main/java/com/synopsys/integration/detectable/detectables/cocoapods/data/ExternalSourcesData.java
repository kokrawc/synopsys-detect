package com.synopsys.integration.detectable.detectables.cocoapods.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ExternalSourcesData {
    private final List<PodSourceData> sources = new ArrayList<>();

    @JsonAnySetter
    public void setDynamicProperty(String name, PodSourceData podSource) {
        podSource.setName(name);
        sources.add(podSource);
    }

    public List<PodSourceData> getSources() {
        return sources;
    }
}
