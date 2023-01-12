package com.synopsys.integration.detect.docs.model;

import java.util.List;

public class SearchRule {
    private final int maxDepth;
    private final boolean nestable;
    private final List<String> notNestableBeneath;
    private final List<String> notNestableBeneathDetectables;
    private final List<String> yieldsTo;

    public SearchRule(
        int maxDepth,
        boolean nestable,
        List<String> notNestableBeneath,
        List<String> notNestableBeneathDetectables,
        List<String> yieldsTo
    ) {
        this.maxDepth = maxDepth;
        this.nestable = nestable;
        this.notNestableBeneath = notNestableBeneath;
        this.notNestableBeneathDetectables = notNestableBeneathDetectables;
        this.yieldsTo = yieldsTo;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public boolean isNestable() {
        return nestable;
    }

    public List<String> getNotNestableBeneath() {
        return notNestableBeneath;
    }

    public List<String> getYieldsTo() {
        return yieldsTo;
    }

    public List<String> getNotNestableBeneathDetectables() {
        return notNestableBeneathDetectables;
    }
}
