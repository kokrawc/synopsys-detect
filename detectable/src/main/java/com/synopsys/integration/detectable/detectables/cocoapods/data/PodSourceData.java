package com.synopsys.integration.detectable.detectables.cocoapods.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PodSourceData {
    @JsonIgnore
    private String name;

    @JsonProperty(":git")
    private String git;

    @JsonProperty(":path")
    private String path;

    public PodSourceData(String name, String git, String path) {
        this.name = name;
        this.git = git;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGit() {
        return git;
    }

    public String getPath() {
        return path;
    }
}
