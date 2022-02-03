package com.synopsys.integration.detectable.detectables.cocoapods.unit;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.detectables.cocoapods.data.PodSourceData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodSource;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodSourceParser;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockNameParser;

public class SourceParserTests {
    public final PodSourceParser parser = new PodSourceParser(new PodlockNameParser());

    @Test
    public void testGithubFound() {
        PodSourceData podSourceData = new PodSourceData("AFOAuth1Client", "https://github.com/lxcid/AFOAuth1Client.git", null);
        Optional<PodSource> source = parser.parse(podSourceData);
        assertSource(source, "AFOAuth1Client", Forge.GITHUB);
    }

    @Test
    public void testPathFound() {
        PodSourceData podSourceData = new PodSourceData("MDFTextAccessibility", null, "../node_modules/..");
        Optional<PodSource> source = parser.parse(podSourceData);
        assertSource(source, "MDFTextAccessibility", Forge.NPMJS);
    }

    //@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertSource(Optional<PodSource> source, String name, Forge forge) {
        Assertions.assertTrue(source.isPresent());
        Assertions.assertEquals(PodName.of(name), source.get().getPodName());
        Assertions.assertEquals(forge, source.get().getForge());
    }
}
