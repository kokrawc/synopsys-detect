package com.synopsys.integration.detectable.detectables.cargo.transform;

import java.util.List;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.detectable.detectables.cargo.data.CargoLockPackageData;
import com.synopsys.integration.detectable.detectables.cargo.model.CargoLockPackage;
import com.synopsys.integration.detectable.detectables.cargo.parse.CargoDependencyLineParser;
import com.synopsys.integration.detectable.util.NameOptionalVersion;
import com.synopsys.integration.util.NameVersion;

public class CargoLockPackageDataTransformer {
    private final CargoDependencyLineParser cargoDependencyLineParser;

    public CargoLockPackageDataTransformer(CargoDependencyLineParser cargoDependencyLineParser) {
        this.cargoDependencyLineParser = cargoDependencyLineParser;
    }

    public CargoLockPackage transform(CargoLockPackageData cargoLockPackageData) {
        String packageName = cargoLockPackageData.getName().orElse("");
        String packageVersion = cargoLockPackageData.getVersion().orElse(null);
        NameVersion nameOptionalVersion = new NameVersion(packageName, packageVersion);

        List<NameOptionalVersion> dependencies = Bds.ofOptional(cargoLockPackageData.getDependencies())
            .mapOptional(cargoDependencyLineParser::parseDependencyName)
            .toPresentList();

        return new CargoLockPackage(nameOptionalVersion, dependencies);
    }
}
