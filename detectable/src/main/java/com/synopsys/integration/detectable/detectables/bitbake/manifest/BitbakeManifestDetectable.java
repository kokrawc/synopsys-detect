package com.synopsys.integration.detectable.detectables.bitbake.manifest;

import java.io.File;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.resolver.BashResolver;
import com.synopsys.integration.detectable.detectable.explanation.PropertyProvided;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.PropertyInsufficientDetectableResult;
import com.synopsys.integration.detectable.detectables.bitbake.common.BitbakeDetectableOptions;
import com.synopsys.integration.detectable.detectables.bitbake.common.BitbakeDetectorAlgorithm;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "various", forge = "YOCTO", requirementsMarkdown = "Properties: Package names. File: build env script. Executable: bash")
public class BitbakeManifestDetectable extends Detectable {
    private final BitbakeDetectableOptions bitbakeDetectableOptions;
    private final FileFinder fileFinder;
    private final BitbakeManifestExtractor bitbakeManifestExtractor;
    private final BashResolver bashResolver;

    private File foundBuildEnvScript;
    private ExecutableTarget bashExe;

    public BitbakeManifestDetectable(DetectableEnvironment detectableEnvironment, FileFinder fileFinder, BitbakeDetectableOptions bitbakeDetectableOptions, BitbakeManifestExtractor bitbakeManifestExtractor,
        BashResolver bashResolver) {
        super(detectableEnvironment);
        this.fileFinder = fileFinder;
        this.bitbakeDetectableOptions = bitbakeDetectableOptions;
        this.bitbakeManifestExtractor = bitbakeManifestExtractor;
        this.bashResolver = bashResolver;
    }

    @Override
    public DetectableResult applicable() {
        Requirements requirements = new Requirements(fileFinder, environment);
        foundBuildEnvScript = requirements.file(bitbakeDetectableOptions.getBuildEnvName());

        if (bitbakeDetectableOptions.getBitbakeAlgorithm() == BitbakeDetectorAlgorithm.LEGACY) {
            return new PropertyInsufficientDetectableResult("Bitbake Manifest detector was not requested (detect.bitbake.algorithm=LEGACY).");
        }

        if (bitbakeDetectableOptions.getPackageNames() == null || (bitbakeDetectableOptions.getPackageNames().size() != 1)) {
            return new PropertyInsufficientDetectableResult("Bitbake requires that at exactly one target image name is provided.");
        } else {
            requirements.explain(new PropertyProvided("Bitbake Target Image Names"));
        }

        return requirements.result();
    }

    @Override
    public DetectableResult extractable() throws DetectableException {
        Requirements requirements = new Requirements(fileFinder, environment);
        bashExe = requirements.executable(bashResolver::resolveBash, "bash");
        return requirements.result();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return bitbakeManifestExtractor.extract(environment.getDirectory(), foundBuildEnvScript, bitbakeDetectableOptions.getSourceArguments(), bitbakeDetectableOptions.getPackageNames(),
            bitbakeDetectableOptions.isFollowSymLinks(), bitbakeDetectableOptions.getSearchDepth(), bashExe, bitbakeDetectableOptions.getLicenseManifestFilePath(),
            bitbakeDetectableOptions.getBitbakeAlgorithm());
    }
}
