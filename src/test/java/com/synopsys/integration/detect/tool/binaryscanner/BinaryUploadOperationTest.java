package com.synopsys.integration.detect.tool.binaryscanner;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.common.util.finder.SimpleFileFinder;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.OperationException;
import com.synopsys.integration.detect.lifecycle.run.data.DockerTargetData;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationFactory;
import com.synopsys.integration.detect.lifecycle.run.step.BinaryScanStepRunner;
import com.synopsys.integration.detect.util.finder.DetectExcludedDirectoryIncludedFileFilter;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.exception.IntegrationException;

public class BinaryUploadOperationTest {
    @Test
    public void testShouldFailOnDirectory() throws OperationException {
        BinaryScanOptions binaryScanOptions = new BinaryScanOptions(Paths.get("."),null, 0, false);
        OperationFactory operationFactory = Mockito.mock(OperationFactory.class);

        Mockito.when(operationFactory.calculateBinaryScanOptions()).thenReturn(binaryScanOptions);

        BinaryScanStepRunner binaryScanStepRunner = new BinaryScanStepRunner(operationFactory);
        Optional<File> result = binaryScanStepRunner.determineBinaryScanFileTarget(DockerTargetData.NO_DOCKER_TARGET);

        Mockito.verify(operationFactory).publishBinaryFailure(Mockito.anyString());
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    public void testMultipleTargetPaths() throws DetectUserFriendlyException, IOException, IntegrationException {
        SimpleFileFinder fileFinder = new SimpleFileFinder();
        DirectoryManager directoryManager = Mockito.mock(DirectoryManager.class);

        File rootDirectory = Files.createTempDirectory("BinaryScannerTest").toFile();
        createDirWithFiles(rootDirectory, "BinaryScannerSubDirectory");

        ArrayList<String> targetPaths = new ArrayList<>();
        targetPaths.add("binaryTestFile_1.txt");
        targetPaths.add("*.text");

        Mockito.when(directoryManager.getSourceDirectory()).thenReturn(rootDirectory);
        Mockito.when(directoryManager.getBinaryOutputDirectory()).thenReturn(rootDirectory);

        BinaryScanFindMultipleTargetsOperation multipleTargets = new BinaryScanFindMultipleTargetsOperation(fileFinder, directoryManager);
        DetectExcludedDirectoryIncludedFileFilter fileFilter = new DetectExcludedDirectoryIncludedFileFilter(Collections.emptyList(), targetPaths);
        Optional<File> zip = multipleTargets.searchForMultipleTargets(fileFilter, false, 3);
        Assertions.assertTrue(zip.isPresent());

        List<String> entries = getZipEntries(zip);
        Assertions.assertTrue(entries.contains("BinaryScannerSubDirectory/binaryTestFile_1.txt"));
        Assertions.assertTrue(entries.contains("BinaryScannerSubDirectory/binaryTestFile_2.text"));

        FileUtils.deleteDirectory(rootDirectory);
    }

    @Test
    public void testDirExclusion() throws DetectUserFriendlyException, IOException, IntegrationException {
        SimpleFileFinder fileFinder = new SimpleFileFinder();
        DirectoryManager directoryManager = Mockito.mock(DirectoryManager.class);

        File rootDirectory = Files.createTempDirectory("BinaryScannerTest").toFile();
        createDirWithFiles(rootDirectory, "includedDir");
        createDirWithFiles(rootDirectory, "excludedDir");
        ArrayList<String> targetPaths = new ArrayList<>();
        targetPaths.add("*.txt");

        Mockito.when(directoryManager.getSourceDirectory()).thenReturn(rootDirectory);
        Mockito.when(directoryManager.getBinaryOutputDirectory()).thenReturn(rootDirectory);

        BinaryScanFindMultipleTargetsOperation multipleTargets = new BinaryScanFindMultipleTargetsOperation(fileFinder, directoryManager);
        DetectExcludedDirectoryIncludedFileFilter fileFilter = new DetectExcludedDirectoryIncludedFileFilter(Arrays.asList("excludedDir"), targetPaths);
        Optional<File> zip = multipleTargets.searchForMultipleTargets(fileFilter, false, 3);
        Assertions.assertTrue(zip.isPresent());

        List<String> entries = getZipEntries(zip);
        Assertions.assertTrue(entries.contains("includedDir/binaryTestFile_1.txt"));
        Assertions.assertTrue(entries.contains("includedDir/binaryTestFile_2.text"));
        Assertions.assertFalse(entries.contains("excludedDir/binaryTestFile_1.txt"));
        Assertions.assertFalse(entries.contains("excludedDir/binaryTestFile_2.text"));

        FileUtils.deleteDirectory(rootDirectory);
    }

    private File createDirWithFiles(final File parentDirectory, String dirName) throws IOException {
        File newDir = new File(parentDirectory, dirName);
        File binaryFile_1 = new File(newDir, "binaryTestFile_1.txt");
        File binaryFile_2 = new File(newDir, "binaryTestFile_2.text");
        FileUtils.write(binaryFile_1, "binary test file 1", StandardCharsets.UTF_8);
        FileUtils.write(binaryFile_2, "binary test file 2", StandardCharsets.UTF_8);
        newDir.mkdirs();
        return newDir;
    }

    @NotNull
    private List<String> getZipEntries(final Optional<File> zip) throws IOException {
        List<String> entries = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip.get()))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                System.out.println(zipEntry.getName());
                entries.add(zipEntry.getName());
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        }
        return entries;
    }
}
