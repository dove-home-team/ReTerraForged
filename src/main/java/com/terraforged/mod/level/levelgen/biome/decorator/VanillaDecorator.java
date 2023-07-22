/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.level.levelgen.biome.decorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.terraforged.mod.level.levelgen.generator.TFChunkGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

public class VanillaDecorator {
    public static void decorate(long seed,
                                int from, int to,
                                BlockPos origin,
                                Holder<Biome> biome,
                                ChunkAccess chunk,
                                WorldGenLevel level,
                                TFChunkGenerator generator,
                                WorldgenRandom random,
                                StructureManager structureManager,
                                FeatureDecorator decorator) {
    	Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Map<Integer, List<Structure>> structuresByStep = registry.stream().collect(Collectors.groupingBy((structure) -> {
           return structure.step().ordinal();
        }));
        for (int stage = from; stage <= to; stage++) {
            var structures = structuresByStep.getOrDefault(stage, ImmutableList.of());
            var features = decorator.getStageFeatures(stage, biome.value());
            if (features == null) continue;

            placeStructures(seed, stage, chunk, level, generator, random, structureManager, structures);

            placeFeatures(seed, structuresByStep.size(), stage, origin, level, generator, random, features);
        }
    }

    private static void placeStructures(long seed,
                                        int stage,
                                        ChunkAccess chunk,
                                        WorldGenLevel level,
                                        TFChunkGenerator generator,
                                        WorldgenRandom random,
                                        StructureManager structureManager,
                                        List<Structure> structures) {
        var chunkPos = chunk.getPos();
        var sectionPos = SectionPos.of(chunkPos, level.getMinSection());

        for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
            random.setFeatureSeed(seed, structureIndex, stage);

            var structure = structures.get(structureIndex);
            var starts = structureManager.startsForStructure(sectionPos, structure);
            for (int startIndex = 0; startIndex < starts.size(); startIndex++) {
                var start = starts.get(startIndex);
                start.placeInChunk(level, structureManager, generator, random, getWritableArea(chunk), chunkPos);
            }
        }
    }
    
    private static final List<ResourceKey<ConfiguredFeature<?, ?>>> HIDDEN = ImmutableList.of(
    	OreFeatures.ORE_DIRT,
    	OreFeatures.ORE_GRANITE,
    	OreFeatures.ORE_DIORITE,
    	OreFeatures.ORE_ANDESITE,
    	OreFeatures.ORE_GRAVEL,
    	MiscOverworldFeatures.SPRING_LAVA_OVERWORLD,
    	MiscOverworldFeatures.SPRING_WATER
    );
    private static void placeFeatures(long seed,
                                      int offset,
                                      int stage,
                                      BlockPos origin,
                                      WorldGenLevel level,
                                      TFChunkGenerator generator,
                                      WorldgenRandom random,
                                      HolderSet<PlacedFeature> features) {

    	//TODO don't hardcode this
    	//TODO make it a placement or something
    	
    	int minOreDepth = 8;
    	int height = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - minOreDepth;
        for (int i = 0; i < features.size(); i++) {
            var feature = features.get(i).value();
            //move ore into the ground
            if(feature.feature() instanceof Holder.Reference<ConfiguredFeature<?,?>> ref) {
                if(origin.getY() > height && HIDDEN.contains(ref.key())) {
                	origin = origin.atY(height);
                }
            }
            
            random.setFeatureSeed(seed, offset + i, stage);
            feature.placeWithBiomeCheck(level, generator, random, origin);
        }
    }

    public static Map<GenerationStep.Decoration, List<Holder<Structure>>> buildStructureMap(Holder<Structure>[] structures) {
        final Map<GenerationStep.Decoration, List<Holder<Structure>>> map = new EnumMap<>(GenerationStep.Decoration.class);

        for(Holder<Structure> holder : structures) {
            map.computeIfAbsent(holder.value().step(), s -> new ArrayList<>()).add(holder);
        }
        
        for (var stage : FeatureDecorator.STAGES) {
            if (!map.containsKey(stage)) {
                map.put(stage, Collections.emptyList());
            }
        }

        return map;
    }

    private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
        var chunkPos = chunkAccess.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();

        LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
        int minY = levelHeightAccessor.getMinBuildHeight() + 1;
        int maxY = levelHeightAccessor.getMaxBuildHeight() - 1;

        return new BoundingBox(minX, minY, minZ, minX + 15, maxY, minZ + 15);
    }
}
