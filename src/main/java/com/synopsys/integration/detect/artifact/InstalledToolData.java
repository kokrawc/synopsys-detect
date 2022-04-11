package com.synopsys.integration.detect.artifact;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class InstalledToolData {
    @SerializedName("version")
    public String version;
    @SerializedName("tools")
    public Map<String, String> toolData;

}
