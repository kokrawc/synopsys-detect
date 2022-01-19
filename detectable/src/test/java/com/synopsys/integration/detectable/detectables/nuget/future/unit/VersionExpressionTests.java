package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.nuget.future.range.NameVersionRange;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersion;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionExpressionParser;
import com.synopsys.integration.detectable.detectables.nuget.future.range.NuGetVersionRange;
import com.synopsys.integration.detectable.detectables.nuget.future.range.SimpleVersion;

public class VersionExpressionTests {

    @Test
    public void parsesGreaterThan() {
        NameVersionRange versionRange = new NuGetVersionExpressionParser().parse("name with spaces >= 1.0.0");

        Assertions.assertEquals("name with spaces", versionRange.getName());
        Assertions.assertEquals(NuGetVersionRange.forMinimumInclusive(new NuGetVersion(new SimpleVersion(1, 0, 0, 0), null, "", null)), versionRange.getVersionRange());
    }
}
