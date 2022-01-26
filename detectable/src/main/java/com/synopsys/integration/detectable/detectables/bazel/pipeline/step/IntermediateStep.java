package com.synopsys.integration.detectable.detectables.bazel.pipeline.step;

import java.io.File;
import java.util.List;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.exception.IntegrationException;

public interface IntermediateStep {

    List<String> process(File workspaceDir, ExecutableTarget bazelExe, List<String> input) throws IntegrationException;
}
