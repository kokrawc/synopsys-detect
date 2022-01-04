package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;

public interface BitbakeManifestGraphBuilder {
    BitbakeManifestGraphBuilder addLayer(String layerName);

    BitbakeManifestGraphBuilder addRecipe(String currentLayer, @Nullable String parentRecipeName, String recipeLayer, String recipeName, String recipeVersion, boolean direct);

    MutableDependencyGraph build();
}
