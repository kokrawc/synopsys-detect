package com.synopsys.integration.detect.workflow.blackduck.project;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.exception.IntegrationException;

public class UnmapCodeLocationsOperation {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BlackDuckApiClient blackDuckService;
    private final CodeLocationService codeLocationService;

    public UnmapCodeLocationsOperation(BlackDuckApiClient blackDuckService, CodeLocationService codeLocationService) {
        this.blackDuckService = blackDuckService;
        this.codeLocationService = codeLocationService;
    }

    public void unmapCodeLocations(ProjectVersionView projectVersionView, List<String> codeLocationNamesToPreserve) throws DetectUserFriendlyException {
        List<String> namesToPreserveLowercase = codeLocationNamesToPreserve.stream().map(String::toLowerCase).collect(Collectors.toList());
        try {
            List<CodeLocationView> codeLocationViews = blackDuckService.getAllResponses(projectVersionView.metaCodelocationsLink());
            for (CodeLocationView codeLocationView : codeLocationViews) {
                if (!namesToPreserveLowercase.contains(codeLocationView.getName().toLowerCase())) {
                    logger.debug("Unmapping codelocation: {}", codeLocationView.getName());
                    codeLocationService.unmapCodeLocation(codeLocationView);
                } else {
                    logger.debug("Preserving codelocation: {} (it is in the \"names to preserve\" list)", codeLocationView.getName());
                }
            }
            logger.info("Successfully unmapped (" + codeLocationViews.size() + ") code locations.");
        } catch (IntegrationException e) {
            String errorMessage = String.format("There was a problem unmapping Code Locations: %s", e.getMessage());
            throw new DetectUserFriendlyException(errorMessage, e, ExitCodeType.FAILURE_GENERAL_ERROR);
        }

    }
}
