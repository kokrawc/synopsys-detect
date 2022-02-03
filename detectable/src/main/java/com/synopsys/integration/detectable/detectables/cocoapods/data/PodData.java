package com.synopsys.integration.detectable.detectables.cocoapods.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class PodData {
    private String name; //TODO: This should not be name. Should be called text. Might look like this: AppHub (from `https://github.com/orta/apphub.git`, branch `build_list`)
    private List<String> dependencies = new ArrayList<>();

    public PodData() {
    }

    public PodData(String name) {
        this.name = name;
    }

    public PodData(String name, List<String> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    @JsonAnySetter
    public void setDynamicProperty(String name, List<String> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
