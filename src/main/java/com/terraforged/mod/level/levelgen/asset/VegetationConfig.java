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

package com.terraforged.mod.level.levelgen.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.biome.viability.Viability;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record VegetationConfig(float frequency, float jitter, float density, TagKey<Biome> biomes, Viability viability) {
    public static final Codec<VegetationConfig> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
    	Codec.FLOAT.optionalFieldOf("frequency", 1F).forGetter(VegetationConfig::frequency),
    	Codec.FLOAT.optionalFieldOf("jitter", 1F).forGetter(VegetationConfig::jitter),
    	Codec.FLOAT.optionalFieldOf("density", 1F).forGetter(VegetationConfig::density),
    	TagKey.hashedCodec(Registries.BIOME).fieldOf("biomes").forGetter(VegetationConfig::biomes),
    	Viability.CODEC.fieldOf("viability").forGetter(VegetationConfig::viability)
    ).apply(instance, VegetationConfig::new));
    public static final Codec<Holder<VegetationConfig>> CODEC = RegistryFileCodec.create(TerraForged.VEGETATION, DIRECT_CODEC);
}
