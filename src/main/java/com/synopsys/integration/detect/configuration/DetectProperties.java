package com.synopsys.integration.detect.configuration;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.IndividualFileMatching;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.SnippetMatching;
import com.synopsys.integration.configuration.property.Properties;
import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.configuration.property.base.PassthroughProperty;
import com.synopsys.integration.configuration.property.base.TypedProperty;
import com.synopsys.integration.configuration.property.types.bool.BooleanProperty;
import com.synopsys.integration.configuration.property.types.enumallnone.enumeration.AllNoneEnum;
import com.synopsys.integration.configuration.property.types.enumallnone.enumeration.NoneEnum;
import com.synopsys.integration.configuration.property.types.enumallnone.property.AllNoneEnumListProperty;
import com.synopsys.integration.configuration.property.types.enumallnone.property.NoneEnumListProperty;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumProperty;
import com.synopsys.integration.configuration.property.types.enumextended.ExtendedEnumValue;
import com.synopsys.integration.configuration.property.types.enums.EnumListProperty;
import com.synopsys.integration.configuration.property.types.enums.EnumProperty;
import com.synopsys.integration.configuration.property.types.integer.IntegerProperty;
import com.synopsys.integration.configuration.property.types.integer.NullableIntegerProperty;
import com.synopsys.integration.configuration.property.types.longs.LongProperty;
import com.synopsys.integration.configuration.property.types.path.NullablePathProperty;
import com.synopsys.integration.configuration.property.types.path.PathListProperty;
import com.synopsys.integration.configuration.property.types.string.CaseSensitiveStringListProperty;
import com.synopsys.integration.configuration.property.types.string.NullableStringProperty;
import com.synopsys.integration.configuration.property.types.string.StringListProperty;
import com.synopsys.integration.configuration.property.types.string.StringProperty;
import com.synopsys.integration.detect.configuration.enumeration.BlackduckScanMode;
import com.synopsys.integration.detect.configuration.enumeration.DetectCategory;
import com.synopsys.integration.detect.configuration.enumeration.DetectGroup;
import com.synopsys.integration.detect.configuration.enumeration.DetectMajorVersion;
import com.synopsys.integration.detect.configuration.enumeration.DetectTargetType;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.configuration.enumeration.RapidCompareMode;
import com.synopsys.integration.detect.tool.signaturescanner.enums.ExtendedIndividualFileMatchingMode;
import com.synopsys.integration.detect.tool.signaturescanner.enums.ExtendedSnippetMode;
import com.synopsys.integration.detect.workflow.bdio.AggregateMode;
import com.synopsys.integration.detectable.detectables.bazel.WorkspaceRule;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDependencyType;
import com.synopsys.integration.detectable.detectables.conan.cli.config.ConanDependencyType;
import com.synopsys.integration.detectable.detectables.dart.pubdep.DartPubDependencyType;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModDependencyType;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleConfigurationType;
import com.synopsys.integration.detectable.detectables.lerna.LernaPackageType;
import com.synopsys.integration.detectable.detectables.npm.NpmDependencyType;
import com.synopsys.integration.detectable.detectables.packagist.PackagistDependencyType;
import com.synopsys.integration.detectable.detectables.pear.PearDependencyType;
import com.synopsys.integration.detectable.detectables.pipenv.parse.PipenvDependencyType;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmDependencyType;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmDependencyTypeV2;
import com.synopsys.integration.detectable.detectables.rubygems.GemspecDependencyType;
import com.synopsys.integration.detectable.detectables.yarn.YarnDependencyType;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.log.LogLevel;

// java:S1192: Sonar wants constants defined for fromVersion when setting property info.
// java:S1123: Warning about deprecations not having Java doc.
@SuppressWarnings({ "java:S1123", "java:S1192" })
public class DetectProperties {
    private static final String SBT_REPORT_DEPRECATION_MESSAGE = "This property is being removed. Sbt will no longer parse report files but instead will use a dependency resolution plugin. Please install the appropriate plugin in the future.";

    private DetectProperties() {
    }

    public static final NullableStringProperty BLACKDUCK_API_TOKEN =
        NullableStringProperty.newBuilder("blackduck.api.token")
            .setInfo("Black Duck API Token", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("The access token used to authenticate with the Black Duck Server.")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .build();

    public static final BooleanProperty BLACKDUCK_OFFLINE_MODE =
        BooleanProperty.newBuilder("blackduck.offline.mode", false)
            .setInfo("Offline Mode", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "This can disable any Black Duck communication - if true, Detect will not upload BDIO files, it will not check policies, and it will not download and install the signature scanner.")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK, DetectGroup.OFFLINE, DetectGroup.DEFAULT)
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_HOST =
        NullableStringProperty.newBuilder("blackduck.proxy.host")
            .setInfo("Proxy Host", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("Hostname for proxy server.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final StringListProperty BLACKDUCK_PROXY_IGNORED_HOSTS =
        StringListProperty.newBuilder("blackduck.proxy.ignored.hosts", emptyList())
            .setInfo("Bypass Proxy Hosts", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "A comma separated list of regular expression host patterns that should not use the proxy.",
                "This property accepts Java regular expressions. Refer to the <i>Configuring Synopsys Detect</i> > <i>Java regular expression support</i> page for more details."
            )
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .setExample("blackduck[0-9]+.mycompany.com")
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_NTLM_DOMAIN =
        NullableStringProperty.newBuilder("blackduck.proxy.ntlm.domain")
            .setInfo("NTLM Proxy Domain", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("NTLM Proxy domain.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_NTLM_WORKSTATION =
        NullableStringProperty.newBuilder("blackduck.proxy.ntlm.workstation")
            .setInfo("NTLM Proxy Workstation", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("NTLM Proxy workstation.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_PASSWORD =
        NullableStringProperty.newBuilder("blackduck.proxy.password")
            .setInfo("Proxy Password", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("Proxy password.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_PORT =
        NullableStringProperty.newBuilder("blackduck.proxy.port")
            .setInfo("Proxy Port", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("Proxy port.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty BLACKDUCK_PROXY_USERNAME =
        NullableStringProperty.newBuilder("blackduck.proxy.username")
            .setInfo("Proxy Username", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("Proxy username.")
            .setGroups(DetectGroup.PROXY, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final BooleanProperty BLACKDUCK_TRUST_CERT =
        BooleanProperty.newBuilder("blackduck.trust.cert", false)
            .setInfo("Trust All SSL Certificates", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("If true, automatically trust the certificate for the current run of Detect only.")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty BLACKDUCK_URL =
        NullableStringProperty.newBuilder("blackduck.url")
            .setInfo("Black Duck URL", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("URL of the Black Duck server.")
            .setExample("https://blackduck.mydomain.com")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK, DetectGroup.DEFAULT)
            .build();

    public static final IntegerProperty DETECT_PARALLEL_PROCESSORS =
        IntegerProperty.newBuilder("detect.parallel.processors", 1)
            .setInfo("Detect Parallel Processors", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "The number of threads to run processes in parallel, defaults to 1, but if you specify less than or equal to 0, the number of processors on the machine will be used.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullablePathProperty DETECT_BASH_PATH =
        NullablePathProperty.newBuilder("detect.bash.path")
            .setInfo("Bash Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Path to the Bash executable.", "If set, Detect will use the given Bash executable instead of searching for one.")
            .setExample("/usr/bin/bash")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_BAZEL_PATH =
        NullablePathProperty.newBuilder("detect.bazel.path")
            .setInfo("Bazel Executable", DetectPropertyFromVersion.VERSION_5_2_0)
            .setHelp("The path to the Bazel executable.")
            .setGroups(DetectGroup.BAZEL, DetectGroup.GLOBAL)
            .build();

    public static final NullableStringProperty DETECT_BAZEL_TARGET =
        NullableStringProperty.newBuilder("detect.bazel.target")
            .setInfo("Bazel Target", DetectPropertyFromVersion.VERSION_5_2_0)
            .setHelp("The Bazel target (for example, //foo:foolib) for which dependencies are collected. For Detect to run Bazel, this property must be set.")
            .setGroups(DetectGroup.BAZEL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final StringListProperty DETECT_BAZEL_CQUERY_OPTIONS =
        StringListProperty.newBuilder("detect.bazel.cquery.options", emptyList())
            .setInfo("Bazel cquery additional options", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp("A comma-separated list of additional options to pass to the bazel cquery command.")
            .setGroups(DetectGroup.BAZEL, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Change to AllEnumListProperty. Perhaps change default to ALL. Add .deprecateNone(message)
    public static final AllNoneEnumListProperty<WorkspaceRule> DETECT_BAZEL_WORKSPACE_RULES =
        AllNoneEnumListProperty.newBuilder("detect.bazel.workspace.rules", emptyList(), WorkspaceRule.class)
            .setInfo("Bazel workspace rules", DetectPropertyFromVersion.VERSION_7_12_0)
            .setHelp(
                "By default Detect discovers Bazel dependencies using all of the supported Bazel workspace rules that it finds in the WORKSPACE file. Alternatively you can use this property to specify the list of Bazel workspace rules Detect should use."
            )
            .setGroups(DetectGroup.BAZEL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_CONAN_PATH =
        NullablePathProperty.newBuilder("detect.conan.path")
            .setInfo("Conan Executable", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp("The path to the conan executable.")
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NoneEnumListProperty<ConanDependencyType> DETECT_CONAN_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.conan.dependency.types.excluded", NoneEnum.NONE, ConanDependencyType.class)
            .setInfo("Conan Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Conan dependency types", "detect.conan.include.build.dependencies"))
            .setExample(ConanDependencyType.BUILD.name())
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullableStringProperty DETECT_CONAN_ARGUMENTS =
        NullableStringProperty.newBuilder("detect.conan.arguments")
            .setInfo("Additional Conan Arguments", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp(
                "A space-separated list of additional arguments to add to the 'conan info' command line when running Detect against a Conan project. Detect will execute the command 'conan info {additional arguments} .'")
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .setExample("\"--profile clang --profile cmake_316\"")
            .build();

    public static final NullablePathProperty DETECT_CONAN_LOCKFILE_PATH =
        NullablePathProperty.newBuilder("detect.conan.lockfile.path")
            .setInfo("Conan Lockfile", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp(
                "The path to the conan lockfile to apply when running 'conan info' to get the dependency graph. If set, Detect will execute the command 'conan info --lockfile {lockfile} .'")
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Should the default change? Has KB matching of Conan projects improved? If so, this could change to 'true'.
    public static final BooleanProperty DETECT_CONAN_REQUIRE_PREV_MATCH =
        BooleanProperty.newBuilder("detect.conan.attempt.package.revision.match", false)
            .setInfo(
                "Attempt Package Revision Match",
                DetectPropertyFromVersion.VERSION_6_8_0
            )
            .setHelp(
                "If package revisions are available (a Conan lock file is found or provided, and Conan's revisions feature is enabled), require that each dependency's package revision match the package revision of the component in the KB.")
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Revise 'help' wording since we should be only producing 1 BDIO document per run in 8.0.0
    public static final NullablePathProperty DETECT_BDIO_OUTPUT_PATH =
        NullablePathProperty.newBuilder("detect.bdio.output.path")
            .setInfo("BDIO Output Directory", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the output directory for all BDIO files.", "If not set, the BDIO files are placed in a 'BDIO' subdirectory of the output directory.")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullableStringProperty DETECT_BDIO_FILE_NAME =
        NullableStringProperty.newBuilder("detect.bdio.file.name")
            .setInfo("BDIO File Name", DetectPropertyFromVersion.VERSION_7_9_0)
            .setHelp(
                "The desired file name of the single bdio file Detect produces in the BDIO Output Directory.",
                "If not set, the file name is generated from your project, version and code location names."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_BINARY_SCAN_FILE =
        NullablePathProperty.newBuilder("detect.binary.scan.file.path")
            .setInfo("Binary Scan Target", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "If specified, this file and this file only will be uploaded for binary scan analysis. This property takes precedence over detect.binary.scan.file.name.patterns. The BINARY_SCAN tool does not provide project and version name defaults to Detect, so you need to set project and version names via properties when only the BINARY_SCAN tool is invoked.")
            .setGroups(DetectGroup.BINARY_SCANNER, DetectGroup.SOURCE_PATH)
            .build();

    public static final StringListProperty DETECT_BINARY_SCAN_FILE_NAME_PATTERNS =
        StringListProperty.newBuilder("detect.binary.scan.file.name.patterns", emptyList())
            .setInfo("Binary Scan Filename Patterns", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "If specified, all files in the source directory whose names match these file name patterns will be zipped and uploaded for binary scan analysis. This property will not be used if detect.binary.scan.file.path is specified. Search depth is controlled by property detect.binary.scan.search.depth. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details.")
            .setGroups(DetectGroup.BINARY_SCANNER, DetectGroup.SOURCE_PATH)
            .setExample("*.jar")
            .build();

    // TODO: Consider raising default search depth
    public static final IntegerProperty DETECT_BINARY_SCAN_SEARCH_DEPTH =
        IntegerProperty.newBuilder("detect.binary.scan.search.depth", 0)
            .setInfo("Binary Scan Search Depth", DetectPropertyFromVersion.VERSION_6_9_0)
            .setHelp(
                "When binary scan filename patterns are being used to search for binary files to scan, this property sets the depth at which Detect will search for files (that match those patterns) to upload for binary scan analysis.")
            .setGroups(DetectGroup.BINARY_SCANNER, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Consider removing environment sourcing code in 8.0.0. IDETECT-3167
    public static final StringProperty DETECT_BITBAKE_BUILD_ENV_NAME =
        StringProperty.newBuilder("detect.bitbake.build.env.name", "oe-init-build-env")
            .setInfo("BitBake Init Script Name", DetectPropertyFromVersion.VERSION_4_4_0)
            .setHelp("The name of the build environment init script.")
            .setGroups(DetectGroup.BITBAKE, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Change to (Nullable?)StringProperty
    public static final StringListProperty DETECT_BITBAKE_PACKAGE_NAMES =
        StringListProperty.newBuilder("detect.bitbake.package.names", emptyList())
            .setInfo("BitBake Package Names", DetectPropertyFromVersion.VERSION_4_4_0)
            .setHelp("A comma-separated list of package names from which dependencies are extracted.")
            .setGroups(DetectGroup.BITBAKE, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Consider removing environment sourcing code in 8.0.0. IDETECT-3167
    public static final StringListProperty DETECT_BITBAKE_SOURCE_ARGUMENTS =
        StringListProperty.newBuilder("detect.bitbake.source.arguments", emptyList())
            .setInfo("BitBake Source Arguments", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp("A comma-separated list of arguments to supply when sourcing the build environment init script.")
            .setGroups(DetectGroup.BITBAKE, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: The task-depends.dot should be in working directory now, should verify? Maybe keep if IDETECT-3167 is completed
    public static final IntegerProperty DETECT_BITBAKE_SEARCH_DEPTH =
        IntegerProperty.newBuilder("detect.bitbake.search.depth", 1)
            .setInfo("BitBake Search Depth", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp("The depth at which Detect will search for files generated by Bitbake.")
            .setGroups(DetectGroup.BITBAKE, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NoneEnumListProperty<BitbakeDependencyType> DETECT_BITBAKE_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.bitbake.dependency.types.excluded", NoneEnum.NONE, BitbakeDependencyType.class)
            .setInfo("Bitbake Excluded Dependency Types", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(
                "The dependency types to exclude from the results.",
                "BUILD dependencies include recipes that are not declared in the license.manifest file, and native recipes. When excluding BUILD dependencies, Detect requires the license.manifest file (found under the {builddir}/tmp directory)."
            )
            .setExample("BUILD")
            .setGroups(DetectGroup.BITBAKE, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullableStringProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_ARGUMENTS =
        NullableStringProperty.newBuilder("detect.blackduck.signature.scanner.arguments")
            .setInfo("Signature Scanner Arguments", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "Additional arguments to use when running the Black Duck signature scanner.",
                "For example: Suppose you are running in bash on Linux and want to use the signature scanner's ability to read a list of directories to exclude from a file (using the signature scanner --exclude-from option). You tell the signature scanner read excluded directories from a file named excludes.txt in your home directory with: --detect.blackduck.signature.scanner.arguments='--exclude-from \\${HOME}/excludes.txt'"
            )
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL)
            .build();

    // TODO: JP don't like it
    public static final NullableStringProperty PROJECT_INSPECTOR_ARGUMENTS =
        NullableStringProperty.newBuilder("detect.project.inspector.arguments")
            .setInfo("Project Inspector Additional Arguments", DetectPropertyFromVersion.VERSION_7_7_0)
            .setHelp("A space-separated list of additional options to pass to all invocations of the project inspector.")
            .setGroups(DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Consider detect.blackduck.signature.scanner.search=COPYRIGHT,LICENSE,ALL,NONE
    public static final BooleanProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_COPYRIGHT_SEARCH =
        BooleanProperty.newBuilder("detect.blackduck.signature.scanner.copyright.search", false)
            .setInfo("Signature Scanner Copyright Search", DetectPropertyFromVersion.VERSION_6_4_0)
            .setHelp("When set to true, user will be able to scan and discover copyright names in Black Duck. Corresponding Signature Scanner CLI Argument: --copyright-search.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER)
            .build();

    public static final BooleanProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN =
        BooleanProperty.newBuilder("detect.blackduck.signature.scanner.dry.run", false)
            .setInfo("Signature Scanner Dry Run", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "If set to true, the signature scanner results are not uploaded to Black Duck, and the scanner results are written to disk via the Signature Scanner CLI argument: --dryRunWriteDir.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL)
            .build();

    public static final ExtendedEnumProperty<ExtendedIndividualFileMatchingMode, IndividualFileMatching> DETECT_BLACKDUCK_SIGNATURE_SCANNER_INDIVIDUAL_FILE_MATCHING =
        ExtendedEnumProperty.newBuilderExtendedDefault(
                "detect.blackduck.signature.scanner.individual.file.matching",
                ExtendedIndividualFileMatchingMode.NONE,
                ExtendedIndividualFileMatchingMode.class,
                IndividualFileMatching.class
            )
            .setInfo("Individual File Matching", DetectPropertyFromVersion.VERSION_6_2_0)
            .setHelp("Users may set this property to indicate what types of files they want to match. Corresponding Signature Scanner CLI Argument: --individualFileMatching.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER)
            .build();

    // TODO: Consider detect.blackduck.signature.scanner.search=COPYRIGHT,LICENSE,ALL,NONE
    public static final BooleanProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_LICENSE_SEARCH =
        BooleanProperty.newBuilder("detect.blackduck.signature.scanner.license.search", false)
            .setInfo("Signature Scanner License Search", DetectPropertyFromVersion.VERSION_6_2_0)
            .setHelp("When set to true, user will be able to scan and discover license names in Black Duck. Corresponding Signature Scanner CLI Argument: --license-search.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER)
            .build();

    public static final NullablePathProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_LOCAL_PATH =
        NullablePathProperty.newBuilder("detect.blackduck.signature.scanner.local.path")
            .setInfo("Signature Scanner Local Path", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "To use a local signature scanner, specify the path where the signature scanner was unzipped. This will likely look similar to 'scan.cli-x.y.z' and includes the 'bin, icon, jre, and lib' directories of the expanded scan.cli.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL)
            .build();

    public static final IntegerProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_MEMORY =
        IntegerProperty.newBuilder("detect.blackduck.signature.scanner.memory", 4096)
            .setInfo("Signature Scanner Memory", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp("The memory for the scanner to use.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final PathListProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_PATHS =
        PathListProperty.newBuilder("detect.blackduck.signature.scanner.paths", emptyList())
            .setInfo("Signature Scanner Target Paths", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "If this property is not set, the signature scanner target path is the source path (see property detect.source.path). If this property is set, the paths provided in this property's value will be signature scanned instead (the signature scanner will be executed once for each provided path).")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL)
            .build();

    public static final ExtendedEnumProperty<ExtendedSnippetMode, SnippetMatching> DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MATCHING =
        ExtendedEnumProperty.newBuilder(
                "detect.blackduck.signature.scanner.snippet.matching",
                ExtendedEnumValue.ofExtendedValue(ExtendedSnippetMode.NONE),
                ExtendedSnippetMode.class,
                SnippetMatching.class
            )
            .setInfo("Snippet Matching", DetectPropertyFromVersion.VERSION_5_5_0)
            .setHelp(
                "Use this value to enable the various snippet scanning modes. For a full explanation, please refer to the 'Running a component scan using the Signature Scanner command line' section in your Black Duck server's online help. Corresponding Signature Scanner CLI Arguments: --snippet-matching, --snippet-matching-only, --full-snippet-scan.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final BooleanProperty DETECT_BLACKDUCK_SIGNATURE_SCANNER_UPLOAD_SOURCE_MODE =
        BooleanProperty.newBuilder("detect.blackduck.signature.scanner.upload.source.mode", false)
            .setInfo("Upload source mode", DetectPropertyFromVersion.VERSION_5_4_0)
            .setHelp(
                "If set to true, the signature scanner will, if supported by your Black Duck version, upload source code to Black Duck. Corresponding Signature Scanner CLI Argument: --upload-source.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Outta here!
    public static final BooleanProperty DETECT_BUILDLESS =
        BooleanProperty.newBuilder("detect.detector.buildless", false)
            .setInfo("Buildless Mode", DetectPropertyFromVersion.VERSION_5_4_0)
            .setHelp("If set to true, only Detector's capable of running without a build will be run.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .build();

    public static final BooleanProperty DETECT_CLEANUP =
        BooleanProperty.newBuilder("detect.cleanup", true)
            .setInfo("Cleanup Output", DetectPropertyFromVersion.VERSION_3_2_0)
            .setHelp("If true, the files created by Detect will be cleaned up.")
            .setGroups(DetectGroup.CLEANUP, DetectGroup.GLOBAL)
            .build();

    public static final NullableStringProperty DETECT_CLONE_PROJECT_VERSION_NAME =
        NullableStringProperty.newBuilder("detect.clone.project.version.name")
            .setInfo("Clone Project Version Name", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "The name of the project version to clone this project version from. Respects the given Clone Categories in detect.project.clone.categories or as set on the Black Duck server.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.GLOBAL, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final BooleanProperty DETECT_CLONE_PROJECT_VERSION_LATEST =
        BooleanProperty.newBuilder("detect.clone.project.version.latest", false)
            .setInfo("Clone Latest Project Version", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp("If set to true, detect will attempt to use the latest project version as the clone for this project. The project must exist and have at least one version.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.GLOBAL, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Currently misleading "base-name"
    public static final NullableStringProperty DETECT_CODE_LOCATION_NAME =
        NullableStringProperty.newBuilder("detect.code.location.name")
            .setInfo("Scan Name", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp(
                "An override for the base name Detect will use for the scan (codelocation) it creates. Detect appends a suffix to the base name that indicates the source (\"scan\" for the signature scanner, \"gradle/bom\" for the Gradle detector, etc.). If this property is set and multiple code locations are generated from the same source, Detect will also append an index to avoid name collisions. When this property is set, detect.project.codelocation.prefix and detect.project.codelocation.suffix are ignored.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_CONDA_ENVIRONMENT_NAME =
        NullableStringProperty.newBuilder("detect.conda.environment.name")
            .setInfo("Anaconda Environment Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The name of the anaconda environment used by your project.")
            .setGroups(DetectGroup.CONDA, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_CONDA_PATH =
        NullablePathProperty.newBuilder("detect.conda.path")
            .setInfo("Conda Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the conda executable.")
            .setGroups(DetectGroup.CONDA, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_CPAN_PATH =
        NullablePathProperty.newBuilder("detect.cpan.path")
            .setInfo("cpan Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the cpan executable.")
            .setGroups(DetectGroup.CPAN, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_CPANM_PATH =
        NullablePathProperty.newBuilder("detect.cpanm.path")
            .setInfo("cpanm Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the cpanm executable.")
            .setGroups(DetectGroup.CPAN, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_DART_PATH =
        NullablePathProperty.newBuilder("detect.dart.path")
            .setInfo("dart Executable", DetectPropertyFromVersion.VERSION_7_5_0)
            .setHelp("The path to the dart executable.")
            .setGroups(DetectGroup.DART, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_FLUTTER_PATH =
        NullablePathProperty.newBuilder("detect.flutter.path")
            .setInfo("flutter Executable", DetectPropertyFromVersion.VERSION_7_5_0)
            .setHelp("The path to the flutter executable.")
            .setGroups(DetectGroup.DART, DetectGroup.GLOBAL)
            .build();

    public static final NoneEnumListProperty<PipenvDependencyType> DETECT_PIPFILE_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.pipfile.dependency.types.excluded", NoneEnum.NONE, PipenvDependencyType.class)
            .setInfo("Pipfile Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_13_0)
            .setHelp(
                "A comma-separated list of dependency types that will be excluded.",
                "If DEV is excluded, the Pipfile Lock Detector will exclude 'develop' dependencies when parsing the Pipfile.lock file."
            )
            .setExample(PipenvDependencyType.DEV.name())
            .setGroups(DetectGroup.PIP, DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .build();

    public static final NoneEnumListProperty<DartPubDependencyType> DETECT_PUB_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.pub.dependency.types.excluded", NoneEnum.NONE, DartPubDependencyType.class)
            .setInfo("Dart Pub Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(
                createDefaultDrivenPropertyHelpText("Dart pub dependency types", "detect.pub.deps.exclude.dev"),
                "If DEV is excluded, the Dart Detector will pass the option --no-dev when running the command 'pub deps'."
            )
            .setExample(DartPubDependencyType.DEV.name())
            .setGroups(DetectGroup.DART, DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .build();

    // TODO: Increase the depth. Ideally based on some kind of data
    public static final IntegerProperty DETECT_DETECTOR_SEARCH_DEPTH =
        IntegerProperty.newBuilder("detect.detector.search.depth", 0)
            .setInfo("Detector Search Depth", DetectPropertyFromVersion.VERSION_3_2_0)
            .setHelp(
                "Depth of subdirectories within the source directory to which Detect will search for files that indicate whether a detector applies.",
                "A value of 0 (the default) tells Detect not to search any subdirectories, a value of 1 tells Detect to search first-level subdirectories, etc."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.DETECTOR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Consider changing to ENUM for future nesting control
    public static final BooleanProperty DETECT_DETECTOR_SEARCH_CONTINUE =
        BooleanProperty.newBuilder("detect.detector.search.continue", false)
            .setInfo("Detector Search Continue", DetectPropertyFromVersion.VERSION_3_2_0)
            .setHelp(
                "If true, the bom tool search will continue to look for nested bom tools of the same type to the maximum search depth, see the detailed help for more information.",
                "If true, Detect will find Maven projects that are in subdirectories of a Maven project and Gradle projects that are in subdirectories of Gradle projects, etc. "
                    + "If false, Detect will only find bom tools in subdirectories of a project if they are of a different type such as an Npm project in a subdirectory of a Gradle project."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.DETECTOR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final BooleanProperty DETECT_DIAGNOSTIC =
        BooleanProperty.newBuilder("detect.diagnostic", false)
            .setInfo("Diagnostic Mode", DetectPropertyFromVersion.VERSION_6_5_0)
            .setHelp(
                "When enabled, diagnostic mode collects all files generated by Synopsys Detect and zips the files using a unique run ID. It includes logs, BDIO files, extraction files, and reports.")
            .setGroups(DetectGroup.DEBUG, DetectGroup.GLOBAL)
            .build();

    public static final BooleanProperty DETECT_DIAGNOSTIC_EXTENDED =
        BooleanProperty.newBuilder("detect.diagnostic.extended", false)
            .setInfo("Diagnostic Mode Extended", DetectPropertyFromVersion.VERSION_6_5_0)
            .setHelp("When enabled, Synopsys Detect performs the actions of --detect.diagnostic, but also includes relevant files such as lock files and build artifacts.")
            .setGroups(DetectGroup.DEBUG, DetectGroup.GLOBAL)
            .build();

    // TODO: Remove mentions of Polaris. The property potentially still has relevance.
    public static final BooleanProperty DETECT_IGNORE_CONNECTION_FAILURES =
        BooleanProperty.newBuilder("detect.ignore.connection.failures", false)
            .setInfo("Detect Ignore Connection Failures", DetectPropertyFromVersion.VERSION_5_3_0)
            .setHelp(
                "If true, Detect will ignore any products that it cannot connect to.",
                "If true, when Detect attempts to boot a product it will also check if it can communicate with it - if it cannot, it will not run the product."
            )
            .setGroups(DetectGroup.GENERAL, DetectGroup.BLACKDUCK_SERVER, DetectGroup.POLARIS)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Consider the script using this property. New Jira ticket please
    public static final PassthroughProperty PHONEHOME_PASSTHROUGH =
        PassthroughProperty.newBuilder("detect.phone.home.passthrough")
            .setInfo("Phone Home Passthrough", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp("Additional values may be sent home for usage information. The keys will be sent without the prefix.")
            .setGroups(DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: This could go away if we more tightly integrated Docker Inspector code within Detect. Yuuuge effort
    public static final PassthroughProperty DOCKER_PASSTHROUGH =
        PassthroughProperty.newBuilder("detect.docker.passthrough")
            .setInfo("Docker Passthrough", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "Additional properties may be passed to the docker inspector by adding the prefix detect.docker.passthrough to each Docker Inspector property name and assigning a value. The 'detect.docker.passthrough' prefix will be removed from the property name to generate the property name passed to Docker Inspector (with the given value).")
            .setGroups(DetectGroup.DOCKER, DetectGroup.DEFAULT)
            .setCategory(DetectCategory.Advanced)
            .setExample("(This example is unusual in that it shows a complete propertyname=value) detect.docker.passthrough.imageinspector.service.log.length=1000")
            .build();

    public static final NullableStringProperty DETECT_DOCKER_IMAGE =
        NullableStringProperty.newBuilder("detect.docker.image")
            .setInfo("Docker Image Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The Docker image name (repo:tag) to inspect.",
                "For Detect to run Docker Inspector, either this property, detect.docker.tar, or detect.docker.image.id must be set. Docker Inspector finds packages installed by the Linux package manager in Linux-based images. detect.docker.image, detect.docker.tar, and detect.docker.image.id are three alternative ways to specify an image (you should only set one of these properties). When a value of this property is provided, Docker Inspector will use the Docker engine to pull the image."
            )
            .setExample("centos:centos8")
            .setGroups(DetectGroup.DOCKER, DetectGroup.SOURCE_PATH)
            .build();

    public static final NullableStringProperty DETECT_DOCKER_IMAGE_ID =
        NullableStringProperty.newBuilder("detect.docker.image.id")
            .setInfo("Docker Image ID", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp(
                "The ID (shown in the 'IMAGE ID' column of 'docker images' output) of the target Docker image. The target image must already be local (must appear in the output of 'docker images').",
                "detect.docker.image, detect.docker.tar, and detect.docker.image.id are three alternative ways to specify an image (you should only set one of these properties)."
            )
            .setExample("0d120b6ccaa8")
            .setGroups(DetectGroup.DOCKER, DetectGroup.SOURCE_PATH)
            .setExample("fe1cc5b91830")
            .build();

    public static final NullablePathProperty DETECT_DOCKER_INSPECTOR_PATH =
        NullablePathProperty.newBuilder("detect.docker.inspector.path")
            .setInfo("Docker Inspector .jar File Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "This is used to override using the hosted Docker Inspector .jar file by binary repository url. You can use a compatible (the same major version that Detect downloads by default) local Docker Inspector .jar file at this path."
            )
            .setGroups(DetectGroup.DOCKER, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_DOCKER_INSPECTOR_VERSION =
        NullableStringProperty.newBuilder("detect.docker.inspector.version")
            .setInfo("Docker Inspector Version", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Version of the Docker Inspector to use. By default Detect will attempt to automatically determine the version to use.")
            .setGroups(DetectGroup.DOCKER, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setExample("9.1.1")
            .build();

    // TODO: Remove in 8.0.0. This is never really used, could have been a bad sign if it wasn't there.
    public static final NullablePathProperty DETECT_DOCKER_PATH =
        NullablePathProperty.newBuilder("detect.docker.path")
            .setInfo("Docker Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Path to the docker executable (used to load image inspector Docker images in order to run the Docker Inspector in air gap mode).")
            .setExample("/usr/local/bin/docker")
            .setGroups(DetectGroup.DOCKER, DetectGroup.GLOBAL)
            .build();

    // TODO: Remove in 8.0.0. Maybe get rid of this? Docker Inspector doesn't require Docker executable
    public static final BooleanProperty DETECT_DOCKER_PATH_REQUIRED =
        BooleanProperty.newBuilder("detect.docker.path.required", false)
            .setInfo("Run Without Docker in Path", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp("If set to true, Detect will attempt to run the Docker Inspector only if it finds a docker client executable.")
            .setGroups(DetectGroup.DOCKER, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_DOCKER_PLATFORM_TOP_LAYER_ID =
        NullableStringProperty.newBuilder("detect.docker.platform.top.layer.id")
            .setInfo("Platform Top Layer ID", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp(
                "To exclude components from platform layers from the results, assign to this property the ID of the top layer of the platform image. Get the platform top layer ID from the output of 'docker inspect platformimage:tag'. The platform top layer ID is the last item in RootFS.Layers. For more information, see 'Isolating application components' in the Docker Inspector documentation.",
                "If you are interested in components from the application layers of your image, but not interested in components from the underlying platform layers, you can exclude components from platform layers from the results by using this property to specify the boundary between platform layers and application layers. "
            )
            .setGroups(DetectGroup.DOCKER, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setExample("sha256:f6253634dc78da2f2e3bee9c8063593f880dc35d701307f30f65553e0f50c18c")
            .build();

    public static final NullableStringProperty DETECT_DOCKER_TAR =
        NullableStringProperty.newBuilder("detect.docker.tar")
            .setInfo("Image Archive File", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "An image .tar file which is either a Docker image saved to a file using the 'docker save' command, or an Open Container Initiative (OCI) image .tar file. The file must be readable by all.",
                "detect.docker.image, detect.docker.tar, and detect.docker.image.id are three alternative ways to specify an image (you should only set one of these properties)."
            )
            .setExample("./ubuntu21_04.tar")
            .setGroups(DetectGroup.DOCKER, DetectGroup.SOURCE_PATH)
            .build();

    // TODO: Should go away with new nuget inspector
    public static final NullablePathProperty DETECT_DOTNET_PATH =
        NullablePathProperty.newBuilder("detect.dotnet.path")
            .setInfo("dotnet Executable", DetectPropertyFromVersion.VERSION_4_4_0)
            .setHelp("The path to the dotnet executable.")
            .setGroups(DetectGroup.NUGET, DetectGroup.GLOBAL)
            .build();

    // TODO: Excluding ALL detector types doesn't make sense. Exclude the Detector tool instead. Change to NoneEnumListProperty, change default to NONE
    public static final AllNoneEnumListProperty<DetectorType> DETECT_EXCLUDED_DETECTOR_TYPES =
        AllNoneEnumListProperty.newBuilder("detect.excluded.detector.types", emptyList(), DetectorType.class)
            .setInfo("Detector Types Excluded", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "By default, all detectors will be included. If you want to exclude specific detectors, specify the ones to exclude here. If you want to exclude all detectors, specify \"ALL\". Exclusion rules always win.",
                "If Detect runs one or more detector on your project that you would like to exclude, you can use this property to prevent Detect from running them."
            )
            .setGroups(DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .setExample("NPM,LERNA")
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final BooleanProperty DETECT_FORCE_SUCCESS =
        BooleanProperty.newBuilder("detect.force.success", false)
            .setInfo("Force Success", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("If true, Detect will always exit with code 0.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Change default to false in 8.0.0
    public static final BooleanProperty DETECT_FORCE_SUCCESS_ON_SKIP =
        BooleanProperty.newBuilder("detect.force.success.on.skip", true)
            .setInfo("Force Success On Skip", DetectPropertyFromVersion.VERSION_7_12_1)
            .setHelp(
                "If true, Detect will always exit with code 0 when a scan of any type is skipped. Typically this happens when the Black Duck minimum scan interval timer has not been met.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullablePathProperty DETECT_GIT_PATH =
        NullablePathProperty.newBuilder("detect.git.path")
            .setInfo("Git Executable", DetectPropertyFromVersion.VERSION_5_5_0)
            .setHelp("Path of the git executable")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_GO_PATH =
        NullablePathProperty.newBuilder("detect.go.path")
            .setInfo("Go Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Path to the Go executable.")
            .setGroups(DetectGroup.GO, DetectGroup.GLOBAL)
            .build();

    public static final EnumProperty<GoModDependencyType> DETECT_GO_MOD_DEPENDENCY_TYPES_EXCLUDED =
        EnumProperty.newBuilder("detect.go.mod.dependency.types.excluded", GoModDependencyType.NONE, GoModDependencyType.class)
            .setInfo("Go Mod Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(
                createDefaultDrivenPropertyHelpText("Go Mod dependency types", "detect.go.mod.enable.verification"),
                String.format(
                    "If %s is provided, Detect will use the results of 'go mod why' to filter out unused dependencies from Go modules declaring Go 1.16 or higher. If %s is provided, Detect will use the results of 'go mod why -vendor' to filter out all unused dependencies.",
                    GoModDependencyType.UNUSED.name(),
                    GoModDependencyType.VENDORED.name()
                )
            )
            .setExample(GoModDependencyType.VENDORED.name())
            .setGroups(DetectGroup.GO, DetectGroup.GLOBAL)
            .build();

    public static final NullableStringProperty DETECT_GRADLE_BUILD_COMMAND =
        NullableStringProperty.newBuilder("detect.gradle.build.command")
            .setInfo("Gradle Build Command", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "Gradle command line arguments to add to the gradle/gradlew command line.",
                "By default, Detect runs the gradle (or gradlew) command with one task: dependencies. You can use this property to insert one or more additional gradle command line arguments (options or tasks) before the dependencies argument."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Inconsistent location of excluded/included within the property names. included/excluded should likely be at the end. Warn support!
    public static final CaseSensitiveStringListProperty DETECT_GRADLE_EXCLUDED_CONFIGURATIONS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.excluded.configurations", Collections.emptyList())
            .setInfo("Gradle Exclude Configurations", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Gradle configurations to exclude.",
                "As Detect examines the Gradle project for dependencies, Detect will skip any Gradle configurations specified via this property. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final CaseSensitiveStringListProperty DETECT_GRADLE_EXCLUDED_PROJECTS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.excluded.projects")
            .setInfo("Gradle Exclude Projects", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Gradle sub-projects to exclude.",
                "As Detect examines the Gradle project for dependencies, Detect will skip any Gradle sub-projects specified via this property. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final CaseSensitiveStringListProperty DETECT_GRADLE_EXCLUDED_PROJECT_PATHS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.excluded.project.paths")
            .setInfo("Gradle Exclude Project Paths", DetectPropertyFromVersion.VERSION_7_12_0)
            .setHelp(
                "A comma-separated list of Gradle sub-project paths to exclude.",
                "As Detect examines the Gradle project for dependencies, Detect will skip any Gradle sub-project whose path matches this property. Gradle project paths usually take the form ':parent:child' and are unique. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final CaseSensitiveStringListProperty DETECT_GRADLE_INCLUDED_CONFIGURATIONS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.included.configurations")
            .setInfo("Gradle Include Configurations", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Gradle configurations to include.",
                "As Detect examines the Gradle project for dependencies, if this property is set, Detect will include only those Gradle configurations specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NoneEnumListProperty<GradleConfigurationType> DETECT_GRADLE_CONFIGURATION_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.gradle.configuration.types.excluded", NoneEnum.NONE, GradleConfigurationType.class)
            .setInfo("Gradle Configuration Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(
                createDefaultDrivenPropertyHelpText("Gradle configuration types", "detect.gradle.include.unresolved.configurations"),
                "Including dependencies from unresolved Gradle configurations could lead to false positives. Dependency versions from an unresolved configuration may differ from a resolved one. See https://docs.gradle.org/7.2/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs"
            )
            .setExample(GradleConfigurationType.UNRESOLVED.name())
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .build();

    public static final CaseSensitiveStringListProperty DETECT_GRADLE_INCLUDED_PROJECTS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.included.projects")
            .setInfo("Gradle Include Projects", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Gradle sub-projects to include.",
                "As Detect examines the Gradle project for dependencies, if this property is set, Detect will include only those sub-projects specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final CaseSensitiveStringListProperty DETECT_GRADLE_INCLUDED_PROJECT_PATHS =
        CaseSensitiveStringListProperty.newBuilder("detect.gradle.included.project.paths")
            .setInfo("Gradle Include Project Paths", DetectPropertyFromVersion.VERSION_7_12_0)
            .setHelp(
                "A comma-separated list of Gradle sub-project paths to include.",
                "As Detect examines the Gradle project for dependencies, if this property is set, Detect will include only those sub-projects whose path matches this property. Gradle project paths usually take the form ':parent:child' and are unique. Leaving this unset implies 'include all'. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullablePathProperty DETECT_GRADLE_PATH =
        NullablePathProperty.newBuilder("detect.gradle.path")
            .setInfo("Gradle Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the Gradle executable (gradle or gradlew).", "If set, Detect will use the given Gradle executable instead of searching for one.")
            .setGroups(DetectGroup.GRADLE, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_HEX_REBAR3_PATH =
        NullablePathProperty.newBuilder("detect.hex.rebar3.path")
            .setInfo("Rebar3 Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the rebar3 executable.")
            .setGroups(DetectGroup.HEX, DetectGroup.GLOBAL)
            .build();

    // TODO: Change to directories.excluded
    public static final StringListProperty DETECT_EXCLUDED_DIRECTORIES =
        StringListProperty.newBuilder("detect.excluded.directories", emptyList())
            .setInfo("Detect Excluded Directories", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A comma-separated list of names, name patterns, relative paths, or path patterns of directories that Detect should exclude.",
                "Subdirectories whose name or path is resolved from the patterns in this list will not be searched when determining which detectors to run, and will be excluded from signature scan using the Scan CLI '--exclude' flag. Refer to the <i>Downloading and Running Synopsys Detect</i> > <i>Including and Excluding Tools, Detectors, Directories, etc.</i> page for more details."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.DETECTOR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setExample("**/*-test")
            .build();

    // TODO: Horribly named detect.directories.excluded.defaults=false
    public static final BooleanProperty DETECT_EXCLUDED_DIRECTORIES_DEFAULTS_DISABLED =
        BooleanProperty.newBuilder("detect.excluded.directories.defaults.disabled", false)
            .setInfo("Detect Excluded Directories Defaults Disabled", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "If false, Detect will exclude the default directory names. See the detailed help for more information.",
                "If false, the following directories will be excluded by Detect when searching for detectors: bin, build, .git, .gradle, node_modules, out, packages, target, .synopsys, and the following directories will be excluded from signature scan using the Scan CLI '--exclude' flag: .git, .gradle, node_modules, .synopsys."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.DETECTOR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Rename to included signature scan? @Alex
    public static final IntegerProperty DETECT_EXCLUDED_DIRECTORIES_SEARCH_DEPTH =
        IntegerProperty.newBuilder("detect.excluded.directories.search.depth", 4)
            .setInfo("Detect Excluded Directories Search Depth", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp("Enables you to adjust the depth to which Detect will search when creating signature scanner exclusion patterns.")
            .setGroups(DetectGroup.SIGNATURE_SCANNER, DetectGroup.SOURCE_SCAN)
            .build();

    public static final BooleanProperty DETECT_IMPACT_ANALYSIS_ENABLED =
        BooleanProperty.newBuilder("detect.impact.analysis.enabled", false)
            .setInfo("Vulnerability Impact Analysis Enabled", DetectPropertyFromVersion.VERSION_6_5_0)
            .setHelp(
                "If set to true, Detect will attempt to look for *.class files and generate a Vulnerability Impact Analysis Report for upload to Black Duck.")
            .setGroups(DetectGroup.IMPACT_ANALYSIS, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_IMPACT_ANALYSIS_OUTPUT_PATH =
        NullablePathProperty.newBuilder("detect.impact.analysis.output.path")
            .setInfo("Impact Analysis Output Directory", DetectPropertyFromVersion.VERSION_6_5_0)
            .setHelp(
                "The path to the output directory for Impact Analysis reports.",
                "If not set, the Impact Analysis reports are placed in a 'impact-analysis' subdirectory of the output directory."
            )
            .setGroups(DetectGroup.IMPACT_ANALYSIS, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: None doesn't make sense. Change default to ALL
    public static final AllNoneEnumListProperty<DetectorType> DETECT_INCLUDED_DETECTOR_TYPES =
        AllNoneEnumListProperty.newBuilder("detect.included.detector.types", emptyList(), DetectorType.class)
            .setInfo("Detector Types Included", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "By default, all tools will be included. If you want to include only specific tools, specify the ones to include here. Exclusion rules always win.",
                "If you want to limit Detect to a subset of its detectors, use this property to specify that subset."
            )
            .setExample("NPM")
            .setGroups(DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullablePathProperty DETECT_JAVA_PATH =
        NullablePathProperty.newBuilder("detect.java.path")
            .setInfo("Java Executable", DetectPropertyFromVersion.VERSION_5_0_0)
            .setHelp("Path to the java executable.", "If set, Detect will use the given java executable instead of searching for one.")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    // TODO: Rename detect.lerna.package.names.excluded
    public static final CaseSensitiveStringListProperty DETECT_LERNA_EXCLUDED_PACKAGES =
        CaseSensitiveStringListProperty.newBuilder("detect.lerna.excluded.packages")
            .setInfo("Lerna Packages Excluded", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A comma-separated list of Lerna packages to exclude.",
                "As Detect parses the output of lerna ls --all --json, Detect will exclude any Lerna packages specified via this property. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.LERNA, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Rename detect.lerna.package.names.included
    public static final CaseSensitiveStringListProperty DETECT_LERNA_INCLUDED_PACKAGES =
        CaseSensitiveStringListProperty.newBuilder("detect.lerna.included.packages")
            .setInfo("Lerna Packages Included", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A comma-separated list of Lerna packages to include.",
                "As Detect parses the output of lerna ls --all --json2, if this property is set, Detect will include only those Lerna packages specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.LERNA, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullablePathProperty DETECT_LERNA_PATH =
        NullablePathProperty.newBuilder("detect.lerna.path")
            .setInfo("Lerna Executable", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp("Path of the lerna executable.")
            .setGroups(DetectGroup.LERNA, DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NoneEnumListProperty<LernaPackageType> DETECT_LERNA_PACKAGE_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.lerna.package.types.excluded", NoneEnum.NONE, LernaPackageType.class)
            .setInfo("Lerna Package Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Lerna package types", "detect.lerna.include.private"))
            .setExample(LernaPackageType.PRIVATE.name())
            .setGroups(DetectGroup.LERNA, DetectGroup.GLOBAL)
            .build();

    // TODO: Rename to detect.maven.additional.arguments
    public static final NullableStringProperty DETECT_MAVEN_BUILD_COMMAND =
        NullableStringProperty.newBuilder("detect.maven.build.command")
            .setInfo("Maven Build Command", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "Maven command line arguments to add to the mvn/mvnw command line.",
                "By default, Detect runs the mvn (or mvnw) command with one argument: dependency:tree. You can use this property to insert one or more additional mvn command line arguments (goals, etc.) before the dependency:tree argument. For example: suppose you are running in bash on Linux, and want to point maven to your settings file (maven_dev_settings.xml in your home directory) and assign the value 'other' to property 'reason'. You could do this with: --detect.maven.build.command='--settings \\${HOME}/maven_dev_settings.xml --define reason=other'"
            )
            .setGroups(DetectGroup.MAVEN, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Rename to detect.maven.modules.excluded
    public static final CaseSensitiveStringListProperty DETECT_MAVEN_EXCLUDED_MODULES =
        CaseSensitiveStringListProperty.newBuilder("detect.maven.excluded.modules")
            .setInfo("Maven Modules Excluded", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Maven modules (sub-projects) to exclude.",
                "As Detect parses the mvn dependency:tree output for dependencies, Detect will skip any Maven modules specified via this property. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.MAVEN, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Rename to detect.maven.modules.included
    public static final CaseSensitiveStringListProperty DETECT_MAVEN_INCLUDED_MODULES =
        CaseSensitiveStringListProperty.newBuilder("detect.maven.included.modules")
            .setInfo("Maven Modules Included", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of Maven modules (sub-projects) to include.",
                "As Detect parses the mvn dependency:tree output for dependencies, if this property is set, Detect will include only those Maven modules specified via this property that are not excluded. Leaving this unset implies 'include all'. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.MAVEN, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: JP LongShot: rename all Path properties to detect.something.executable.path
    public static final NullablePathProperty DETECT_MAVEN_PATH =
        NullablePathProperty.newBuilder("detect.maven.path")
            .setInfo("Maven Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the Maven executable (mvn or mvnw).", "If set, Detect will use the given Maven executable instead of searching for one.")
            .setGroups(DetectGroup.MAVEN, DetectGroup.GLOBAL)
            .build();

    // TODO: Rename detect.maven.scopes.included
    public static final CaseSensitiveStringListProperty DETECT_MAVEN_INCLUDED_SCOPES =
        CaseSensitiveStringListProperty.newBuilder("detect.maven.included.scopes")
            .setInfo("Dependency Scope Included", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "A comma separated list of Maven scopes. Output will be limited to dependencies within these scopes (overridden by exclude).",
                "If set, Detect will include only dependencies of the given Maven scope. This property accepts filename globbing-style wildcards. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.MAVEN, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Rename detect.maven.scopes.excluded
    public static final CaseSensitiveStringListProperty DETECT_MAVEN_EXCLUDED_SCOPES =
        CaseSensitiveStringListProperty.newBuilder("detect.maven.excluded.scopes")
            .setInfo("Dependency Scope Excluded", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "A comma separated list of Maven scopes. Output will be limited to dependencies outside these scopes (overrides include).",
                "If set, Detect will include only dependencies outside of the given Maven scope. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.MAVEN, DetectGroup.SOURCE_SCAN)
            .build();

    public static final BooleanProperty DETECT_NOTICES_REPORT =
        BooleanProperty.newBuilder("detect.notices.report", false)
            .setInfo("Generate Notices Report", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("When set to true, a Black Duck notices report in text form will be created in your source directory.")
            .setGroups(DetectGroup.REPORT, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_NOTICES_REPORT_PATH =
        NullablePathProperty.newBuilder("detect.notices.report.path")
            .setInfo("Notices Report Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The output directory for notices report. Default is the source directory.")
            .setGroups(DetectGroup.REPORT, DetectGroup.GLOBAL, DetectGroup.REPORT_SETTING)
            .build();

    // TODO: Rename to detect.npm.additional.arguments
    public static final NullableStringProperty DETECT_NPM_ARGUMENTS =
        NullableStringProperty.newBuilder("detect.npm.arguments")
            .setInfo("Additional NPM Command Arguments", DetectPropertyFromVersion.VERSION_4_3_0)
            .setHelp(
                "A space-separated list of additional arguments that Detect will add at then end of the npm ls command line when Detect executes the NPM CLI Detector on an NPM project.")
            .setExample("--depth=0")
            .setGroups(DetectGroup.NPM, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NoneEnumListProperty<NpmDependencyType> DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.npm.dependency.types.excluded", NoneEnum.NONE, NpmDependencyType.class)
            .setInfo("Npm Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Npm dependency types", "detect.npm.include.dev.dependencies", "detect.npm.include.peer.dependencies"))
            .setExample(String.format("%s,%s", NpmDependencyType.DEV.name(), NpmDependencyType.PEER.name()))
            .setGroups(DetectGroup.NPM, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_NPM_PATH =
        NullablePathProperty.newBuilder("detect.npm.path")
            .setInfo("NPM Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the Npm executable.")
            .setGroups(DetectGroup.NPM, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_NUGET_CONFIG_PATH =
        NullablePathProperty.newBuilder("detect.nuget.config.path")
            .setInfo("Nuget Config File", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp("The path to the Nuget.Config file to supply to the nuget exe.")
            .setGroups(DetectGroup.NUGET, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Rename to detect.nuget.modules.excluded
    public static final CaseSensitiveStringListProperty DETECT_NUGET_EXCLUDED_MODULES =
        CaseSensitiveStringListProperty.newBuilder("detect.nuget.excluded.modules")
            .setInfo("Nuget Projects Excluded", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The projects within the solution to exclude. Detect will exclude all projects with names that include any of the given regex patterns. To match a full project name (for example: 'BaGet.Core'), use a regular expression that matches only the full name ('^BaGet.Core$')")
            .setExample("^BaGet.Core$,^BaGet.Core.Tests$")
            .setGroups(DetectGroup.NUGET, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Deprecate
    public static final BooleanProperty DETECT_NUGET_IGNORE_FAILURE =
        BooleanProperty.newBuilder("detect.nuget.ignore.failure", false)
            .setInfo("Ignore Nuget Failures", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("If true errors will be logged and then ignored.")
            .setGroups(DetectGroup.NUGET, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Rename to detect.nuget.modules.included
    public static final CaseSensitiveStringListProperty DETECT_NUGET_INCLUDED_MODULES =
        CaseSensitiveStringListProperty.newBuilder("detect.nuget.included.modules")
            .setInfo("Nuget Modules Included", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The names of the projects in a solution to include (overrides exclude). Detect will include all projects with names that include any of the given regex patterns. To match a full project name (for example: 'BaGet.Core'), use a regular expression that matches only the full name ('^BaGet.Core$')")
            .setExample("^BaGet.Core$,^BaGet.Core.Tests$")
            .setGroups(DetectGroup.NUGET, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Deprecate
    public static final NullableStringProperty DETECT_NUGET_INSPECTOR_VERSION =
        NullableStringProperty.newBuilder("detect.nuget.inspector.version")
            .setInfo("Nuget Inspector Version", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Version of the Nuget Inspector. By default Detect will run the latest version that is compatible with the Detect version.")
            .setGroups(DetectGroup.NUGET, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Deprecate
    public static final StringListProperty DETECT_NUGET_PACKAGES_REPO_URL =
        StringListProperty.newBuilder("detect.nuget.packages.repo.url", singletonList("https://api.nuget.org/v3/index.json"))
            .setInfo("Nuget Packages Repository URL", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The source for nuget packages",
                "Set this to \"https://www.nuget.org/api/v2/\" if your are still using a nuget client expecting the v2 api."
            )
            .setGroups(DetectGroup.NUGET, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_OUTPUT_PATH =
        NullablePathProperty.newBuilder("detect.output.path")
            .setInfo("Detect Output Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The path to the output directory.",
                "If set, Detect will use the given directory to store files that it downloads and creates, instead of using the default location (~/blackduck)."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_TOOLS_OUTPUT_PATH =
        NullablePathProperty.newBuilder("detect.tools.output.path")
            .setInfo("Detect Tools Output Path", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp(
                "The path to the tools directory where detect should download and/or access things like the Signature Scanner that it shares over multiple runs.",
                "If set, Detect will use the given directory instead of using the default location of output path plus tools."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NoneEnumListProperty<PackagistDependencyType> DETECT_PACKAGIST_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.packagist.dependency.types.excluded", NoneEnum.NONE, PackagistDependencyType.class)
            .setInfo("Packagist Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Packagist dependency types", "detect.packagist.include.dev.dependencies"))
            .setGroups(DetectGroup.PACKAGIST, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NoneEnumListProperty<PearDependencyType> DETECT_PEAR_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.pear.dependency.types.excluded", NoneEnum.NONE, PearDependencyType.class)
            .setInfo("Pear Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Pear dependency types", "detect.pear.only.required.deps"))
            .setExample(PearDependencyType.OPTIONAL.name())
            .setGroups(DetectGroup.PEAR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_PEAR_PATH =
        NullablePathProperty.newBuilder("detect.pear.path")
            .setInfo("Pear Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the pear executable.")
            .setGroups(DetectGroup.PEAR, DetectGroup.GLOBAL)
            .build();

    public static final NullableStringProperty DETECT_PIP_PROJECT_NAME =
        NullableStringProperty.newBuilder("detect.pip.project.name")
            .setInfo("PIP Project Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The name of your PIP project, to be used if your project's name cannot be correctly inferred from its setup.py file.")
            .setGroups(DetectGroup.PIP, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullableStringProperty DETECT_PIP_PROJECT_VERSION_NAME =
        NullableStringProperty.newBuilder("detect.pip.project.version.name")
            .setInfo("PIP Project Version Name", DetectPropertyFromVersion.VERSION_4_1_0)
            .setHelp("The version of your PIP project, to be used if your project's version name cannot be correctly inferred from its setup.py file.")
            .setGroups(DetectGroup.PIP, DetectGroup.SOURCE_SCAN)
            .build();

    public static final PathListProperty DETECT_PIP_REQUIREMENTS_PATH =
        PathListProperty.newBuilder("detect.pip.requirements.path", emptyList())
            .setInfo("PIP Requirements Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of paths to requirements files, to be used to analyze requirements files with a filename other than requirements.txt or to specify which requirements files should be analyzed.",
                "This property should only be set if you want the PIP Inspector Detector to run. For example: If your project uses Pipenv, do not set this property."
            )
            .setGroups(DetectGroup.PIP, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Why is this even an option? I want to NUKE this, but in case we can't please replace it: detect.pipenv.dependency.types.included=ALL/DIRECT
    public static final BooleanProperty DETECT_PIP_ONLY_PROJECT_TREE =
        BooleanProperty.newBuilder("detect.pip.only.project.tree", false)
            .setInfo("PIP Include Only Project Tree", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp(
                "By default, pipenv includes all dependencies found in the graph. Set to true to only include dependencies found underneath the dependency that matches the provided pip project and version name.")
            .setGroups(DetectGroup.PIP, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_PIP_PATH =
        NullablePathProperty.newBuilder("detect.pip.path")
            .setInfo("Pip Executable", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp("The path to the Pip executable.")
            .setGroups(DetectGroup.PIP, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_PIPENV_PATH =
        NullablePathProperty.newBuilder("detect.pipenv.path")
            .setInfo("Pipenv Executable", DetectPropertyFromVersion.VERSION_4_1_0)
            .setHelp("The path to the Pipenv executable.")
            .setGroups(DetectGroup.PIP, DetectGroup.GLOBAL)
            .build();

    public static final NoneEnumListProperty<PnpmDependencyTypeV2> DETECT_PNPM_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.pnpm.dependency.types.excluded", NoneEnum.NONE, PnpmDependencyTypeV2.class)
            .setInfo("pnpm Dependency Types", DetectPropertyFromVersion.VERSION_7_11_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Pnpm dependency type", "detect.pnpm.dependency.types"))
            .setGroups(DetectGroup.PNPM, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_SWIFT_PATH =
        NullablePathProperty.newBuilder("detect.swift.path")
            .setInfo("Swift Executable", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp("Path of the swift executable.")
            .setGroups(DetectGroup.SWIFT, DetectGroup.GLOBAL)
            .build();

    public static final AllNoneEnumListProperty<PolicyRuleSeverityType> DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES =
        AllNoneEnumListProperty.newBuilder("detect.policy.check.fail.on.severities", AllNoneEnum.NONE, PolicyRuleSeverityType.class)
            .setInfo("Fail on Policy Violation Severities", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "A comma-separated list of policy violation severities that will fail Detect. If this is set to NONE, Detect will not fail due to policy violations. A value of ALL is equivalent to all of the other possible values except NONE.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.GLOBAL, DetectGroup.PROJECT_SETTING, DetectGroup.POLICY)
            .build();

    public static final StringListProperty DETECT_POLICY_CHECK_FAIL_ON_NAMES =
        StringListProperty.newBuilder("detect.policy.check.fail.on.names", Collections.emptyList())
            .setInfo("Fail on Policy Names with Violations", DetectPropertyFromVersion.VERSION_7_12_0)
            .setHelp(
                "A comma-separated list of policy names with a non-zero number of violations that will fail Detect.",
                "If left unset, Detect will not fail due to violated policies of a certain name. This property does not change the behavior of detect.policy.check.fail.on.severities."
            )
            .setGroups(DetectGroup.PROJECT, DetectGroup.GLOBAL, DetectGroup.PROJECT_SETTING, DetectGroup.POLICY)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_APPLICATION_ID =
        NullableStringProperty.newBuilder("detect.project.application.id")
            .setInfo("Application ID", DetectPropertyFromVersion.VERSION_5_2_0)
            .setHelp("Sets the 'Application ID' project setting.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_GROUP_NAME =
        NullableStringProperty.newBuilder("detect.project.group.name")
            .setInfo("Project Group Name", DetectPropertyFromVersion.VERSION_7_8_0)
            .setHelp("Sets the 'Project Group' to assign the project to. Must match exactly to an existing project group on Black Duck.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    // Dynamic property
    public static final NullableStringProperty DETECT_CUSTOM_FIELDS_PROJECT =
        NullableStringProperty.newBuilder("detect.custom.fields.project")
            .setInfo("Custom Fields", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp(
                "A  list of custom fields with a label and comma-separated value starting from index 0. For each index, provide one label and one value. For example, to set a custom field with label 'example' to 'one,two': `detect.custom.fields.project[0].label='example'` and `detect.custom.fields.project[0].value='one,two'`. To set another field, use index 1. Note that these will not show up in the detect configuration log.",
                "When assigning a value that contains a comma to a single-value field such as a text field, append '[0]' to the end of the value property name. For example, to set the value of the first field you are setting ('detect.custom.fields.version[0]') to 'text1,text2', use 'detect.custom.fields.version[0].value[0]=text1,text2'."
            )
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    // Dynamic property
    public static final NullableStringProperty DETECT_CUSTOM_FIELDS_VERSION =
        NullableStringProperty.newBuilder("detect.custom.fields.version")
            .setInfo("Custom Fields", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp(
                "A  list of custom fields with a label and comma-separated value starting from index 0. For each index, provide one label and one value. For example , to set a custom field with label 'example' to 'one,two': `detect.custom.fields.version[0].label='example'` and `detect.custom.fields.version[0].value='one,two'`. To set another field, use index 1. Note that these will not show up in the detect configuration log.",
                "When assigning a value that contains a comma to a single-value field such as a text field, append '[0]' to the end of the value property name. For example, to set the value of the first field you are setting ('detect.custom.fields.version[0]') to 'text1,text2', use 'detect.custom.fields.version[0].value[0]=text1,text2'."
            )
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    //TODO- change default of this property to ALL in 8.0.0
    public static final AllNoneEnumListProperty<ProjectCloneCategoriesType> DETECT_PROJECT_CLONE_CATEGORIES =
        AllNoneEnumListProperty.newBuilder(
                "detect.project.clone.categories",
                Arrays.asList(
                    ProjectCloneCategoriesType.COMPONENT_DATA,
                    ProjectCloneCategoriesType.VULN_DATA,
                    ProjectCloneCategoriesType.VERSION_SETTINGS,
                    ProjectCloneCategoriesType.CUSTOM_FIELD_DATA,
                    ProjectCloneCategoriesType.LICENSE_TERM_FULFILLMENT
                ), ProjectCloneCategoriesType.class
            )
            .setInfo("Clone Project Categories", DetectPropertyFromVersion.VERSION_4_2_0)
            .setHelp(
                "The value of this property is used to set the 'Cloning' settings on created Black Duck projects. If property detect.project.version.update is set to true, the value of this property is used to set the 'Cloning' settings on updated Black Duck projects.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_CODELOCATION_PREFIX =
        NullableStringProperty.newBuilder("detect.project.codelocation.prefix")
            .setInfo("Scan Name Prefix", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("A prefix to the name of the scans created by Detect. Useful for running against the same projects on multiple machines.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_CODELOCATION_SUFFIX =
        NullableStringProperty.newBuilder("detect.project.codelocation.suffix")
            .setInfo("Scan Name Suffix", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("A suffix to the name of the scans created by Detect.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Should mention downstream affects. Could be made smarter with ticket IDETECT-3121
    public static final BooleanProperty DETECT_PROJECT_CODELOCATION_UNMAP =
        BooleanProperty.newBuilder("detect.project.codelocation.unmap", false)
            .setInfo("Unmap All Other Scans for Project", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp("If set to true, unmaps all other scans mapped to the project version produced by the current run of Detect.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_DESCRIPTION =
        NullableStringProperty.newBuilder("detect.project.description")
            .setInfo("Project Description", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp("If project description is specified, your project will be created with this description.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final StringListProperty DETECT_PROJECT_USER_GROUPS =
        StringListProperty.newBuilder("detect.project.user.groups", emptyList())
            .setInfo("Project User Groups", DetectPropertyFromVersion.VERSION_5_4_0)
            .setHelp("A comma-separated list of names of user groups to add to the project.")
            .setExample("ProjectManagers,TechLeads")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final StringListProperty DETECT_PROJECT_TAGS =
        StringListProperty.newBuilder("detect.project.tags", emptyList())
            .setInfo("Project Tags", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp("A comma-separated list of tags to add to the project. This property is not supported when using Synopsys Detect in offline mode.")
            .setExample("Critical")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Make an enum DetectorType
    public static final NullableStringProperty DETECT_PROJECT_DETECTOR =
        NullableStringProperty.newBuilder("detect.project.detector")
            .setInfo("Project Name and Version Detector", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp(
                "The detector that will be used to determine the project name and version when multiple detector types apply. This property should be used with detect.project.tool.",
                "If Detect finds that multiple detectors apply, this property can be used to select the detector that will provide the project name and version. When using this property, you should also set detect.project.tool=DETECTOR"
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final BooleanProperty DETECT_PROJECT_LEVEL_ADJUSTMENTS =
        BooleanProperty.newBuilder("detect.project.level.adjustments", true)
            .setInfo("Allow Project Level Adjustments", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "Sets the component adjustments setting on the Black Duck project.",
                "Corresponds to the 'Always maintain component adjustments to all versions of this project' checkbox under 'Component Adjustments' on the Black Duck Project settings page."
            )
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_NAME =
        NullableStringProperty.newBuilder("detect.project.name")
            .setInfo("Project Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "An override for the name to use for the Black Duck project. If not supplied, Detect will attempt to use the tools to figure out a reasonable project name. If that fails, the final part of the directory path where the inspection is taking place will be used.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final NullableStringProperty DETECT_PARENT_PROJECT_NAME =
        NullableStringProperty.newBuilder("detect.parent.project.name")
            .setInfo("Parent Project Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "When a parent project and version name are specified, the created detect project will be added as a component to the specified parent project version. The specified parent project and parent project version must exist on Black Duck.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PARENT_PROJECT_VERSION_NAME =
        NullableStringProperty.newBuilder("detect.parent.project.version.name")
            .setInfo("Parent Project Version Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "When a parent project and version name are specified, the created detect project will be added as a component to the specified parent project version. The specified parent project and parent project version must exist on Black Duck.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableIntegerProperty DETECT_PROJECT_TIER =
        NullableIntegerProperty.newBuilder("detect.project.tier")
            .setInfo("Project Tier", DetectPropertyFromVersion.VERSION_3_1_0)
            .setHelp("If a Black Duck project tier is specified, your project will be created with this tier.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final EnumListProperty<DetectTool> DETECT_PROJECT_TOOL =
        EnumListProperty.newBuilder("detect.project.tool", Arrays.asList(DetectTool.DOCKER, DetectTool.DETECTOR, DetectTool.BAZEL), DetectTool.class)
            .setInfo("Detector Tool Priority", DetectPropertyFromVersion.VERSION_5_0_0)
            .setHelp(
                "The tool priority for project name and version. The project name and version will be determined by the first tool in this list that provides them.",
                "This allows you to control which tool provides the project name and version when more than one tool are capable of providing it."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final EnumProperty<ProjectVersionDistributionType> DETECT_PROJECT_VERSION_DISTRIBUTION =
        EnumProperty.newBuilder("detect.project.version.distribution", ProjectVersionDistributionType.EXTERNAL, ProjectVersionDistributionType.class)
            .setInfo("Version Distribution", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("An override for the Project Version distribution")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_VERSION_NAME =
        NullableStringProperty.newBuilder("detect.project.version.name")
            .setInfo("Version Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "An override for the version to use for the Black Duck project. If not supplied, Detect will attempt to use the tools to figure out a reasonable version name. If that fails, the current date will be used.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_VERSION_NICKNAME =
        NullableStringProperty.newBuilder("detect.project.version.nickname")
            .setInfo("Version Nickname", DetectPropertyFromVersion.VERSION_5_2_0)
            .setHelp("If a project version nickname is specified, your project version will be created with this nickname.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_VERSION_NOTES =
        NullableStringProperty.newBuilder("detect.project.version.notes")
            .setInfo("Version Notes", DetectPropertyFromVersion.VERSION_3_1_0)
            .setHelp("If project version notes are specified, your project version will be created with these notes.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final EnumProperty<ProjectVersionPhaseType> DETECT_PROJECT_VERSION_PHASE =
        EnumProperty.newBuilder("detect.project.version.phase", ProjectVersionPhaseType.DEVELOPMENT, ProjectVersionPhaseType.class)
            .setInfo("Version Phase", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("An override for the Project Version phase.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final BooleanProperty DETECT_PROJECT_VERSION_UPDATE =
        BooleanProperty.newBuilder("detect.project.version.update", false)
            .setInfo("Update Project Version", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp(
                "If set to true, will update the Project Version with the configured properties. See detailed help for more information.",
                "When set to true, the following properties will be updated on the Project. Project tier (detect.project.tier) and Project Level Adjustments (detect.project.level.adjustments). "
                    + "The following properties will also be updated on the Version.Version notes (detect.project.version.notes), phase(detect.project.version.phase), distribution(detect.project.version.distribution)."
            )
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final NullableStringProperty DETECT_PROJECT_VERSION_LICENSE =
        NullableStringProperty.newBuilder("detect.project.version.license")
            .setInfo("Project Version License", DetectPropertyFromVersion.VERSION_7_11_0)
            .setHelp("An override for a Project Version's license.")
            .setExample("Apache License 2.0")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .build();

    public static final NullablePathProperty DETECT_PYTHON_PATH =
        NullablePathProperty.newBuilder("detect.python.path")
            .setInfo("Python Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The path to the Python executable.")
            .setGroups(DetectGroup.PYTHON, DetectGroup.GLOBAL)
            .build();

    // TODO: Rename to detect.detector.types.required
    public static final EnumListProperty<DetectorType> DETECT_REQUIRED_DETECTOR_TYPES =
        EnumListProperty.newBuilder("detect.required.detector.types", emptyList(), DetectorType.class)
            .setInfo("Required Detect Types", DetectPropertyFromVersion.VERSION_4_3_0)
            .setHelp(
                "The set of required detectors.",
                "If you want one or more detectors to be required (must be found to apply), use this property to specify the set of required detectors. If this property is set, and one (or more) of the given detectors is not found to apply, Detect will fail."
            )
            .setExample("NPM")
            .setGroups(DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .build();

    public static final BooleanProperty DETECT_RISK_REPORT_PDF =
        BooleanProperty.newBuilder("detect.risk.report.pdf", false)
            .setInfo("Generate Risk Report (PDF)", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("When set to true, a Black Duck risk report in PDF form will be created.")
            .setGroups(DetectGroup.REPORT, DetectGroup.GLOBAL, DetectGroup.REPORT_SETTING)
            .build();

    public static final NullablePathProperty DETECT_RISK_REPORT_PDF_PATH =
        NullablePathProperty.newBuilder("detect.risk.report.pdf.path")
            .setInfo("Risk Report Output Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("The output directory for risk report in PDF. Default is the source directory.")
            .setGroups(DetectGroup.REPORT, DetectGroup.GLOBAL)
            .build();

    public static final NoneEnumListProperty<GemspecDependencyType> DETECT_RUBY_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.ruby.dependency.types.excluded", NoneEnum.NONE, GemspecDependencyType.class)
            .setInfo("Ruby Dependency Types Excluded", DetectPropertyFromVersion.VERSION_7_10_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Ruby(Gempsec) dependency types", "detect.ruby.include.dev.dependencies", "detect.ruby.include.runtime.dependencies"))
            .setExample(String.format("%s,%s", GemspecDependencyType.DEV.name(), GemspecDependencyType.RUNTIME))
            .setGroups(DetectGroup.RUBY, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .build();

    public static final NullablePathProperty DETECT_SBT_PATH =
        NullablePathProperty.newBuilder("detect.sbt.path")
            .setInfo("Sbt Executable", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Path to the Sbt executable.", "If set, Detect will use the given Sbt executable instead of searching for one.")
            .setExample("C:\\Program Files (x86)\\sbt\\bin\\sbt.bat")
            .setGroups(DetectGroup.SBT, DetectGroup.GLOBAL)
            .build();

    // TODO: Rename to detect.sbt.additional.arguments
    public static final NullableStringProperty DETECT_SBT_ARGUMENTS =
        NullableStringProperty.newBuilder("detect.sbt.arguments")
            .setInfo("Additional sbt command Arguments", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A space-separated list of additional arguments to add to sbt command line when running Detect against an SBT project. Detect will execute the command 'sbt {additional arguments} {Detect-added arguments}'.")
            .setGroups(DetectGroup.SBT, DetectGroup.SOURCE_SCAN)
            .setExample("\"-Djline.terminal=jline.UnsupportedTerminal\"")
            .build();

    // TODO: Rename to detect.sbt.configurations.excluded
    public static final CaseSensitiveStringListProperty DETECT_SBT_EXCLUDED_CONFIGURATIONS =
        CaseSensitiveStringListProperty.newBuilder("detect.sbt.excluded.configurations")
            .setInfo("SBT Configurations Excluded", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The names of the sbt configurations to exclude.",
                "This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.SBT, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(SBT_REPORT_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    // TODO: Rename to detect.sbt.configurations.included
    public static final CaseSensitiveStringListProperty DETECT_SBT_INCLUDED_CONFIGURATIONS =
        CaseSensitiveStringListProperty.newBuilder("detect.sbt.included.configurations")
            .setInfo("SBT Configurations Included", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The names of the sbt configurations to include.",
                "This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.SBT, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(SBT_REPORT_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    public static final IntegerProperty DETECT_SBT_REPORT_DEPTH =
        IntegerProperty.newBuilder("detect.sbt.report.search.depth", 3)
            .setInfo("SBT Report Search Depth", DetectPropertyFromVersion.VERSION_4_3_0)
            .setHelp("Depth the sbt detector will use to search for report files.")
            .setGroups(DetectGroup.SBT, DetectGroup.SOURCE_SCAN)
            .setDeprecated(SBT_REPORT_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    // TODO: Rename to detect.signature.scan.output.path
    public static final NullablePathProperty DETECT_SCAN_OUTPUT_PATH =
        NullablePathProperty.newBuilder("detect.scan.output.path")
            .setInfo("Scan Output Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The output directory for all signature scanner output files. If not set, the signature scanner output files will be in a 'scan' subdirectory of the output directory.")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    public static final NullablePathProperty DETECT_SOURCE_PATH =
        NullablePathProperty.newBuilder("detect.source.path")
            .setInfo("Source Path", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The source path is the path to the project directory to inspect. If no value is provided, the source path defaults to the current working directory.",
                "Detect will search the source directory for hints that indicate which package manager(s) the project uses, and will attempt to run the corresponding detector(s). "
                    +
                    "The source path is also the default target for signature scanning. (This can be overridden with the detect.blackduck.signature.scanner.paths property.)"
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.SOURCE_PATH)
            .build();

    public static final EnumProperty<DetectTargetType> DETECT_TARGET_TYPE =
        EnumProperty.newBuilder("detect.target.type", DetectTargetType.SOURCE, DetectTargetType.class)
            .setInfo("Detect Target", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "Informs detect of what is being scanned which allows improved user experience when scanning different types of targets.",
                "Changes the behaviour of detect to better suite what is being scanned. For example, when IMAGE is selected and the DOCKER tool applies and has not been excluded, detect will not pick a source directory, will automatically disable the DETECTOR tool and run BINARY/SIGNATURE SCAN on the provided image."
            )
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Simple)
            .build();

    public static final BooleanProperty DETECT_TEST_CONNECTION =
        BooleanProperty.newBuilder("detect.test.connection", false)
            .setInfo("Test Connection to Black Duck", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Test the connection to Black Duck with the current configuration.")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.GLOBAL)
            .build();

    public static final LongProperty DETECT_TIMEOUT =
        LongProperty.newBuilder("detect.timeout", 300L)
            .setInfo("Detect Timeout", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp(
                "The amount of time in seconds Detect will wait for network connection, for scans to finish, and to generate reports (i.e. risk and policy check). When changing this value, keep in mind the checking of policies might have to wait for scans to process which can take some time.")
            .setExample("600")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .build();

    // TODO: Change to AllEnumListProperty, NONE doesn't make sense
    public static final AllNoneEnumListProperty<DetectTool> DETECT_TOOLS_EXCLUDED =
        AllNoneEnumListProperty.newBuilder("detect.tools.excluded", emptyList(), DetectTool.class)
            .setInfo("Detect Tools Excluded", DetectPropertyFromVersion.VERSION_5_0_0)
            .setHelp(
                "The tools Detect should not allow, in a comma-separated list. Excluded tools will not be run even if all criteria for the tool is met. Exclusion rules always win.",
                "This property and detect.tools provide control over which tools Detect runs. " +
                    "If neither detect.tools nor detect.tools.excluded are set, Detect will allow (run if applicable, based on the values of other properties) all Detect tools. If detect.tools is set, and detect.tools.excluded is not set, Detect will only allow to run those tools that are specified in the detect.tools list. If detect.tools.excluded is set, Detect will only allow those tools that are not specified in the detect.tools.excluded list."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build();

    // TODO: Change to NoneEnumListProperty, ALL doesn't make sense
    public static final AllNoneEnumListProperty<DetectTool> DETECT_TOOLS =
        AllNoneEnumListProperty.newBuilder("detect.tools", emptyList(), DetectTool.class)
            .setInfo("Detect Tools Included", DetectPropertyFromVersion.VERSION_5_0_0)
            .setHelp(
                "The tools Detect should allow in a comma-separated list. Tools in this list (as long as they are not also in the excluded list) will be allowed to run if all criteria of the tool are met. Exclusion rules always win.",
                "This property and detect.tools.excluded provide control over which tools Detect runs. " +
                    "If neither detect.tools nor detect.tools.excluded are set, Detect will allow (run if applicable, based on the values of other properties) all Detect tools. If detect.tools is set, and detect.tools.excluded is not set, Detect will only allow to run those tools that are specified in the detect.tools list. If detect.tools.excluded is set, Detect will only allow those tools that are not specified in the detect.tools.excluded list."
            )
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .build()
            .deprecateNone("The value NONE is deprecated. Instead, you should set " + DETECT_TOOLS_EXCLUDED.getKey() + " to ALL.");

    public static final NoneEnumListProperty<YarnDependencyType> DETECT_YARN_DEPENDENCY_TYPES_EXCLUDED =
        NoneEnumListProperty.newBuilder("detect.yarn.dependency.types.excluded", NoneEnum.NONE, YarnDependencyType.class)
            .setInfo("Yarn Dependency Types Excluded", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp(createDefaultDrivenPropertyHelpText("Yarn dependency types", "detect.yarn.prod.only"))
            .setExample(YarnDependencyType.NON_PRODUCTION.name())
            .setGroups(DetectGroup.YARN, DetectGroup.SOURCE_SCAN)
            .build();

    // TODO: Rename to detect.yarn.workspaces.excluded
    public static final CaseSensitiveStringListProperty DETECT_YARN_EXCLUDED_WORKSPACES =
        CaseSensitiveStringListProperty.newBuilder("detect.yarn.excluded.workspaces")
            .setInfo("Yarn Exclude Workspaces", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A comma-separated list of Yarn workspaces (specified by the workspace directory's relative path) to exclude.",
                "By default, Detect includes all workspaces, but will skip any Yarn workspaces specified via this property. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.YARN, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setExample("workspaces/workspace-a,workspaces/*-test")
            .build();

    // TODO: Rename to detect.yarn.workspaces.included
    public static final CaseSensitiveStringListProperty DETECT_YARN_INCLUDED_WORKSPACES =
        CaseSensitiveStringListProperty.newBuilder("detect.yarn.included.workspaces")
            .setInfo("Yarn Include Workspaces", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "A comma-separated list of Yarn workspaces (specified by the workspace directory's relative path) to include.",
                "By default, Detect includes all workspaces. If workspaces are excluded or included, Detect will include any workspace included by this property that is not excluded. Exclusion rules always win. This property accepts filename globbing-style wildcards. Refer to the <i>Configuring Synopsys Detect</i> > <i>Property wildcard support</i> page for more details."
            )
            .setGroups(DetectGroup.YARN, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setExample("workspaces/workspace-a,workspaces/workspace-b")
            .build();

    // TODO: Drop. Move help to the other logging property. Better logging doc would help instead of keeping this.
    public static final EnumProperty<LogLevel> LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION =
        EnumProperty.newBuilder("logging.level.com.synopsys.integration", LogLevel.INFO, LogLevel.class)
            .setInfo("Logging Level", DetectPropertyFromVersion.VERSION_5_3_0)
            .setHelp(
                "The logging level of Detect.",
                "To keep the log file size manageable, use INFO level logging for normal use. Use DEBUG or TRACE for troubleshooting.<p/>" +
                    "Detect logging uses Spring Boot logging, which uses Logback (https://logback.qos.ch). " +
                    "The format of this property name is <i>logging.level.{package}[.{class}]</i>. " +
                    "The property name shown above specifies package <i>com.synopsys.integration</i> because that is the name of Detect's top-level package. " +
                    "Changing the logging level for that package changes the logging level for all Detect code, as well as Synopsys integration libraries that Detect uses. " +
                    "Non-Synopsys libraries that Detect uses are not affected. " +
                    "However, you can use this property to set the logging level for some of the non-Synopsys libraries that Detect uses by using the appropriate package name. " +
                    "For example, <i>logging.level.org.apache.http=TRACE</i> sets the logging level to TRACE for the Apache HTTP client library. " +
                    "<p/>" +
                    "For log message format, Detect uses a default value of <i>%d{yyyy-MM-dd HH:mm:ss z} ${LOG_LEVEL_PATTERN:%-6p}[%thread] %clr(---){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}</i>. "
                    +
                    "You can change your log message format by setting the Spring Boot <i>logging.pattern.console</i> property to a different pattern. " +
                    "<p/>" +
                    "Refer to the Spring Boot logging and Logback Project documentation for more details."
            )
            .setGroups(DetectGroup.LOGGING, DetectGroup.GLOBAL)
            .build();

    public static final EnumProperty<LogLevel> LOGGING_LEVEL_DETECT =
        EnumProperty.newBuilder("logging.level.detect", LogLevel.INFO, LogLevel.class)
            .setInfo("Logging Level Shorthand", DetectPropertyFromVersion.VERSION_5_5_0)
            .setHelp(
                "Shorthand for the logging level of detect. Equivalent to setting <i>logging.level.com.synopsys.integration</i>.",
                "Refer to the description of property <i>logging.level.com.synopsys.integration</i> for additional details."
            )
            .setGroups(DetectGroup.LOGGING, DetectGroup.GLOBAL)
            .build();

    public static final BooleanProperty DETECT_WAIT_FOR_RESULTS =
        BooleanProperty.newBuilder("detect.wait.for.results", false)
            .setInfo("Wait For Results", DetectPropertyFromVersion.VERSION_5_5_0)
            .setHelp("If set to true, Detect will wait for Synopsys products until results are available or the detect.timeout is exceeded.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .build();

    public static final BooleanProperty DETECT_FOLLOW_SYMLINKS =
        BooleanProperty.newBuilder("detect.follow.symbolic.links", true)
            .setInfo("Follow Symbolic Links", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "If set to true, Detect will follow symbolic links when searching for detectors, when searching for files that select detectors (such as Bitbake and Sbt) need, when searching for directories to exclude from signature scan, and when searching for binary scan targets.  Symbolic links are not supported for Impact Analysis.")
            .setGroups(DetectGroup.GENERAL, DetectGroup.GLOBAL)
            .build();

    public static final EnumProperty<BlackduckScanMode> DETECT_BLACKDUCK_SCAN_MODE =
        EnumProperty.newBuilder("detect.blackduck.scan.mode", BlackduckScanMode.INTELLIGENT, BlackduckScanMode.class)
            .setInfo("Detect Scan Mode", DetectPropertyFromVersion.VERSION_6_9_0)
            .setHelp(
                "Set the Black Duck scanning mode of Detect",
                "Set the scanning mode of Detect to control how Detect will send data to Black Duck. RAPID will not persist the results and disables select Detect functionality for faster results. INTELLIGENT persists the results and permits all features of Detect."
            )
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK)
            .setCategory(DetectCategory.Advanced)
            .build();

    public static final EnumProperty<RapidCompareMode> DETECT_BLACKDUCK_RAPID_COMPARE_MODE =
        EnumProperty.newBuilder("detect.blackduck.rapid.compare.mode", RapidCompareMode.ALL, RapidCompareMode.class)
            .setInfo("Rapid Compare Mode", DetectPropertyFromVersion.VERSION_7_12_0)
            .setHelp(
                "Controls how rapid scan evaluates policy rules",
                "Set the compare mode of rapid scan. ALL evaluates all RAPID or FULL policies. BOM_COMPARE_STRICT will only show policy violations not present in an existing project version BOM. BOM_COMPARE depends on the type of policy rule modes and behaves like ALL if the policy rule is only RAPID but like BOM_COMPARE_STRICT when the policy rule is RAPID and FULL. See the Black Duck documentation for complete details."
            )
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK)
            .setCategory(DetectCategory.Advanced)
            .build();

    //#endregion Active Properties

    //#region Deprecated Properties
    // username/password ==> api token
    public static final String BDIO1_DEPRECATION_MESSAGE = "This property is being removed, along with the option to generate BDIO in BDIO1 format. In the future, BDIO2 format will be the only option.";
    public static final String AGGREGATION_MODE_DEPRECATION_MESSAGE = "This property is being removed, along with the ability to set the aggregation mode. In the future, Detect will always operate in SUBPROJECT aggregation mode (regardless of how it is configured) to more accurately report the dependency graph.";
    public static final String BAZEL_DEPENDENCY_TYPE_DEPRECATION_MESSAGE = "This property is being removed. Please use property 'detect.bazel.workspace.rules' instead.";

    public static final AllNoneEnumListProperty<WorkspaceRule> DETECT_BAZEL_DEPENDENCY_RULE =
        AllNoneEnumListProperty.newBuilder("detect.bazel.dependency.type", emptyList(), WorkspaceRule.class)
            .setInfo("Bazel workspace external dependency rule", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp(
                "The Bazel workspace rule(s) used to pull in external dependencies. If not set, Detect will attempt to determine the rule(s) from the contents of the WORKSPACE file.")
            .setGroups(DetectGroup.BAZEL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(BAZEL_DEPENDENCY_TYPE_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final NullableStringProperty DETECT_BOM_AGGREGATE_NAME =
        NullableStringProperty.newBuilder("detect.bom.aggregate.name")
            .setInfo("Aggregate BDIO File Name", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("If set, this will aggregate all the BOMs to create a single BDIO file with the filename provided.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(
                "This property is being removed. Use detect.bdio.file.name to control the name of the bdio file Detect generates. Currently detect.bdio.file.name has the same effects as this property. In the future, Detect will always operate in SUBPROJECT aggregation mode regardless of how it is configured; detect.bdio.file.name will only control the BDIO file name.",
                DetectMajorVersion.EIGHT
            )
            .build();

    @Deprecated
    public static final EnumProperty<AggregateMode> DETECT_BOM_AGGREGATE_REMEDIATION_MODE =
        EnumProperty.newBuilder("detect.bom.aggregate.remediation.mode", AggregateMode.TRANSITIVE, AggregateMode.class)
            .setInfo("BDIO Aggregate Remediation Mode", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp(
                "If an aggregate BDIO file is being generated (that is, property detect.bom.aggregate.name has been set) " +
                    "and this property is set to DIRECT, the aggregate BDIO file will exclude code location nodes " +
                    "from the top layer of the dependency tree to preserve the correct identification of direct dependencies in the resulting Black Duck BOM. " +
                    "When this property is set to TRANSITIVE (the default), component source information is preserved by including code location nodes at the " +
                    "top of the dependency tree, but all components will appear as TRANSITIVE in the BOM. " +
                    "SUBPROJECT aggregation mode provides both component source information and correct identification of direct and transitive dependencies by " +
                    "encoding code location nodes as subprojects in the graph. SUBPROJECT aggregation mode must only be used with Black Duck 2021.8.0 or later, " +
                    "and has no effect (is equivalent to TRANSITIVE mode) when detect.bdio2.enabled is set to false.")
            .setGroups(DetectGroup.PROJECT, DetectGroup.PROJECT_SETTING)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(AGGREGATION_MODE_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    // TODO: Let's make sure this deprecation message gets out. Currently only when set. If unset or 'true', warn users the endpoint is going away.
    @Deprecated
    public static final BooleanProperty BLACKDUCK_LEGACY_UPLOAD_ENABLED =
        BooleanProperty.newBuilder("blackduck.legacy.upload.enabled", true)
            .setInfo("Use legacy BDIO upload endpoints in Black Duck", DetectPropertyFromVersion.VERSION_7_0_0)
            .setHelp(
                "If set to true, Detect will upload the BDIO files to Black Duck using older REST APIs.  Set this to false if you want to use the intelligent persistent scan endpoints in Black Duck.  The intelligent persistent endpoints are a Black Duck feature to be used with a later Black Duck version.")
            .setGroups(DetectGroup.BLACKDUCK_SERVER, DetectGroup.BLACKDUCK)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated("This property is being removed as support for the legacy endpoint is dropped.", DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_BDIO2_ENABLED =
        BooleanProperty.newBuilder("detect.bdio2.enabled", true)
            .setInfo("BDIO 2 Enabled", DetectPropertyFromVersion.VERSION_6_1_0)
            .setHelp("The version of BDIO files to generate.", "If set to false, BDIO version 1 will be generated. If set to true, BDIO version 2 will be generated.")
            .setGroups(DetectGroup.PATHS, DetectGroup.GLOBAL)
            .setDeprecated(BDIO1_DEPRECATION_MESSAGE, DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final NullableStringProperty DETECT_GRADLE_INSPECTOR_VERSION =
        NullableStringProperty.newBuilder("detect.gradle.inspector.version")
            .setInfo("Gradle Inspector Version", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp(
                "The version of the Gradle Inspector that Detect should use. By default, Detect will try to automatically determine the correct Gradle Inspector version.",
                "The Detect Gradle detector uses a separate program, the Gradle Inspector, to discover dependencies from Gradle projects. Detect automatically downloads the Gradle Inspector as needed. Use the property to use a specific version of the Gradle Inspector."
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(
                "This property is being removed because it no longer provides functionality. The gradle inspector library is no longer used to gather Gradle dependencies. The init script generated by Detect has all the necessary functionality.",
                DetectMajorVersion.EIGHT
            )
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_MAVEN_INCLUDE_PLUGINS =
        BooleanProperty.newBuilder("detect.maven.include.plugins", false)
            .setInfo("Maven Include Plugins", DetectPropertyFromVersion.VERSION_5_6_0)
            .setHelp("Whether or not detect will include the plugins section when parsing a pom.xml in buildless legacy mode. ")
            .setGroups(DetectGroup.MAVEN, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(
                "This property is being removed. The project inspector will be used to parse maven projects. Please configure the project inspector to include modules.",
                DetectMajorVersion.EIGHT
            )
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_MAVEN_BUILDLESS_LEGACY_MODE =
        BooleanProperty.newBuilder("detect.maven.buildless.legacy.mode", true)
            .setInfo("Maven Buildless Legacy Mode", DetectPropertyFromVersion.VERSION_7_5_0)
            .setHelp("Legacy maven parsing supports plugins but the newer project inspector parser does not. Setting to false enables the project inspector for maven.")
            .setGroups(DetectGroup.MAVEN, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated("This property is being removed. The legacy maven buildless parser is being replaced by the project inspector.", DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_CONAN_INCLUDE_BUILD_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.conan.include.build.dependencies", true)
            .setInfo("Include Conan Build Dependencies", DetectPropertyFromVersion.VERSION_6_8_0)
            .setHelp("Set this value to false if you would like to exclude your project's build dependencies.")
            .setGroups(DetectGroup.CONAN, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_CONAN_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_PUD_DEPS_EXCLUDE_DEV =
        BooleanProperty.newBuilder("detect.pub.deps.exclude.dev", false)
            .setInfo("Detect Dart Pub Deps Exclude Dev Dependencies", DetectPropertyFromVersion.VERSION_7_5_0)
            .setHelp(
                "If true, the Dart Detector will pass the option --no-dev when running the command 'pub deps'."
            )
            .setGroups(DetectGroup.DART, DetectGroup.DETECTOR, DetectGroup.GLOBAL)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_PUB_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_GO_ENABLE_VERIFICATION =
        BooleanProperty.newBuilder("detect.go.mod.enable.verification", true)
            .setInfo("Go Mod Dependency Verification", DetectPropertyFromVersion.VERSION_7_1_0)
            .setHelp("When enabled, Detect will use the results of 'go mod why' to filter out unused dependencies. Set to false if you have an empty BOM.")
            .setGroups(DetectGroup.GO, DetectGroup.GLOBAL)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_GO_MOD_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_GRADLE_INCLUDE_UNRESOLVED_CONFIGURATIONS =
        BooleanProperty.newBuilder("detect.gradle.include.unresolved.configurations", false)
            .setInfo("Gradle Include Unresolved Configurations", DetectPropertyFromVersion.VERSION_7_6_0)
            .setHelp(
                "When set to true, dependencies discovered from unresolved Gradle configurations will be included. It is set to false by default for a more accurate BOM.",
                "Including dependencies from unresolved Gradle configurations could lead to false positives. Dependency versions from an unresolved configuration may differ from a resolved one. See https://docs.gradle.org/7.2/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs"
            )
            .setGroups(DetectGroup.GRADLE, DetectGroup.SOURCE_SCAN)
            .setCategory(DetectCategory.Advanced)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_GRADLE_CONFIGURATION_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_LERNA_INCLUDE_PRIVATE =
        BooleanProperty.newBuilder("detect.lerna.include.private", false)
            .setInfo("Include Lerna Packages defined as private.", DetectPropertyFromVersion.VERSION_6_0_0)
            .setHelp("Lerna allows for private packages that do not get published. Set this to true to include all packages including private packages.")
            .setGroups(DetectGroup.LERNA, DetectGroup.GLOBAL)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_LERNA_PACKAGE_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_NPM_INCLUDE_DEV_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.npm.include.dev.dependencies", true)
            .setInfo("Include NPM Development Dependencies", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Set this value to false if you would like to exclude your dev dependencies when ran.")
            .setGroups(DetectGroup.NPM, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_NPM_INCLUDE_PEER_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.npm.include.peer.dependencies", true)
            .setInfo("Include NPM Peer Dependencies", DetectPropertyFromVersion.VERSION_7_1_0)
            .setHelp("Set this value to false if you would like to exclude your peer dependencies when ran.")
            .setGroups(DetectGroup.NPM, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_PACKAGIST_INCLUDE_DEV_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.packagist.include.dev.dependencies", true)
            .setInfo("Include Packagist Development Dependencies", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Set this value to false if you would like to exclude your dev requires dependencies when ran.")
            .setGroups(DetectGroup.PACKAGIST, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_PACKAGIST_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_PEAR_ONLY_REQUIRED_DEPS =
        BooleanProperty.newBuilder("detect.pear.only.required.deps", false)
            .setInfo("Include Only Required Pear Dependencies", DetectPropertyFromVersion.VERSION_3_0_0)
            .setHelp("Set to true if you would like to include only required packages.")
            .setGroups(DetectGroup.PEAR, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_PEAR_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_RUBY_INCLUDE_RUNTIME_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.ruby.include.runtime.dependencies", true)
            .setInfo("Ruby Runtime Dependencies", DetectPropertyFromVersion.VERSION_5_4_0)
            .setHelp("If set to false, runtime dependencies will not be included when parsing *.gemspec files.")
            .setGroups(DetectGroup.RUBY, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_RUBY_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_RUBY_INCLUDE_DEV_DEPENDENCIES =
        BooleanProperty.newBuilder("detect.ruby.include.dev.dependencies", false)
            .setInfo("Ruby Development Dependencies", DetectPropertyFromVersion.VERSION_5_4_0)
            .setHelp("If set to true, development dependencies will be included when parsing *.gemspec files.")
            .setGroups(DetectGroup.RUBY, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_RUBY_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final BooleanProperty DETECT_YARN_PROD_ONLY =
        BooleanProperty.newBuilder("detect.yarn.prod.only", false)
            .setInfo("Include Yarn Production Dependencies Only", DetectPropertyFromVersion.VERSION_4_0_0)
            .setHelp("Set this to true to only scan production dependencies.")
            .setGroups(DetectGroup.YARN, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_YARN_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    @Deprecated
    public static final AllNoneEnumListProperty<PnpmDependencyType> DETECT_PNPM_DEPENDENCY_TYPES =
        AllNoneEnumListProperty.newBuilder("detect.pnpm.dependency.types", AllNoneEnum.ALL, PnpmDependencyType.class)
            .setInfo("pnpm Dependency Types", DetectPropertyFromVersion.VERSION_7_8_0)
            .setHelp(
                "Set this value to indicate which pnpm dependency types for should include in the BOM.",
                "If you want Detect to report a specific type(s) of dependencies, pass a comma-separated list of such types (ex. APP, DEV, OPTIONAL).  By default, all types will be reported."
            )
            .setGroups(DetectGroup.PNPM, DetectGroup.GLOBAL, DetectGroup.SOURCE_SCAN)
            .setDeprecated(createDetectorPropertyDeprecationMessage(DETECT_PNPM_DEPENDENCY_TYPES_EXCLUDED), DetectMajorVersion.EIGHT)
            .build();

    // TODO: Revise in 8.0.0
    // Can't take in the DetectProperty<?> due to an illegal forward reference :(
    private static String createDefaultDrivenPropertyHelpText(String exclusionTypePlural, String... propertyKeysDrivingDefaults) {
        String defaults = StringUtils.join(propertyKeysDrivingDefaults, ", ");
        return String.format(
            "Set this value to indicate which %s Detect should exclude from the BOM. Currently if this property is not set, the default value is driven by the default values of these properties: %s. In version 8.0.0, all %s will be reported by default.",
            exclusionTypePlural,
            defaults,
            exclusionTypePlural
        );
    }

    // TODO: Remove in 8.0.0
    // TODO: Rename to createReplacedByPropertyMessage. Add a method to PropertyBuilder if you're feeling lucky punk - TP APPROVED
    private static String createDetectorPropertyDeprecationMessage(@NotNull Property replacementProperty) {
        return String.format(
            "This property is being removed in favor of %s. If the replacement property is set, this property is ignored. The default value of this property is used if both properties are not set.",
            replacementProperty.getKey()
        );
    }

    // Accessor to get all properties
    public static Properties allProperties() {
        List<Property> properties = new ArrayList<>();
        Field[] allFields = DetectProperties.class.getDeclaredFields();
        for (Field field : allFields) {
            if (Property.class.isAssignableFrom(field.getType())) {
                try {
                    Object property = field.get(Property.class);
                    Property detectProperty = (Property) property;
                    properties.add(detectProperty);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new Properties(properties);
    }

    public static List<TypedProperty<?, ?>> allTypedProperties() {
        return allProperties().getProperties().stream()
            .filter(property -> TypedProperty.class.isAssignableFrom(property.getClass()))
            .map(property -> (TypedProperty<?, ?>) property)
            .collect(Collectors.toList());
    }

}
