package com.synopsys.integration.detectable.detectables.packrat.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.packrat.parse.PackratDescriptionFileParser;
import com.synopsys.integration.detectable.util.OptionalNameOptionalVersion;

class PackratDescriptionFileParserTest {
    @Test
    void invalidReturnsEmpty() {
        PackratDescriptionFileParser packratDescriptionFileParser = new PackratDescriptionFileParser();

        List<String> invalidDescriptionFileLines = new ArrayList<>();
        invalidDescriptionFileLines.add("Package invalidProjectName");
        invalidDescriptionFileLines.add("Type: Package");
        invalidDescriptionFileLines.add("Title: A test title");
        invalidDescriptionFileLines.add("    Dependencies");
        invalidDescriptionFileLines.add("Version bad");
        invalidDescriptionFileLines.add("Author: Test");

        OptionalNameOptionalVersion nameVersion = packratDescriptionFileParser.getProjectNameVersion(invalidDescriptionFileLines);
        Assertions.assertFalse(nameVersion.getVersion().isPresent());
        Assertions.assertFalse(nameVersion.getName().isPresent());
    }

    @Test
    void validGetsProjectNameAndVersion() {
        PackratDescriptionFileParser packratDescriptionFileParser = new PackratDescriptionFileParser();

        List<String> validDescriptionFileLines = new ArrayList<>();
        validDescriptionFileLines.add("Package:TestProjectName ");
        validDescriptionFileLines.add("Type: Package");
        validDescriptionFileLines.add("Title: A test title");
        validDescriptionFileLines.add("    Dependencies");
        validDescriptionFileLines.add("Version:1.0.0 ");
        validDescriptionFileLines.add("Author: Test");

        OptionalNameOptionalVersion nameVersion = packratDescriptionFileParser.getProjectNameVersion(validDescriptionFileLines);
        Assertions.assertTrue(nameVersion.getVersion().isPresent());
        Assertions.assertTrue(nameVersion.getName().isPresent());
        Assertions.assertEquals("TestProjectName", nameVersion.getName().get());
        Assertions.assertEquals("1.0.0", nameVersion.getVersion().get());
    }
}