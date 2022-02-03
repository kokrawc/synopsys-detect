package com.synopsys.integration.detectable.detectables.cocoapods.unit;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockNameParser;

public class NameParserTests {
    public final PodlockNameParser parser = new PodlockNameParser();

    @Test
    public void testSuperParsed() {
        assertParsesTextTo("ARAnalytics/HockeyApp", "ARAnalytics");
    }

    @Test
    public void testFromIgnored() {
        assertParsesTextTo("CocoaLumberjack (from `https://github.com/CocoaLumberjack/CocoaLumberjack.git`)", "CocoaLumberjack");
    }

    @Test
    public void testVersionIgnored() {
        assertParsesTextTo("AFNetworking (~> 2.5)", "AFNetworking");
    }

    @Test
    public void testDashes() {
        assertParsesTextTo("Nimble-Snapshots (4.3.0)", "Nimble-Snapshots");
    }

    private void assertParsesTextTo(String text, String expected) {
        Optional<PodName> name = parser.parsePodName(text);
        Assertions.assertTrue(name.isPresent());
        Assertions.assertEquals(PodName.of(expected), name.get());
    }
}
