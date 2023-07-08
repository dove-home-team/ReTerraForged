/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.gen;

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.batch.Batcher;
import com.terraforged.engine.tile.Tile;

public class TileGeneratorStriped extends TileGenerator {
    public TileGeneratorStriped(TileGenerator.Builder builder) {
        super(builder);
    }

    @Override
    public Tile generateRegion(int seed, int regionX, int regionZ) {
        Tile tile = this.createEmptyRegion(regionX, regionZ);
        try (Resource<Batcher> batcher = this.threadPool.batcher();){
            tile.generateAreaStriped(seed, this.generator.getHeightmap(), batcher.get(), this.batchSize);
        }
        this.postProcess(tile);
        return tile;
    }

    @Override
    public Tile generateRegion(int seed, float centerX, float centerZ, float zoom, boolean filter) {
        Tile tile = this.createEmptyRegion(0, 0);
        try (Resource<Batcher> batcher = this.threadPool.batcher();){
            tile.generateAreaStriped(seed, this.generator.getHeightmap(), batcher.get(), this.batchSize, centerX, centerZ, zoom);
        }
        this.postProcess(tile, filter);
        return tile;
    }
}
