package com.synopsys.integration.detectable.detectables.bitbake.manifest;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class LicenseManifestFinder {

    // TODO this needs refactoring; also, compare to MB's search
    public Optional<File> find(File sourceDir, String targetImageName, String givenLicenseManifestFilePath) {
        if (StringUtils.isNotBlank(givenLicenseManifestFilePath)) {
            File licenseManifestFile = new File (givenLicenseManifestFilePath);
            if (licenseManifestFile.canRead()) {
                return Optional.of(licenseManifestFile);
            }
        }
        // TODO might need to be more flexible?
        Optional<File> licenseFile;
        try {
            File buildDir = new File(sourceDir, "build");
            File tmpDir = new File(buildDir, "tmp");
            File licensesDir = new File(tmpDir, "licenses");
            List<File> licensesDirContents = Arrays.asList(licensesDir.listFiles());
            licenseFile = licensesDirContents.stream().filter(f -> f.getName().startsWith(targetImageName)).findFirst();
            return licenseFile;
        } catch (Exception e) {

        }

        // TODO almost sure this does not make sense; it's what the battery test currently relies on though
        File sourceDirLicenseFile = new File(sourceDir, "license.manifest");
        if (sourceDirLicenseFile.canRead()) {
            return Optional.of(sourceDirLicenseFile);
        }
        return Optional.empty();
    }
}
