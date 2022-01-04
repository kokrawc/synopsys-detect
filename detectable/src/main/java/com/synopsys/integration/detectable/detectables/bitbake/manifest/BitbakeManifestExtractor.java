package com.synopsys.integration.detectable.detectables.bitbake.manifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.bitbake.common.BitbakeDetectorAlgorithm;
import com.synopsys.integration.detectable.detectables.bitbake.common.BitbakeSession;
import com.synopsys.integration.detectable.detectables.bitbake.common.TaskDependsDotFile;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.graph.BitbakeManifestGraphTransformer;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.ShowRecipesOutputParser;
import com.synopsys.integration.detectable.detectables.bitbake.common.parse.GraphParserTransformer;
import com.synopsys.integration.detectable.detectables.bitbake.dependency.BitbakeRecipesToLayerMapConverter;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.LicenseManifestParser;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.ShowRecipesResults;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.ToolVersionLogger;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class BitbakeManifestExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectableExecutableRunner executableRunner;
    private final FileFinder fileFinder;
    private final GraphParserTransformer graphParserTransformer;
    private final BitbakeManifestGraphTransformer bitbakeManifestGraphTransformer;
    private final ShowRecipesOutputParser showRecipesOutputParser;
    private final BitbakeRecipesToLayerMapConverter bitbakeRecipesToLayerMap;
    private final ToolVersionLogger toolVersionLogger;
    // TODO inject
    private final LicenseManifestFinder licenseManifestFinder = new LicenseManifestFinder();

    public BitbakeManifestExtractor(DetectableExecutableRunner executableRunner, FileFinder fileFinder, GraphParserTransformer graphParserTransformer, BitbakeManifestGraphTransformer bitbakeManifestGraphTransformer,
        ShowRecipesOutputParser showRecipesOutputParser, BitbakeRecipesToLayerMapConverter bitbakeRecipesToLayerMap, ToolVersionLogger toolVersionLogger) {
        this.executableRunner = executableRunner;
        this.fileFinder = fileFinder;
        this.graphParserTransformer = graphParserTransformer;
        this.bitbakeManifestGraphTransformer = bitbakeManifestGraphTransformer;
        this.showRecipesOutputParser = showRecipesOutputParser;
        this.bitbakeRecipesToLayerMap = bitbakeRecipesToLayerMap;
        this.toolVersionLogger = toolVersionLogger;
    }

    public Extraction extract(File sourceDirectory, File buildEnvScript, List<String> sourceArguments, List<String> targetImageNames, boolean followSymLinks,
        Integer searchDepth, ExecutableTarget bash, String licenseManifestFilePath, BitbakeDetectorAlgorithm algorithm) {

        String targetImageName = targetImageNames.get(0);

        // TODO inject?
        BitbakeSession bitbakeSession = new BitbakeSession(fileFinder, executableRunner, sourceDirectory, buildEnvScript, sourceArguments, bash, toolVersionLogger);

        Extraction extraction;
        LicenseManifestParser licenseManifestParser = new LicenseManifestParser(); // TODO inject
        TaskDependsDotFile taskDependsDotFile = new TaskDependsDotFile(); // TODO inject
        DependencyGraph dependencyGraph;
        try {
            File taskDependsFile = taskDependsDotFile.generate(bitbakeSession, sourceDirectory, targetImageName, followSymLinks, searchDepth);
            InputStream dependsFileInputStream = FileUtils.openInputStream(taskDependsFile);
            GraphParser graphParser = new GraphParser(dependsFileInputStream);
            BitbakeGraph bitbakeGraph = graphParserTransformer.transform(graphParser);
            logger.info("Parsed {} recipes nodes from task-depends.dot file", bitbakeGraph.getNodes().size());
            List<String> licenseManifestFileLines = readLicenseManifestFile(sourceDirectory, targetImageName, licenseManifestFilePath);
            Map<String, String> imageRecipes = licenseManifestParser.collectImageRecipes(licenseManifestFileLines);
            logger.info("Found {} image recipes in license.manifest file", imageRecipes.size());
            List<String> bitbakeRecipeCatalogLines = bitbakeSession.executeBitbakeForRecipeLayerLines();
            ShowRecipesResults showRecipesResults = showRecipesOutputParser.parse(bitbakeRecipeCatalogLines);
            logger.info("Found {} recipes on {} layers in show-recipes output", showRecipesResults.getRecipes().size(), showRecipesResults.getLayerNames().size());
            dependencyGraph = bitbakeManifestGraphTransformer.generateGraph(imageRecipes, showRecipesResults, bitbakeGraph, algorithm);
        } catch (IntegrationException | ExecutableRunnerException | IOException e) {
            extraction = new Extraction.Builder()
                .failure(e.getMessage())
                .build();
            return extraction;
        }
        CodeLocation codeLocation = new CodeLocation(dependencyGraph);
        extraction = new Extraction.Builder()
            .success(codeLocation)
            .build();
        return extraction;
    }

    private List<String> readLicenseManifestFile(File sourceDir, String targetImageName, String givenLicenseManifestFilePath) throws IntegrationException {

        Optional<File> licenseManifestFile = licenseManifestFinder.find(sourceDir, targetImageName, givenLicenseManifestFilePath);
        if (!licenseManifestFile.isPresent()) {
            throw new IntegrationException(String.format("Unable to find license.manifest for %s", targetImageName));
        }
        List<String> licenseManifestFileLines;
        try {
            licenseManifestFileLines = FileUtils.readLines(licenseManifestFile.get(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IntegrationException(String.format("License Manifest file %s is non-existent or not readable.", givenLicenseManifestFilePath));
        }
        return licenseManifestFileLines;
    }
}
