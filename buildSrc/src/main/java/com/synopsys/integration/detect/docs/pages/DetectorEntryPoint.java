package com.synopsys.integration.detect.docs.pages;

import java.util.List;

import com.synopsys.integration.detect.docs.model.Detectable;
import com.synopsys.integration.detect.docs.model.SearchRule;

public class DetectorEntryPoint {
    private final String name;
    private SearchRule searchRule;
    private final List<Detectable> detectables;

    public DetectorEntryPoint(String name, List<Detectable> detectables, SearchRule searchRule) {
        this.name = name;
        this.detectables = detectables;
        this.searchRule = searchRule;
    }

    public String getName() {
        return name;
    }

    public List<Detectable> getDetectables() {
        return detectables;
    }

    public SearchRule getSearchRule() {
        return searchRule;
    }
}
