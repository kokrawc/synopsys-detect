package com.synopsys.integration.detectable.detectables.nuget.future.range;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuGetVersionExpressionParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //Of the form 'Package >= Version' but with any operator.
    public NameVersionRange parse(String versionRangeText) {
        if (versionRangeText.contains(">=")) {
            return parseForTokens(versionRangeText, ">=", NuGetVersionRange::forMinimumInclusive);
        } else if (versionRangeText.contains("<=")) {
            return parseForTokens(versionRangeText, "<=", NuGetVersionRange::forMaximumInclusive);
        } else if (versionRangeText.contains("=")) {
            return parseForTokens(versionRangeText, "=", NuGetVersionRange::forExact);
        } else if (versionRangeText.contains(">")) {
            return parseForTokens(versionRangeText, ">", NuGetVersionRange::forMinimumExclusive);
        } else if (versionRangeText.contains("<")) {
            return parseForTokens(versionRangeText, "<", NuGetVersionRange::forMaximumExclusive);
        } else {
            logger.warn("Unknown package format '" + versionRangeText + "', treating entire expression as dependency name: " + versionRangeText);
            return new NameVersionRange(versionRangeText, null);
        }
    }

    private NameVersionRange parseForTokens(String versionRangeText, String tokens, Function<NuGetVersion, NuGetVersionRange> rangeFunction) {
        String[] pieces = StringUtils.split(versionRangeText, tokens);
        if (pieces.length == 1) {
            logger.warn("Unknown package format '" + versionRangeText + "', treating opening expression as dependency name: " + pieces[0]);
            return new NameVersionRange(pieces[0], null);
        } else if (pieces.length != 2) {
            logger.warn("Unknown package format '" + versionRangeText + "', treating entire expression as dependency name: " + versionRangeText);
            return new NameVersionRange(versionRangeText, null);
        }
        String name = pieces[0].trim();
        NuGetVersion minVersion = NuGetVersion.TryParse(pieces[1].trim());
        return new NameVersionRange(name, rangeFunction.apply(minVersion));
    }
}
