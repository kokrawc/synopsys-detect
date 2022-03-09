package com.synopsys.integration.detectable.util;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.util.Stringable;

public class OptionalNameOptionalVersion extends Stringable {
    @Nullable
    private final String name;
    @Nullable
    private final String version;

    public OptionalNameOptionalVersion(@Nullable String name, @Nullable String version) {
        this.name = name;
        this.version = version;
    }

    public static OptionalNameOptionalVersion empty() {
        return new OptionalNameOptionalVersion(null, null);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }
}
