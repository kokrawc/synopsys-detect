package com.synopsys.integration.detectable.detectables.cargo.parse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.moandjiezana.toml.Toml;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectables.cargo.model.CargoLock;
import com.synopsys.integration.detectable.detectables.cargo.model.Package;

public class CargoLockParser {

    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    private final Map<String, Dependency> packageMap = new HashMap<>();

    public DependencyGraph parseLockFile(String lockFile) throws DetectableException {
        try {
            CargoLock cargoLock = new Toml().read(lockFile).to(CargoLock.class);
            if (cargoLock.getPackages().isPresent()) {
                return parseDependencies(cargoLock.getPackages().get().stream()
                    .filter(p -> p.getName().isPresent())
                    .collect(Collectors.toList()));
            }
        } catch (IllegalStateException e) {
            throw new DetectableException("Illegal syntax was detected in Cargo.lock file", e);
        }
        return new MutableMapDependencyGraph();
    }

    private DependencyGraph parseDependencies(List<Package> lockPackages) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        Set<String> rootPackages = determineRootPackages(lockPackages);

        for (String rootPackage : rootPackages) {
            graph.addChildToRoot(packageMap.get(rootPackage));
        }

        for (Package lockPackage : lockPackages) {
            if (!lockPackage.getDependencies().isPresent()) {
                continue;
            }
            List<String> dependencies = extractDependencyIds(lockPackage.getDependencies().get());
            for (String dependency : dependencies) {
                Dependency child = packageMap.get(dependency);
                Dependency parent = packageMap.get(createPackageId(lockPackage.getName().get(), lockPackage.getVersion().orElse("")));
                if (child != null && parent != null) {
                    graph.addChildWithParent(child, parent);
                }
            }
        }
        return graph;
    }

    private Set<String> determineRootPackages(List<Package> lockPackages) {
        Set<String> rootPackages = new HashSet<>();
        Set<String> dependencyPackages = new HashSet<>();

        for (Package lockPackage : lockPackages) {
            String packageName = lockPackage.getName().get(); // we filtered out packages with no names
            String packageVersion = lockPackage.getVersion().orElse("");
            String packageId = createPackageId(packageName, packageVersion);

            Dependency dependency = createCargoDependency(packageName, packageVersion);
            packageMap.put(packageId, dependency);
            packageMap.put(packageName, dependency); // packages for which only one version is a project dependency may only be referenced by package name by other packages
            rootPackages.add(packageId);
            lockPackage.getDependencies()
                .map(this::extractDependencyIds)
                .ifPresent(dependencyPackages::addAll);
        }
        return pruneRootPackages(rootPackages, dependencyPackages);
    }

    private Set<String> pruneRootPackages(Set<String> rootPackages, Set<String> packageDependencies) {
        for (String packageDependency : packageDependencies) {
            rootPackages = rootPackages.stream()
                .filter(rootPackage -> !rootPackage.startsWith(packageDependency)) // filters dependency declaration for "package" and "package-version"
                .collect(Collectors.toSet());
        }
        return rootPackages;
    }

    private String createPackageId(String name, String version) {
        if (StringUtils.isBlank(version)) {
            return name;
        }
        return String.format("%s-%s", name, version);
    }

    private List<String> extractDependencyIds(List<String> rawDependencies) {
        return rawDependencies.stream()
            .map(dependency -> {
                String[] pieces = dependency.split(" ");
                if (pieces.length == 1) {
                    return pieces[0]; // not all dependencies are declared with their versions
                }
                return createPackageId(pieces[0], pieces[1]);
            })
            .collect(Collectors.toList());
    }

    private Dependency createCargoDependency(String name, String version) {
        ExternalId dependencyExternalId = externalIdFactory.createNameVersionExternalId(Forge.CRATES, name, version);
        return new Dependency(name, version, dependencyExternalId);
    }
}
