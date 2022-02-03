package com.synopsys.integration.detectable.detectables.cocoapods.unit;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.cocoapods.data.PodData;
import com.synopsys.integration.detectable.detectables.cocoapods.model.Pod;
import com.synopsys.integration.detectable.detectables.cocoapods.model.PodName;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodDataTransformer;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockNameParser;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockVersionParser;

public class PodDataTransformerTest {
    private final PodDataTransformer podDataTransformer = new PodDataTransformer(new PodlockNameParser(), new PodlockVersionParser());

    @Test
    public void test() {
        List<String> dependencyData = Arrays.asList("FBSnapshotTestCase (~> 2.0)", "Nimble", "Quick");
        PodData podData = new PodData("Nimble-Snapshots/Core (4.3.0)", dependencyData);
        Optional<Pod> pod = podDataTransformer.transform(podData);

        Assertions.assertTrue(pod.isPresent());
        List<PodName> dependencies = pod.get().getDependencies();
        Assertions.assertEquals(PodName.of("Nimble-Snapshots"), pod.get().getPodName());
        Assertions.assertEquals(PodName.of("FBSnapshotTestCase"), dependencies.get(0));
        Assertions.assertEquals(PodName.of("Nimble"), dependencies.get(1));
        Assertions.assertEquals(PodName.of("Quick"), dependencies.get(2));
        Assertions.assertEquals(3, dependencies.size());
    }
}
