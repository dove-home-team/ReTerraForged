package com.terraforged.mod.registry;

import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.level.levelgen.noise.module.Valley;
import com.terraforged.mod.noise.Module;
import com.terraforged.mod.noise.combiner.Add;
import com.terraforged.mod.noise.combiner.Max;
import com.terraforged.mod.noise.combiner.Min;
import com.terraforged.mod.noise.combiner.Mul;
import com.terraforged.mod.noise.combiner.Sub;
import com.terraforged.mod.noise.modifier.Abs;
import com.terraforged.mod.noise.modifier.AdvancedTerrace;
import com.terraforged.mod.noise.modifier.Alpha;
import com.terraforged.mod.noise.modifier.Bias;
import com.terraforged.mod.noise.modifier.Boost;
import com.terraforged.mod.noise.modifier.Cache;
import com.terraforged.mod.noise.modifier.Clamp;
import com.terraforged.mod.noise.modifier.Curve;
import com.terraforged.mod.noise.modifier.Freq;
import com.terraforged.mod.noise.modifier.Grad;
import com.terraforged.mod.noise.modifier.Invert;
import com.terraforged.mod.noise.modifier.LegacyTerrace;
import com.terraforged.mod.noise.modifier.Map;
import com.terraforged.mod.noise.modifier.Modulate;
import com.terraforged.mod.noise.modifier.Power;
import com.terraforged.mod.noise.modifier.PowerCurve;
import com.terraforged.mod.noise.modifier.Scale;
import com.terraforged.mod.noise.modifier.Steps;
import com.terraforged.mod.noise.modifier.Terrace;
import com.terraforged.mod.noise.modifier.Threshold;
import com.terraforged.mod.noise.modifier.VariableCurve;
import com.terraforged.mod.noise.modifier.Warp;
import com.terraforged.mod.noise.selector.Base;
import com.terraforged.mod.noise.selector.Blend;
import com.terraforged.mod.noise.selector.MultiBlend;
import com.terraforged.mod.noise.selector.Select;
import com.terraforged.mod.noise.selector.VariableBlend;
import com.terraforged.mod.noise.source.BillowNoise;
import com.terraforged.mod.noise.source.CellEdgeNoise;
import com.terraforged.mod.noise.source.CellNoise;
import com.terraforged.mod.noise.source.Constant;
import com.terraforged.mod.noise.source.CubicNoise;
import com.terraforged.mod.noise.source.PerlinNoise;
import com.terraforged.mod.noise.source.PerlinNoise2;
import com.terraforged.mod.noise.source.Rand;
import com.terraforged.mod.noise.source.RidgeNoise;
import com.terraforged.mod.noise.source.SimplexNoise;
import com.terraforged.mod.noise.source.SimplexNoise2;
import com.terraforged.mod.noise.source.SimplexRidgeNoise;
import com.terraforged.mod.noise.source.Sin;

import net.minecraft.resources.ResourceKey;

public interface TFModules {
	ResourceKey<Codec<? extends Module>> CONSTANT = resolve("constant");
	ResourceKey<Codec<? extends Module>> BILLOW = resolve("billow");
	ResourceKey<Codec<? extends Module>> CELL = resolve("cell");
	ResourceKey<Codec<? extends Module>> CELL_EDGE = resolve("cell_edge");
	ResourceKey<Codec<? extends Module>> CUBIC = resolve("cubic");
	ResourceKey<Codec<? extends Module>> PERLIN = resolve("perlin");
	ResourceKey<Codec<? extends Module>> PERLIN2 = resolve("perlin2");
	ResourceKey<Codec<? extends Module>> RIDGE = resolve("ridge");
	ResourceKey<Codec<? extends Module>> SIMPLEX = resolve("simplex");
	ResourceKey<Codec<? extends Module>> SIMPLEX2 = resolve("simplex2");
	ResourceKey<Codec<? extends Module>> SIMPLEX_RIDGE = resolve("simplex_ridge");
	ResourceKey<Codec<? extends Module>> SIN = resolve("sin");
	ResourceKey<Codec<? extends Module>> LINE = resolve("line");
	ResourceKey<Codec<? extends Module>> RAND = resolve("rand");
	ResourceKey<Codec<? extends Module>> ADD = resolve("add");
	ResourceKey<Codec<? extends Module>> MAX = resolve("max");
	ResourceKey<Codec<? extends Module>> MIN = resolve("min");
	ResourceKey<Codec<? extends Module>> MUL = resolve("mul");
	ResourceKey<Codec<? extends Module>> SUB = resolve("sub");
	ResourceKey<Codec<? extends Module>> ABS = resolve("abs");
	ResourceKey<Codec<? extends Module>> ADVANCED_TERRACE = resolve("advanced_terrace");
	ResourceKey<Codec<? extends Module>> ALPHA = resolve("alpha");
	ResourceKey<Codec<? extends Module>> BIAS = resolve("bias");
	ResourceKey<Codec<? extends Module>> BOOST = resolve("boost");
	ResourceKey<Codec<? extends Module>> CACHE = resolve("cache");
	ResourceKey<Codec<? extends Module>> CLAMP = resolve("clamp");
	ResourceKey<Codec<? extends Module>> CURVE = resolve("curve");
	ResourceKey<Codec<? extends Module>> FREQ = resolve("freq");
	ResourceKey<Codec<? extends Module>> GRAD = resolve("grad");
	ResourceKey<Codec<? extends Module>> INVERT = resolve("invert");
	ResourceKey<Codec<? extends Module>> LEGACY_TERRACE = resolve("legacy_terrace");
	ResourceKey<Codec<? extends Module>> MAP = resolve("map");
	ResourceKey<Codec<? extends Module>> MODULATE = resolve("modulate");
	ResourceKey<Codec<? extends Module>> POWER = resolve("power");
	ResourceKey<Codec<? extends Module>> POWER_CURVE = resolve("power_curve");
	ResourceKey<Codec<? extends Module>> SCALE = resolve("scale");
	ResourceKey<Codec<? extends Module>> STEPS = resolve("steps");
	ResourceKey<Codec<? extends Module>> TERRACE = resolve("terrace");
	ResourceKey<Codec<? extends Module>> THRESHOLD = resolve("threshold");
	ResourceKey<Codec<? extends Module>> VARIABLE_CURVE = resolve("variable_curve");
	ResourceKey<Codec<? extends Module>> WARP = resolve("warp");
	ResourceKey<Codec<? extends Module>> BASE = resolve("base");
	ResourceKey<Codec<? extends Module>> BLEND = resolve("blend");
	ResourceKey<Codec<? extends Module>> MULTI_BLEND = resolve("multi_blend");
	ResourceKey<Codec<? extends Module>> SELECT = resolve("select");
	ResourceKey<Codec<? extends Module>> VARIABLE_BLEND = resolve("variable_blend");
	ResourceKey<Codec<? extends Module>> VALLEY = resolve("valley");
	
	static void register(BiConsumer<ResourceKey<Codec<? extends Module>>, Codec<? extends Module>> register) {
		register.accept(CONSTANT, Constant.CODEC);
		register.accept(BILLOW, BillowNoise.CODEC);
		register.accept(CELL, CellNoise.CODEC);
		register.accept(CELL_EDGE, CellEdgeNoise.CODEC);
		register.accept(CUBIC, CubicNoise.CODEC);
		register.accept(PERLIN, PerlinNoise.CODEC);
		register.accept(PERLIN2, PerlinNoise2.CODEC);
		register.accept(RIDGE, RidgeNoise.CODEC);
		register.accept(SIMPLEX, SimplexNoise.CODEC);
		register.accept(SIMPLEX2, SimplexNoise2.CODEC);
		register.accept(SIMPLEX_RIDGE, SimplexRidgeNoise.CODEC);
		register.accept(SIN, Sin.CODEC);
		register.accept(RAND, Rand.CODEC);
		register.accept(ADD, Add.CODEC);
		register.accept(MAX, Max.CODEC);
		register.accept(MIN, Min.CODEC);
		register.accept(MUL, Mul.CODEC);
		register.accept(SUB, Sub.CODEC);
		register.accept(ABS, Abs.CODEC);
		register.accept(ADVANCED_TERRACE, AdvancedTerrace.CODEC);
		register.accept(ALPHA, Alpha.CODEC);
		register.accept(BIAS, Bias.CODEC);
		register.accept(BOOST, Boost.CODEC);
		register.accept(CACHE, Cache.CODEC);
		register.accept(CLAMP, Clamp.CODEC);
		register.accept(CURVE, Curve.CODEC);
		register.accept(FREQ, Freq.CODEC);
		register.accept(GRAD, Grad.CODEC);
		register.accept(INVERT, Invert.CODEC);
		register.accept(LEGACY_TERRACE, LegacyTerrace.CODEC);
		register.accept(MAP, Map.CODEC);
		register.accept(MODULATE, Modulate.CODEC);
		register.accept(POWER, Power.CODEC);
		register.accept(POWER_CURVE, PowerCurve.CODEC);
		register.accept(SCALE, Scale.CODEC);
		register.accept(STEPS, Steps.CODEC);
		register.accept(TERRACE, Terrace.CODEC);
		register.accept(THRESHOLD, Threshold.CODEC);
		register.accept(VARIABLE_CURVE, VariableCurve.CODEC);
		register.accept(WARP, Warp.CODEC);
		register.accept(BASE, Base.CODEC);
		register.accept(BLEND, Blend.CODEC);
		register.accept(MULTI_BLEND, MultiBlend.CODEC);
		register.accept(SELECT, Select.CODEC);
		register.accept(VARIABLE_BLEND, VariableBlend.CODEC);
		register.accept(VALLEY, Valley.Noise.CODEC);
	}
	
	private static ResourceKey<Codec<? extends Module>> resolve(String path) {
		return TerraForged.resolve(TerraForged.MODULE, path);
	}
}
