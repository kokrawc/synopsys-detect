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

// TODO right now transitives recipes are placed under the layer of the direct dependency under which they are FIRST found, which is random
// They should be under their own layer
// That makes layer nodes seem redundant, since the recipe ID contains its layer
// ==> So, get rid of layer nodes
// TODO Since we can't build full graph, maybe transitives should be pushed down a layer under a dummy node to get the categorization right
// So, first level: all direct dependencies plus a fake one under which ALL transitives go

public class BitbakeManifestGraphBuilderByLayer implements BitbakeManifestGraphBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator;
    private MutableDependencyGraph dependencyGraph;
    private final Map<String, Dependency> layerDependenciesAdded = new HashMap<>();
    private final Map<String, Dependency> recipeDependenciesAdded = new HashMap<>();

    public BitbakeManifestGraphBuilderByLayer(BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator) {
        this.bitbakeManifestExternalIdGenerator = bitbakeManifestExternalIdGenerator;
        dependencyGraph = new MutableMapDependencyGraph();
    }

    @Override
    public BitbakeManifestGraphBuilder addLayer(String layerName) {
        if (layerDependenciesAdded.get(layerName) != null) {
            logger.warn("Attempt to add layer {} to graph more than once");
            return this;
        }
        logger.info("*** layer: {}", layerName);
        ExternalId layerExternalId = bitbakeManifestExternalIdGenerator.generateLayerExternalId(layerName);
        Dependency layerDependency = new Dependency(layerExternalId);
        dependencyGraph.addChildToRoot(layerDependency);
        layerDependenciesAdded.put(layerName, layerDependency);
        logger.info("*** layerExternalId for layer: {}: {}", layerName, layerExternalId.toString());
        return this;
    }

    @Override
    public BitbakeManifestGraphBuilder addRecipe(String currentLayer, @Nullable String parentRecipeName, String recipeLayer, String recipeName, String recipeVersion, boolean direct) {
        if (recipeDependenciesAdded.containsKey(recipeName)) {
            // if we were building a true graph, we wouldn't do this
            return this;
        }
        ExternalId imageRecipeExternalId = bitbakeManifestExternalIdGenerator.generateRecipeExternalId(recipeLayer, recipeName, recipeVersion);
        Dependency recipeDependency = new Dependency(imageRecipeExternalId);
        // If we wanted a true graph: if (parentRecipeName != null) parentDependency = recipeDependenciesAdded.get(parentRecipeName);
        Dependency parentDependency = layerDependenciesAdded.get(currentLayer);
        dependencyGraph.addChildWithParent(recipeDependency, parentDependency);
        recipeDependenciesAdded.put(recipeName, recipeDependency);

        //logger.info("*** externalId for recipe: {}:{}:{}: {}", recipeLayer, recipeName, recipeVersion, imageRecipeExternalId.toString());
        return this;
    }

    @Override
    public MutableDependencyGraph build() {
        return dependencyGraph;
    }
}
