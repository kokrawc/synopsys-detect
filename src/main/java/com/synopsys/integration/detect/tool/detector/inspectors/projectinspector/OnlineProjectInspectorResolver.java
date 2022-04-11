package com.synopsys.integration.detect.tool.detector.inspectors.projectinspector;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.artifact.InstalledToolLocator;
import com.synopsys.integration.detect.artifact.InstalledToolManager;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;

public class OnlineProjectInspectorResolver implements com.synopsys.integration.detectable.detectable.inspector.ProjectInspectorResolver {
    private static final String INSTALLED_TOOL_JSON_KEY = "project-inspector";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArtifactoryProjectInspectorInstaller projectInspectorInstaller;
    private final DirectoryManager directoryManager;
    private final InstalledToolManager installedToolManager;
    private final InstalledToolLocator installedToolLocator;

    private boolean hasResolvedInspector = false;

    public OnlineProjectInspectorResolver(
        ArtifactoryProjectInspectorInstaller projectInspectorInstaller,
        DirectoryManager directoryManager,
        InstalledToolManager installedToolManager,
        InstalledToolLocator installedToolLocator
    ) {
        this.projectInspectorInstaller = projectInspectorInstaller;
        this.directoryManager = directoryManager;
        this.installedToolManager = installedToolManager;
        this.installedToolLocator = installedToolLocator;
    }

    @Override
    public ExecutableTarget resolveProjectInspector() throws DetectableException {
        File inspectorFile = null;
        if (!hasResolvedInspector) {
            hasResolvedInspector = true;

            /* TODO: The dream.
            ArtifactResult artifactResult = artifactoryResolver.getCurrentArtifactInfo("project_inspector");
            ArtifactCache cache = cache.hasVersion(artifactResult); //could come from the tool installer.
            if (cache.isDownloaded()) {
                return cache.locate();
            } else {
                ArtifactInstall installation = artifactInstaller.install();
                cache.save(installation);
                return installation.locate();
            } */

            File installDirectory = directoryManager.getPermanentDirectory(INSTALLED_TOOL_JSON_KEY);
            try {
                inspectorFile = projectInspectorInstaller.install(installDirectory);
            } catch (DetectableException e) {
                logger.debug("Unable to install the project inspector from Artifactory.");
            }

            if (inspectorFile == null) {
                // Remote installation has failed
                logger.debug("Attempting to locate previous install of project inspector.");
                return installedToolLocator.locateTool(INSTALLED_TOOL_JSON_KEY)
                    .map(ExecutableTarget::forFile)
                    .orElseThrow(() ->
                        new DetectableException("Unable to locate previous install of the project inspector.")
                    );
            } else {
                installedToolManager.saveInstalledToolLocation(INSTALLED_TOOL_JSON_KEY, inspectorFile.getAbsolutePath());
            }
        }
        return ExecutableTarget.forFile(inspectorFile);
    }
}
