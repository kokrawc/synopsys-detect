package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model.NuGetLockFile;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model.ProjectReference;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model.RestoreFramework;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.LockFileParser;

//Small samples from real lock files to verify the model is actually handling these cases. (Basically GSON tests so tried to keep them small and sweet)
public class ModelTests {
    @Test
    public void parsesDependencyGroupsInEmpty() {
        String lockfile = "{ \"projectFileDependencyGroups\": {\n"
            + "    \"\": [\n"
            + "      \"Microsoft.Azure.Mobile.Client >= 4.0.1\""
            + "    ]\n"
            + "  } }";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);
        Assertions.assertEquals(1, lockFile.projectFileDependencyGroups.size());
        List<String> group = lockFile.projectFileDependencyGroups.get("");
        Assertions.assertEquals(1, group.size());
        Assertions.assertEquals("Microsoft.Azure.Mobile.Client >= 4.0.1", group.get(0));
    }

    @Test
    public void parsesDependencyGroupsInNamed() {
        String lockfile = "{ \"projectFileDependencyGroups\": {\n"
            + "    \"\": [],\n"
            + "    \"UAP,Version=v10.0\": [\n"
            + "      \"Xamarin.Forms >= 2.3.4.247\""
            + "    ]\n"
            + "  } }";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);
        Assertions.assertEquals(2, lockFile.projectFileDependencyGroups.size());

        List<String> group1 = lockFile.projectFileDependencyGroups.get("");
        Assertions.assertEquals(0, group1.size());

        List<String> group2 = lockFile.projectFileDependencyGroups.get("UAP,Version=v10.0");
        Assertions.assertEquals(1, group2.size());
        Assertions.assertEquals("Xamarin.Forms >= 2.3.4.247", group2.get(0));
    }

    @Test
    public void parsesDependencyGroupsWithSpace() {
        String lockfile = "{ \"projectFileDependencyGroups\": {\n"
            + "    \"\": [],\n"
            + "    \"UAP,Version=v10.0\": [\n"
            + "      \"MySpecialProject with space in name >= 1.0.0\""
            + "    ]\n"
            + "  } }";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);
        Assertions.assertEquals(2, lockFile.projectFileDependencyGroups.size());

        List<String> group1 = lockFile.projectFileDependencyGroups.get("");
        Assertions.assertEquals(0, group1.size());

        List<String> group2 = lockFile.projectFileDependencyGroups.get("UAP,Version=v10.0");
        Assertions.assertEquals(1, group2.size());
        Assertions.assertEquals("MySpecialProject with space in name >= 1.0.0", group2.get(0));
    }

    @Test
    void parsesProjectRestoreFrameworks() {
        String lockfile = "{ \"project\": { \n"
            + "  \"restore\": {\n"
            + "     \"frameworks\": {\n"
            + "        \"uap10.0.10240\": {\n"
            + "          \"projectReferences\": {\n"
            + "            \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\": {\n"
            + "              \"projectPath\": \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\"\n"
            + "            }\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "   }\n"
            + "}}";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);
        Assertions.assertEquals(1, lockFile.project.restore.frameworks.size());

        RestoreFramework framework = lockFile.project.restore.frameworks.get("uap10.0.10240");
        Assertions.assertEquals(1, framework.projectReferences.size());

        ProjectReference reference = framework.projectReferences.get("C:\\PROJECTS\\SmartHive\\SmartHive.Controllers\\SmartHive.Controllers.csproj");
        Assertions.assertEquals("C:\\PROJECTS\\SmartHive\\SmartHive.Controllers\\SmartHive.Controllers.csproj", reference.projectPath);
    }

    @Test
    void parsesProjectNameFromRestore() {
        String lockfile = "{ \"project\": {\n"
            + "    \"restore\": {\n"
            + "      \"projectName\": \"SmartHive.LevelMapApp.UWP\"\n"
            + "    }\n"
            + "}}";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);

        Assertions.assertEquals("SmartHive.LevelMapApp.UWP", lockFile.project.restore.projectName);
    }

    @Test
    void parsesProjectNameWithSpaceFromRestore() {
        String lockfile = "{ \"project\": {\n"
            + "    \"restore\": {\n"
            + "      \"projectName\": \"Project With Space\"\n"
            + "    }\n"
            + "}}";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);

        Assertions.assertEquals("Project With Space", lockFile.project.restore.projectName);
    }

    @Test
    void parsesVersionOnProject() {
        String lockfile = "{   \"project\": {\n"
            + "    \"version\": \"1.0.0\" } }";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);

        Assertions.assertEquals("1.0.0", lockFile.project.version);
    }

    @Test
    void parsesProjectDependencies() {
        String lockfile = "{   \"project\": {\n"
            + "    \"dependencies\": {\n"
            + "      \"Xamarin.Forms\": \"[2.3.4.270, )\"\n"
            + "    }\n"
            + "  }\n"
            + "}";

        NuGetLockFile lockFile = new LockFileParser(new Gson()).parse(lockfile);

        Assertions.assertEquals(1, lockFile.project.dependencies.size());
        Assertions.assertEquals("[2.3.4.270, )", lockFile.project.dependencies.get("Xamarin.Forms"));
    }

}
