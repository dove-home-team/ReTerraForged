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

package com.terraforged.mod.worldgen;

import java.util.Map;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.data.ModBiomes;
import com.terraforged.mod.util.storage.WeightMap;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.Structure;

public class GeneratorPreset {

	public static Generator build(
		TerrainLevels levels,
    	Map<BiomeType, Holder<ClimateType>> climates,
		WeightMap<Holder<TerrainNoise>> terrain,
		Holder<VegetationConfig>[] vegetation,
		Holder<Structure>[] structures,
		Holder<NoiseCave>[] caves,
		HolderGetter<Biome> biomes,
		HolderGetter<NoiseGeneratorSettings> noiseSettings
	) {
        var biomeGenerator = new BiomeGenerator(vegetation, structures, caves);
        var noiseGenerator = new NoiseGenerator(levels, terrain).withErosion();
        var biomeSource = new Source(
        	noiseGenerator,
        	climates,
        	biomes.getOrThrow(ModBiomes.CAVE),
        	biomes.getOrThrow(Biomes.PLAINS),
        	biomes.getOrThrow(Biomes.DEEP_COLD_OCEAN),
        	biomes.getOrThrow(Biomes.DEEP_FROZEN_OCEAN),
        	biomes.getOrThrow(Biomes.DEEP_LUKEWARM_OCEAN),
        	biomes.getOrThrow(Biomes.DEEP_OCEAN),
        	biomes.getOrThrow(Biomes.COLD_OCEAN),
        	biomes.getOrThrow(Biomes.FROZEN_OCEAN),
        	biomes.getOrThrow(Biomes.WARM_OCEAN),
        	biomes.getOrThrow(Biomes.OCEAN),
        	biomes.getOrThrow(Biomes.SNOWY_BEACH),
        	biomes.getOrThrow(Biomes.STONY_SHORE),
        	biomes.getOrThrow(Biomes.BEACH),
        	biomes.getOrThrow(Biomes.FROZEN_RIVER),
        	biomes.getOrThrow(Biomes.RIVER)
        );
        var vanillaGen = getVanillaGen(biomeSource, noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        return new Generator(levels, vanillaGen, biomeSource, biomeGenerator, noiseGenerator, terrain, climates, vegetation, structures, caves);
    }

    public static VanillaGen getVanillaGen(
    	BiomeSource source, 
    	Holder<NoiseGeneratorSettings> noiseSettings
    ) {
        return new VanillaGen(source, noiseSettings);
    }

    public static boolean isTerraForgedWorld(WorldGenSettings settings) {
        var stem = settings.dimensions().get(LevelStem.OVERWORLD).orElseThrow();
        return getGenerator(stem.generator()) != null;
    }

    public static boolean isTerraForgedWorld(ServerLevel level) {
        return getGenerator(level) != null;
    }

    public static Generator getGenerator(ServerLevel level) {
        return getGenerator(level.getChunkSource().getGenerator());
    }

    private static Generator getGenerator(ChunkGenerator chunkGenerator) {
        return chunkGenerator instanceof Generator generator ? generator : null;
    }
}