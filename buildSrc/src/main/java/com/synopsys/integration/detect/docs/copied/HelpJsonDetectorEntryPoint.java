package com.synopsys.integration.detect.docs.copied;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.detect.docs.model.SearchRule;

public class HelpJsonDetectorEntryPoint {
    private String name;
    private SearchRule searchRule;
    private List<HelpJsonDetectable> detectables = new ArrayList<>();

    public List<HelpJsonDetectable> getDetectables() {
        return detectables;
    }

    public void setDetectables(List<HelpJsonDetectable> detectables) {
        this.detectables = detectables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSearchRule(final SearchRule searchRule) {
        this.searchRule = searchRule;
    }

    public SearchRule getSearchRule() {
        return searchRule;
    }
}
