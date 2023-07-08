/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.world.terrain.region;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.cell.Populator;
import com.terraforged.noise.util.NoiseUtil;

public class RegionLerper implements Populator {
    private final Populator lower;
    private final Populator upper;

    public RegionLerper(Populator lower, Populator upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public void apply(int seed, Cell cell, float x, float y) {
        float alpha = cell.terrainRegionEdge;
        if (alpha == 0.0f) {
            this.lower.apply(seed, cell, x, y);
            return;
        }
        if (alpha == 1.0f) {
            this.upper.apply(seed, cell, x, y);
            return;
        }
        this.lower.apply(seed, cell, x, y);
        float lowerValue = cell.value;
        this.upper.apply(seed, cell, x, y);
        float upperValue = cell.value;
        cell.value = NoiseUtil.lerp(lowerValue, upperValue, alpha);
    }
}
