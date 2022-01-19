package com.synopsys.integration.detectable.detectables.nuget.future.range;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class NuGetVersion {
    private final SimpleVersion version;

    public String[] getReleaseLabels() {
        return releaseLabels;
    }

    private final String[] releaseLabels;
    private final String buildmetaData;
    private final String originalVersion;

    public NuGetVersion(SimpleVersion version, String[] releaseLabels, @NotNull String buildmetaData, String originalVersion) {
        this.version = version;
        this.releaseLabels = releaseLabels;
        this.buildmetaData = buildmetaData;
        this.originalVersion = originalVersion;
    }

    public boolean IsPrerelease() {
        if (this.releaseLabels != null) {
            for (int i = 0; i < this.releaseLabels.length; i++) {
                if (!StringUtils.isEmpty(this.releaseLabels[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getRelease() {
        if (this.releaseLabels != null) {
            if (this.releaseLabels.length == 1) {
                // There is exactly 1 label
                return this.releaseLabels[0];
            } else {
                // Join all labels
                return String.join(".", this.releaseLabels);
            }
        }

        return "";
    }

    public static NuGetVersion TryParse(String value) {
        if (value != null) {
            // trim the value before passing it in since we not strict here
            NuGetSections sections = ParseSections(value.trim());

            // null indicates the string did not meet the rules
            if (sections != null
                && !StringUtils.isEmpty(sections.versionString)) {
                String versionPart = sections.versionString;

                if (versionPart.indexOf('.') < 0) {
                    // System.Version requires at least a 2 part version to parse.
                    versionPart += ".0";
                }

                Optional<SimpleVersion> version = SimpleVersion.parse(versionPart);
                if (version.isPresent()) {
                    SimpleVersion systemVersion = version.get();
                    // labels
                    if (sections.releaseLabels != null) {
                        for (int i = 0; i < sections.releaseLabels.length; i++) {
                            if (!IsValidPart(sections.releaseLabels[i], false)) {
                                return null;
                            }
                        }
                    }

                    // build metadata
                    if (sections.buildMetadata != null
                        && !IsValid(sections.buildMetadata, true)) {
                        return null;
                    }

                    SimpleVersion ver = NormalizeVersionValue(systemVersion);

                    String originalVersion = value;

                    if (originalVersion.indexOf(' ') > -1) {
                        originalVersion = StringUtils.replace(value, " ", "");
                    }

                    String buildmetaData = "";
                    if (sections.buildMetadata != null) {
                        buildmetaData = sections.buildMetadata;
                    }

                    return new NuGetVersion(ver,
                        sections.releaseLabels,
                        buildmetaData,
                        originalVersion);
                }
            }
        }

        return null;
    }

    static SimpleVersion NormalizeVersionValue(SimpleVersion version) {
        SimpleVersion normalized = version;

        if (version.getBuild() == null || version.getBuild() < 0
            || version.getRevision() == null || version.getRevision() < 0) {
            normalized = new SimpleVersion(
                version.getMajor(),
                version.getMinor(),
                Integer.max(version.getBuild() == null ? 0 : version.getBuild(), 0),
                Integer.max(version.getRevision() == null ? 0 : version.getRevision(), 0));
        }

        return normalized;
    }

    static boolean IsValid(String s, boolean allowLeadingZeros) {
        String[] parts = StringUtils.split(s, '.');

        // Check each part individually
        for (int i = 0; i < parts.length; i++) {
            if (!IsValidPart(parts[i], allowLeadingZeros)) {
                return false;
            }
        }

        return true;
    }

    static boolean IsValidPart(String s, boolean allowLeadingZeros) {
        if (s.length() == 0) {
            // empty labels are not allowed
            return false;
        }

        // 0 is fine, but 00 is not.
        // 0A counts as an alpha numeric string where zeros are not counted
        if (!allowLeadingZeros
            && s.length() > 1
            && s.charAt(0) == '0') {
            boolean allDigits = true;

            // Check if all characters are digits.
            // The first is already checked above
            for (int i = 1; i < s.length(); i++) {
                if (!IsDigit(s.charAt(i))) {
                    allDigits = false;
                    break;
                }
            }

            if (allDigits) {
                // leading zeros are not allowed in numeric labels
                return false;
            }
        }

        for (int i = 0; i < s.length(); i++) {
            // Verify that the part contains only allowed characters
            if (!IsLetterOrDigitOrDash(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    static boolean IsLetterOrDigitOrDash(char c) {
        int x = (int) c;

        // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-"
        return (x >= 48 && x <= 57) || (x >= 65 && x <= 90) || (x >= 97 && x <= 122) || x == 45;
    }

    static boolean IsDigit(char c) {
        int x = (int) c;

        // "0123456789"
        return (x >= 48 && x <= 57);
    }

    public SimpleVersion getVersion() {
        return version;
    }

    public String getBuildmetaData() {
        return buildmetaData;
    }

    public String getOriginalVersion() {
        return originalVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NuGetVersion that = (NuGetVersion) o;

        if (version != null ? !version.equals(that.version) : that.version != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(releaseLabels, that.releaseLabels))
            return false;
        return buildmetaData != null ? buildmetaData.equals(that.buildmetaData) : that.buildmetaData == null;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(releaseLabels);
        result = 31 * result + (buildmetaData != null ? buildmetaData.hashCode() : 0);
        return result;
    }

    private static class NuGetSections {
        public String versionString = null;
        public String[] releaseLabels = null;
        public String buildMetadata = null;
    }

    private static NuGetSections ParseSections(String value) {
        NuGetSections sections = new NuGetSections();
        int dashPos = -1;
        int plusPos = -1;

        boolean end = false;
        for (int i = 0; i < value.length(); i++) {
            end = (i == value.length() - 1);

            if (dashPos < 0) {
                if (end || value.charAt(i) == '-' || value.charAt(i) == '+') {
                    int endPos = i + (end ? 1 : 0);
                    sections.versionString = value.substring(0, endPos);

                    dashPos = i;

                    if (value.charAt(i) == '+') {
                        plusPos = i;
                    }
                }
            } else if (plusPos < 0) {
                if (end || value.charAt(i) == '+') {
                    int start = dashPos + 1;
                    int endPos = i + (end ? 1 : 0);
                    String releaseLabel = value.substring(start, endPos - start);

                    sections.releaseLabels = StringUtils.split(releaseLabel, '.');

                    plusPos = i;
                }
            } else if (end) {
                int start = plusPos + 1;
                int endPos = i + 1; //because end is guaranteed true
                sections.buildMetadata = value.substring(start, endPos - start);
            }
        }

        return sections;
    }

}
