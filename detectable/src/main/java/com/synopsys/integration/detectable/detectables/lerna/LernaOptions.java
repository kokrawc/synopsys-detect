package com.synopsys.integration.detectable.detectables.lerna;

import java.util.List;

import com.synopsys.integration.detectable.detectable.util.ExcludedDependencyTypeFilter;

public class LernaOptions {
    private final ExcludedDependencyTypeFilter<LernaDependencyType> dependencyTypeFilter;
    private final List<String> excludedPackages;
    private final List<String> includedPackages;

    public LernaOptions(ExcludedDependencyTypeFilter<LernaDependencyType> dependencyTypeFilter, List<String> excludedPackages, List<String> includedPackages) {
        this.dependencyTypeFilter = dependencyTypeFilter;
        this.excludedPackages = excludedPackages;
        this.includedPackages = includedPackages;
    }

    public ExcludedDependencyTypeFilter<LernaDependencyType> getDependencyTypeFilter() {
        return dependencyTypeFilter;
    }

    public List<String> getExcludedPackages() {
        return excludedPackages;
    }

    public List<String> getIncludedPackages() {
        return includedPackages;
    }
}
