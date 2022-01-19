package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersion;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionRange;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionRangeParser;
import com.synopsys.integration.detectable.detectables.nuget.future.range.SimpleVersion;

public class VersionConstraintsTests {

    @Test
    public void parsesExactVersion() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[6.0]", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(6, 0, 0, 0), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());
        Assertions.assertTrue(versionRange.includeMaxVersion());
        Assertions.assertEquals(new SimpleVersion(6, 0, 0, 0), versionRange.maxVersion().getVersion());

        //Accepts only 6.0
        assertIncluded(versionRange, 6, 0, 0, 0);

        assertExcluded(versionRange, 7, 1, 3, 0);
        assertExcluded(versionRange, 6, 0, 0, 1);
        assertExcluded(versionRange, 5, 5, 1, 0);
    }

    @Test
    public void parsesMinorWildcard() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("6.*", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(1, 0, 0, 0), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());

        //Accepts any 6.x.y version
        assertIncluded(versionRange, 6, 3, 2, 0);
        assertIncluded(versionRange, 6, 0, 1, 0);

        assertExcluded(versionRange, 7, 1, 3, 0);
        assertExcluded(versionRange, 5, 5, 1, 0);
    }

    @Test
    public void parsesMinExclusive() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("(4.1.3,)", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertFalse(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(1, 0, 0, 0), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());

        //Accepts any version above, but not including 4.1.3
        assertIncluded(versionRange, 5, 0, 0, 0);
        assertIncluded(versionRange, 6, 0, 0, 0);

        assertExcluded(versionRange, 4, 1, 3, 0);
        assertExcluded(versionRange, 2, 5, 1, 0);
    }

    @Test
    public void parsesMaxOnly() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("(, 5.0)", true);

        Assertions.assertFalse(versionRange.hasMinVersion());

        Assertions.assertTrue(versionRange.hasMaxVersion());
        Assertions.assertFalse(versionRange.includeMaxVersion());
        Assertions.assertEquals(new SimpleVersion(5, 0, 0, 0), versionRange.maxVersion().getVersion());

        //Accepts any version up below 5.x
        assertIncluded(versionRange, 4, 2, 2, 2);
        assertIncluded(versionRange, 1, 0, 0, 0);

        assertExcluded(versionRange, 5, 0, 0, 0);
        assertExcluded(versionRange, 6, 5, 1, 0);
    }

    @Test
    public void parsesMajorOnlyMinMax() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[1, 3)", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(1, 0, 0, 0), versionRange.minVersion().getVersion());

        Assertions.assertTrue(versionRange.hasMaxVersion());
        Assertions.assertFalse(versionRange.includeMaxVersion());
        Assertions.assertEquals(new SimpleVersion(3, 0, 0, 0), versionRange.maxVersion().getVersion());

        //Accepts any 1.x or 2.x version, but not 0.x or 3.x and higher.
        assertIncluded(versionRange, 1, 3, 2, 0);
        assertIncluded(versionRange, 2, 4, 4, 0);

        assertExcluded(versionRange, 0, 5, 1, 0);
        assertExcluded(versionRange, 3, 0, 0, 0);
        assertExcluded(versionRange, 4, 5, 1, 0);
    }

    @Test
    public void parsesDifferentLengthMinMax() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[1.3.2, 1.5)", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(1, 3, 2, 0), versionRange.minVersion().getVersion());

        Assertions.assertTrue(versionRange.hasMaxVersion());
        Assertions.assertFalse(versionRange.includeMaxVersion());
        Assertions.assertEquals(new SimpleVersion(1, 5, 0, 0), versionRange.maxVersion().getVersion());

        //Accepts 1.3.2 up to 1.4.x, but not 1.5 and higher.
        assertIncluded(versionRange, 1, 3, 2, 0);
        assertIncluded(versionRange, 1, 4, 4, 0);

        assertExcluded(versionRange, 1, 5, 1, 0);
        assertExcluded(versionRange, 2, 5, 1, 0);
    }

    @Test
    public void parsesNormalThreePlaces() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[4.1.6, )", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(4, 1, 6, 0), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());
    }

    @Test
    public void parsesFourPlaces() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[2.3.4.270, )", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(2, 3, 4, 270), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());
    }

    @Test
    public void parsesReleaseLabel() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("[0.0.5-preview, )", true);

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(0, 0, 5, 0), versionRange.minVersion().getVersion());
        Assertions.assertEquals("preview", versionRange.minVersion().getRelease());

        Assertions.assertFalse(versionRange.hasMaxVersion());
    }

    @Test
    public void parsesFixed() {
        NuGetVersionRange versionRange = new NuGetVersionRangeParser().parse("6.0.0", true); //Equivalent to [6.0.0,)

        Assertions.assertTrue(versionRange.hasMinVersion());
        Assertions.assertTrue(versionRange.includeMinVersion());
        Assertions.assertEquals(new SimpleVersion(6, 0, 0, 0), versionRange.minVersion().getVersion());

        Assertions.assertFalse(versionRange.hasMaxVersion());
        Assertions.assertFalse(versionRange.includeMaxVersion());
        Assertions.assertNull(versionRange.maxVersion());
    }

    private void assertIncluded(NuGetVersionRange range, int major, int minor, Integer build, Integer revision) {
        SimpleVersion simpleVersion = new SimpleVersion(major, minor, build, revision);
        NuGetVersion version = new NuGetVersion(simpleVersion, null, null, null);
        Assertions.assertTrue(range.Satisfies(version));
    }

    private void assertExcluded(NuGetVersionRange range, int major, int minor, Integer build, Integer revision) {
        SimpleVersion simpleVersion = new SimpleVersion(major, minor, build, revision);
        NuGetVersion version = new NuGetVersion(simpleVersion, null, null, null);
        Assertions.assertFalse(range.Satisfies(version));
    }
}
