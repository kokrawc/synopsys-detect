package com.synopsys.integration.common.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Black Duck Stream
// TODO: Test this class.
public class Bdso<T> extends Bds<Optional<T>> {
    public Bdso(Stream<Optional<T>> stream) {
        super(stream);
    }

    public List<T> toPresentList() {
        return stream.filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
