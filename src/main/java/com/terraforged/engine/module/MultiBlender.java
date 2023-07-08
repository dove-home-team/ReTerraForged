/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.module;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.cell.Populator;
import com.terraforged.noise.func.Interpolation;
import com.terraforged.noise.util.NoiseUtil;

public class MultiBlender extends Select implements Populator {
    private final Populator lower;
    private final Populator middle;
    private final Populator upper;
    private final float midpoint;
    private final float blendLower;
    private final float blendUpper;
    private final float lowerRange;
    private final float upperRange;

    public MultiBlender(Populator control, Populator lower, Populator middle, Populator upper, float min, float mid, float max) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.middle = middle;
        this.midpoint = mid;
        this.blendLower = min;
        this.blendUpper = max;
        this.lowerRange = this.midpoint - this.blendLower;
        this.upperRange = this.blendUpper - this.midpoint;
    }

    @Override
    public void apply(int seed, Cell cell, float x, float y) {
        float select = this.getSelect(seed, cell, x, y);
        if (select < this.blendLower) {
            this.lower.apply(seed, cell, x, y);
            return;
        }
        if (select > this.blendUpper) {
            this.upper.apply(seed, cell, x, y);
            return;
        }
        if (select < this.midpoint) {
            float alpha = Interpolation.CURVE3.apply((select - this.blendLower) / this.lowerRange);
            this.lower.apply(seed, cell, x, y);
            float lowerVal = cell.value;
            this.middle.apply(seed, cell, x, y);
            float upperVal = cell.value;
            cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
        } else {
            float alpha = Interpolation.CURVE3.apply((select - this.midpoint) / this.upperRange);
            this.middle.apply(seed, cell, x, y);
            float lowerVal = cell.value;
            this.upper.apply(seed, cell, x, y);
            cell.value = NoiseUtil.lerp(lowerVal, cell.value, alpha);
        }
    }
}
