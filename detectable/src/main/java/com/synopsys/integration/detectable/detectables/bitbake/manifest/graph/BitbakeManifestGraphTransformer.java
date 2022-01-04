package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.detectable.detectables.bitbake.common.BitbakeDetectorAlgorithm;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeNode;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeRecipe;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.model.BitbakeNodesByName;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.ShowRecipesResults;

public class BitbakeManifestGraphTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator;

    // TODO remove:
    private int nodeCount = 0;

    public BitbakeManifestGraphTransformer(BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator) {
        this.bitbakeManifestExternalIdGenerator = bitbakeManifestExternalIdGenerator;
    }

    public DependencyGraph generateGraph(Map<String, String> imageRecipes, ShowRecipesResults showRecipesResult, BitbakeGraph bitbakeGraphFromTaskDepends, BitbakeDetectorAlgorithm algorithm) {

        // TODO TEMPORARY
        BitbakeManifestGraphBuilder graphBuilder;
        if (algorithm == BitbakeDetectorAlgorithm.BYLAYER) {
            graphBuilder = new BitbakeManifestGraphBuilderByLayer(bitbakeManifestExternalIdGenerator);
        } else if (algorithm == BitbakeDetectorAlgorithm.BILEVEL) {
            graphBuilder = new BitbakeManifestGraphBuilderTwoLevel(bitbakeManifestExternalIdGenerator);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown BitbakeDetectorAlgorithm", algorithm.toString()));
        }

        // TODO it seems we can't afford a graph (way too many nodes). Have to follow task-depends graph to find dependencies, but add as
        // a flat list; each recipe only once (this presumably is why Jake did it that way).

        // TODO shouldn't need both of these. Also, we build a map inside graphBuilder
        List<String> recipesAddedToGraph = new ArrayList<>(bitbakeGraphFromTaskDepends.getNodes().size());
        Map<String, Dependency> namesToExternalIds = new HashMap<>(bitbakeGraphFromTaskDepends.getNodes().size());

        // TODO: Feels like these should be done before this method is called (and maybe be more consistent w/ each other?)
        Map<String, BitbakeNode> recipeVersionLookup = toMap(bitbakeGraphFromTaskDepends);
        BitbakeNodesByName bitbakeNodesByName = new BitbakeNodesByName(bitbakeGraphFromTaskDepends);

        // TODO: It takes less than a second per layer loop, but still: It'd be nice to have
        // in advance a list of recipes per layer, assuming that's easy

        for (String currentLayerName : showRecipesResult.getLayerNames()) {
            graphBuilder.addLayer(currentLayerName);

            for (Map.Entry<String, String> candidateImageRecipeEntry : imageRecipes.entrySet()) {
                String candidateImageRecipeName = candidateImageRecipeEntry.getKey();
                String candidateImageRecipeVersion = candidateImageRecipeEntry.getValue();

                // TODO this may be unnecessary
                if (StringUtils.isBlank(candidateImageRecipeVersion)) {
                    logger.warn("*** NO VERSION for recipe {}", candidateImageRecipeName);
                    continue;
                }
                addRecipeToGraph(showRecipesResult, bitbakeGraphFromTaskDepends, bitbakeNodesByName, recipeVersionLookup,
                    graphBuilder, null, currentLayerName, currentLayerName, candidateImageRecipeName, candidateImageRecipeVersion, 0, new ArrayList<>(), recipesAddedToGraph);
            }
        }
        logger.info("# recipes added to graph: {}", recipesAddedToGraph.size());
        return graphBuilder.build();
    }

    private void addRecipeToGraph(ShowRecipesResults showRecipesResult, BitbakeGraph bitbakeGraphFromTaskDepends, BitbakeNodesByName bitbakeNodesByName,
        Map<String, BitbakeNode> recipeVersionLookup, BitbakeManifestGraphBuilder graphBuilder, String parentRecipeName, String currentLayer, String recipeLayer, String recipeName,
        String recipeVersion, int depth, List<String> recipeDependencyBreadcrumbs, List<String> recipesAddedToGraph) {

        //logger.trace("[{}] Will add recipe {}:{} to graph IF it's direct and associated with this layer, or transitive", depth, recipeName, recipeVersion);

        if (depth == 0) {
            if (showRecipesResult.getRecipes().containsKey(recipeName)) {
                Collection<String> candidateImageRecipeLayers = showRecipesResult.getRecipes().get(recipeName).getLayerNames();
                if (candidateImageRecipeLayers.contains(recipeLayer)) {
                    logger.info("Recipe {} is a direct dependency and associated with this layer ({})", recipeName, recipeLayer);
                } else {
                    logger.trace("Recipe {} is a direct dependency not associated with this layer ({})", recipeName, recipeLayer);
                    return;
                }
            } else {
                logger.warn("No layer list found for recipe {}", recipeName);
                return;
            }
        }
        //logger.info("recipeDependencyBreadcrumbs: {}", recipeDependencyBreadcrumbs);
        if (recipeDependencyBreadcrumbs.contains(recipeName)) {
            //logger.info("Recipe {} is already in breadcrumb list {}; not adding it (again) to graph", recipeName, recipeDependencyBreadcrumbs);
            return;
        }

        if (recipesAddedToGraph.contains(recipeName)) {
            return;
        }

        nodeCount++;
        logger.info("[{}] Adding recipe {}:{} to graph (count: {})", depth, recipeName, recipeVersion, nodeCount);
        recipesAddedToGraph.add(recipeName);
        recipeDependencyBreadcrumbs.add(recipeName);

        boolean direct = depth == 0;
        graphBuilder.addRecipe(currentLayer, parentRecipeName, recipeLayer, recipeName, recipeVersion, direct);

        Optional<BitbakeNode> recipeNode = bitbakeNodesByName.get(recipeName);
        if (recipeNode.isPresent()) {
            //logger.info("Recipe {} has {} children", recipeName, recipeNode.get().getChildren().size());
            for (String childRecipeName : recipeNode.get().getChildren()) {
                //logger.info("[{}] looking at recipe {}'s child: {}", depth, recipeName, childRecipeName);
                if (childRecipeName.endsWith("-native")) {
                    //logger.info("Omitting native recipe {}", childRecipeName);
                    continue;
                }
                BitbakeRecipe childRecipe = showRecipesResult.getRecipes().get(childRecipeName);
                if (childRecipe != null) {
                    if (childRecipe.getPrimaryLayer().isPresent()) {
                        String childPrimaryLayer = childRecipe.getPrimaryLayer().get();
                        BitbakeNode childRecipeNode = recipeVersionLookup.get(childRecipe.getName());
                        if (childRecipeNode == null) {
                            // TODO this should never happen
                            logger.warn("Missing node for recipe {}", childRecipe.getName());
                            continue;
                        } // use !ifPresent:
                        String childRecipeVersion = childRecipeNode.getVersion().orElse(null);
                        if (childRecipeVersion == null) {
                            logger.warn("Missing version for recipe {}", childRecipe.getName());
                            continue;
                        }
                        //ExternalId childExternalId = generateRecipeExternalId(childPrimaryLayer, childRecipe.getName(), childRecipeVersion);
                        //logger.info("*** childExternalId for child recipe: {}:{}:{}: {}", childPrimaryLayer, childRecipe.getName(), childRecipeVersion, childExternalId.toString());
                        addRecipeToGraph(showRecipesResult, bitbakeGraphFromTaskDepends, bitbakeNodesByName, recipeVersionLookup, graphBuilder,
                            recipeName, currentLayer, childPrimaryLayer, childRecipe.getName(), childRecipeVersion, depth + 1, recipeDependencyBreadcrumbs, recipesAddedToGraph);
                    } else {
                        logger.warn("Don't have a primary layer for {}", childRecipe.getName());
                    }
                } else {
                    logger.warn("Recipe {} not found in showRecipesResults", childRecipeName);
                }
            }
        } else {
            logger.warn("Recipe {} not found in bitbakeNodesByName", recipeName);
        }
        if (!recipeDependencyBreadcrumbs.get(recipeDependencyBreadcrumbs.size()-1).equals(recipeName)) {
            throw new UnsupportedOperationException(String.format("What??? Expected recipe %s to be the last breadcrumb!", recipeName));
        }
        recipeDependencyBreadcrumbs.remove(recipeName);
    }

    // TODO this feels like we could build this map while generating the BitbakeGraph
    private Map<String, BitbakeNode> toMap(BitbakeGraph bitbakeGraph) {
        Map<String, BitbakeNode> byNameMap = new HashMap<>();
        for (BitbakeNode node : bitbakeGraph.getNodes()) {
            byNameMap.put(node.getName(), node);
        }
        return byNameMap;
    }
}
