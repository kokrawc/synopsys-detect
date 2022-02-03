package com.synopsys.integration.detectable.detectables.cocoapods.unit;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockVersionParser;

public class VersionParserTests {
    public final PodlockVersionParser parser = new PodlockVersionParser();

    @Test
    public void testThreePartVersion() {
        assertParsesTextTo("JSBadgeView (1.4.1)", "1.4.1");
    }

    @Test
    public void testSuperVersion() {
        assertParsesTextTo("FBSnapshotTestCase/SwiftSupport (2.1.4)", "2.1.4");
    }

    @Test
    public void testIgnoresDash() {
        assertParsesTextTo("Nimble-Snapshots (4.3.0)", "4.3.0");
    }

    @Test
    private void assertParsesTextTo(String text, String expected) {
        Optional<String> name = parser.parseVersion(text);
        Assertions.assertTrue(name.isPresent());
        Assertions.assertEquals(expected, name.get());
    }

}
