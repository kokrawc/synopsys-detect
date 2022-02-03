package com.synopsys.integration.detectable.detectables.cocoapods.model;

import java.util.Optional;

import com.synopsys.integration.bdio.model.dependencyid.NameDependencyId;
import com.synopsys.integration.util.Stringable;

public class PodName extends Stringable {
    private final String name;

    public PodName(String name) {this.name = name;}

    public static PodName of(String name) {
        return new PodName(name);
    }

    public static Optional<PodName> ofOptional(String name) {
        return Optional.of(new PodName(name));
    }

    public NameDependencyId toId() {
        return new NameDependencyId(name);
    }

    public String getValue() {
        return name;
    }
}
