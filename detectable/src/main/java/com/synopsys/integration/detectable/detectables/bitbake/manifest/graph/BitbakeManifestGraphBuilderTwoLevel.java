package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;

public class BitbakeManifestGraphBuilderTwoLevel implements BitbakeManifestGraphBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator;
    private MutableDependencyGraph dependencyGraph;
    private final Map<String, Dependency> recipeDependenciesAdded = new HashMap<>();
    private Dependency transitivesParentDependency = null;

    public BitbakeManifestGraphBuilderTwoLevel(BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator) {
        this.bitbakeManifestExternalIdGenerator = bitbakeManifestExternalIdGenerator;
        dependencyGraph = new MutableMapDependencyGraph();
    }
    @Override
    public BitbakeManifestGraphBuilder addLayer(final String layerName) {
        return this;
    }

    @Override
    public BitbakeManifestGraphBuilder addRecipe(final String currentLayer, @Nullable final String parentRecipeName, final String recipeLayer, final String recipeName, final String recipeVersion, boolean direct) {
        if (recipeDependenciesAdded.containsKey(recipeName)) {
            // if we were building a true graph, we wouldn't do this
            return this;
        }
        ExternalId imageRecipeExternalId = bitbakeManifestExternalIdGenerator.generateRecipeExternalId(recipeLayer, recipeName, recipeVersion);
        Dependency recipeDependency = new Dependency(imageRecipeExternalId);
        // If we wanted a true graph: if (parentRecipeName != null) parentDependency = recipeDependenciesAdded.get(parentRecipeName);
        //Dependency parentDependency = layerDependenciesAdded.get(currentLayer);
        if (direct) {
            dependencyGraph.addChildToRoot(recipeDependency);
        } else {
            ensureTransitivesParentDependencyExists();
            dependencyGraph.addChildWithParent(recipeDependency, transitivesParentDependency);
        }
        recipeDependenciesAdded.put(recipeName, recipeDependency);

        //logger.info("*** externalId for recipe: {}:{}:{}: {}", recipeLayer, recipeName, recipeVersion, imageRecipeExternalId.toString());
        return this;
    }

    @Override
    public MutableDependencyGraph build() {
        return dependencyGraph;
    }

    private void ensureTransitivesParentDependencyExists() {
        if (transitivesParentDependency == null) {
            ExternalId transitivesParentExternalId = bitbakeManifestExternalIdGenerator.generateRecipeExternalId("all", "transitivesParent", "0.0");
            transitivesParentDependency = new Dependency(transitivesParentExternalId);
            dependencyGraph.addChildToRoot(transitivesParentDependency);
        }
    }
}
