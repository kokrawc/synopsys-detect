package com.synopsys.integration.detectable.detectables.xcode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.synopsys.integration.bdio.graph.BasicDependencyGraph;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.ProjectDependencyGraph;
import com.synopsys.integration.bdio.model.BdioIdEscaper;
import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.detectable.result.FailedDetectableResult;
import com.synopsys.integration.detectable.detectables.swift.lock.PackageResolvedExtractor;
import com.synopsys.integration.detectable.detectables.swift.lock.SwiftPackageResolvedDetectable;
import com.synopsys.integration.detectable.detectables.swift.lock.model.PackageResolvedResult;
import com.synopsys.integration.detectable.detectables.xcode.model.XcodeFileReference;
import com.synopsys.integration.detectable.detectables.xcode.model.XcodeWorkspace;
import com.synopsys.integration.detectable.detectables.xcode.model.XcodeWorkspaceResult;
import com.synopsys.integration.detectable.detectables.xcode.parse.XcodeWorkspaceFormatChecker;
import com.synopsys.integration.detectable.detectables.xcode.parse.XcodeWorkspaceParser;
import com.synopsys.integration.detectable.util.ExternalIdCreator;

public class XcodeWorkspaceExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final XcodeWorkspaceParser xcodeWorkspaceParser;
    private final XcodeWorkspaceFormatChecker xcodeWorkspaceFormatChecker;
    private final PackageResolvedExtractor packageResolvedExtractor;
    private final FileFinder fileFinder;

    public XcodeWorkspaceExtractor(
        XcodeWorkspaceParser xcodeWorkspaceParser,
        XcodeWorkspaceFormatChecker xcodeWorkspaceFormatChecker,
        PackageResolvedExtractor packageResolvedExtractor, FileFinder fileFinder
    ) {
        this.xcodeWorkspaceParser = xcodeWorkspaceParser;
        this.xcodeWorkspaceFormatChecker = xcodeWorkspaceFormatChecker;
        this.packageResolvedExtractor = packageResolvedExtractor;
        this.fileFinder = fileFinder;
    }

    public XcodeWorkspaceResult extract(File workspaceDataFile, File workspaceDirectory) throws IOException, ParserConfigurationException, SAXException {
        String workspaceFileContents = FileUtils.readFileToString(workspaceDataFile, Charset.defaultCharset());
        XcodeWorkspace xcodeWorkspace = xcodeWorkspaceParser.parse(workspaceFileContents);
        xcodeWorkspaceFormatChecker.checkForVersionCompatibility(xcodeWorkspace);

        List<DependencyGraph> dependencyGraphs = new LinkedList<>();
        List<FailedDetectableResult> failedDetectableResults = new LinkedList<>();
        for (XcodeFileReference fileReference : xcodeWorkspace.getFileReferences()) {
            File workspaceReferencedDirectory = workspaceDirectory.getParentFile().toPath().resolve(fileReference.getRelativeLocation()).toFile();
            if (!workspaceReferencedDirectory.exists()) {
                logger.warn(
                    "Failed to find subproject '{}' as defined in the workspace at '{}'",
                    workspaceReferencedDirectory,
                    workspaceDataFile.getParentFile().getAbsolutePath()
                );
                continue;
            }

            switch (fileReference.getFileReferenceType()) {
                case DIRECTORY:
                    PackageResolvedResult swiftProjectResult = extractStandalonePackageResolved(workspaceReferencedDirectory);
                    swiftProjectResult.getFailedDetectableResult()
                        .ifPresent(failedDetectableResults::add);
                    DependencyGraph directoryGraph = createGraphFromReference(fileReference, swiftProjectResult.getDependencyGraph());
                    dependencyGraphs.add(directoryGraph);
                    break;
                case XCODE_PROJECT:
                    PackageResolvedResult xcodeProjectResult = extractFromXcodeProject(workspaceReferencedDirectory);
                    xcodeProjectResult.getFailedDetectableResult()
                        .ifPresent(failedDetectableResults::add);
                    DependencyGraph projectGraph = createGraphFromReference(fileReference, xcodeProjectResult.getDependencyGraph());
                    dependencyGraphs.add(projectGraph);
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Unrecognized FileReferenceType: %s", fileReference.getFileReferenceType()));
            }
        }

        if (CollectionUtils.isNotEmpty(failedDetectableResults)) {
            return XcodeWorkspaceResult.failure(failedDetectableResults);
        }

        // TODO: Probably a ProjectDependencyGraph when properly rendered. Hard to test right now - JM 06/2022
        DependencyGraph dependencyGraph = new BasicDependencyGraph();
        dependencyGraphs.forEach(dependencyGraph::copyGraphToRoot);
        return XcodeWorkspaceResult.success(dependencyGraph);
    }

    private DependencyGraph createGraphFromReference(XcodeFileReference fileReference, DependencyGraph extracted) {
        String cleanPath = new BdioIdEscaper().escapeForUri(fileReference.getRelativeLocation().toString());
        DependencyGraph projectDependencyGraph = new ProjectDependencyGraph(ExternalIdCreator.DETECT_FORGE, cleanPath);
        projectDependencyGraph.copyGraphToRoot(extracted);
        return projectDependencyGraph;
    }

    private PackageResolvedResult extractStandalonePackageResolved(File projectDirectory) throws IOException {
        File packageResolved = fileFinder.findFile(projectDirectory, SwiftPackageResolvedDetectable.PACKAGE_RESOLVED_FILENAME);
        if (packageResolved != null) {
            return packageResolvedExtractor.extract(packageResolved);
        }
        return PackageResolvedResult.empty();
    }

    private PackageResolvedResult extractFromXcodeProject(File projectDirectory) throws IOException {
        File searchDirectory = new File(projectDirectory, XcodeProjectDetectable.PACKAGE_RESOLVED_RELATIVE_PATH);
        File packageResolved = fileFinder.findFile(searchDirectory, SwiftPackageResolvedDetectable.PACKAGE_RESOLVED_FILENAME);
        if (packageResolved != null) {
            return packageResolvedExtractor.extract(packageResolved);
        }
        return PackageResolvedResult.empty();
    }
}
