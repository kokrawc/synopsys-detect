package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersion;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionCompare;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionRange;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionRangeParser;
import com.synopsys.integration.detectable.detectables.nuget.future.range.SimpleVersion;

public class VersionCompareTests {

    @TestFactory
    Collection<DynamicTest> versionComparisons() {
        return Arrays.asList(
            DynamicTest.dynamicTest("Major: Three is less than four.",
                () -> assertLess(majorVersion(3), majorVersion(4))),
            DynamicTest.dynamicTest("Major: Six is more than two.",
                () -> assertGreater(majorVersion(6), majorVersion(2))),
            DynamicTest.dynamicTest("Major: Three equals three.",
                () -> assertEquals(majorVersion(3), majorVersion(3))),

            DynamicTest.dynamicTest("Minor: Three is less than four.",
                () -> assertLess(minorVersion(1, 3), minorVersion(1, 4))),
            DynamicTest.dynamicTest("Minor: Six is more than two.",
                () -> assertGreater(minorVersion(1, 6), minorVersion(1, 2))),
            DynamicTest.dynamicTest("Minor: Three equals three.",
                () -> assertEquals(minorVersion(1, 3), minorVersion(1, 3))),

            DynamicTest.dynamicTest("Build: Three is less than four.",
                () -> assertLess(buildVersion(1, 1, 3), buildVersion(1, 1, 4))),
            DynamicTest.dynamicTest("Build: Six is more than two.",
                () -> assertGreater(buildVersion(2, 2, 6), buildVersion(2, 2, 2))),
            DynamicTest.dynamicTest("Build: Three equals three.",
                () -> assertEquals(buildVersion(3, 3, 3), buildVersion(3, 3, 3))),

            DynamicTest.dynamicTest("Revision: Three is less than four.",
                () -> assertLess(version(1, 1, 1, 3), version(1, 1, 1, 4))),
            DynamicTest.dynamicTest("Revision: Six is more than two.",
                () -> assertGreater(version(2, 2, 2, 6), version(2, 2, 3, 2))),
            DynamicTest.dynamicTest("Revision: Three equals three.",
                () -> assertEquals(version(3, 3, 3, 3), version(3, 3, 3, 3)))
        );
    }

    private void assertLess(NuGetVersion left, NuGetVersion right) {
        Assertions.assertTrue(NuGetVersionCompare.Compare(left, right) < 0);
    }

    private void assertEquals(NuGetVersion left, NuGetVersion right) {
        Assertions.assertEquals(0, NuGetVersionCompare.Compare(left, right));
    }

    private void assertGreater(NuGetVersion left, NuGetVersion right) {
        Assertions.assertTrue(NuGetVersionCompare.Compare(left, right) > 0);
    }

    private NuGetVersion version(int major, int minor, Integer build, Integer revision) {
        SimpleVersion simpleVersion = new SimpleVersion(major, minor, build, revision);
        return new NuGetVersion(simpleVersion, null, null, null);
    }

    private NuGetVersion majorVersion(int major) {
        return version(major, 0, 0, 0);
    }

    private NuGetVersion minorVersion(int major, int minor) {
        return version(major, minor, 0, 0);
    }

    private NuGetVersion buildVersion(int major, int minor, int build) {
        return version(major, minor, build, 0);
    }
}
