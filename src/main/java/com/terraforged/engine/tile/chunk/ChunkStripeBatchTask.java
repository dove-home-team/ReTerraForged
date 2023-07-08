/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile.chunk;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.batch.BatchTask;
import com.terraforged.engine.concurrent.batch.BatchTaskException;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.rivermap.Rivermap;

public class ChunkStripeBatchTask implements BatchTask {
    private static final String ERROR = "Failed to generate tile strip: x=%s, z=%s, length=%s";
    private final int seed;
    private final int x;
    private final int z;
    private final int stripeSize;
    private final Tile tile;
    private final Heightmap heightmap;
    private BatchTask.Notifier notifier = BatchTask.NONE;

    public ChunkStripeBatchTask(int seed, int x, int cz, int stripeSize, Tile tile, Heightmap heightmap) {
    	this.seed = seed;
    	this.x = x;
        this.z = cz;
        this.stripeSize = stripeSize;
        this.tile = tile;
        this.heightmap = heightmap;
    }

    @Override
    public void setNotifier(BatchTask.Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void run() {
        try {
            this.drive();
        }
        catch (Throwable t) {
            t.printStackTrace();
            throw new BatchTaskException(String.format(ERROR, this.x, this.z, this.stripeSize), t);
        }
        finally {
            this.notifier.markDone();
        }
    }

    private void drive() {
        int maxX = Math.min(this.tile.getChunkSize().total, this.x + this.stripeSize);
        for (int cx = this.x; cx < maxX; ++cx) {
            this.driveOne(this.seed, this.tile.getChunkWriter(cx, this.z), this.heightmap);
        }
    }

    protected void driveOne(int seed, ChunkWriter chunk, Heightmap heightmap) {
        Rivermap rivers = null;
        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                Cell cell = chunk.genCell(dx, dz);
                float x = chunk.getBlockX() + dx;
                float z = chunk.getBlockZ() + dz;
                heightmap.applyBase(seed, cell, x, z);
                rivers = Rivermap.get(seed, cell, rivers, heightmap);
                heightmap.applyRivers(seed, cell, x, z, rivers);
                heightmap.applyClimate(seed, cell, x, z);
            }
        }
    }

    public static class Zoom
    extends ChunkStripeBatchTask {
        private final float translateX;
        private final float translateZ;
        private final float zoom;

        public Zoom(int seed, int x, int z, int size, Tile tile, Heightmap heightmap, float translateX, float translateZ, float zoom) {
            super(seed, x, z, size, tile, heightmap);
            this.translateX = translateX;
            this.translateZ = translateZ;
            this.zoom = zoom;
        }

        @Override
        protected void driveOne(int seed, ChunkWriter chunk, Heightmap heightmap) {
            Rivermap rivers = null;
            for (int dz = 0; dz < 16; ++dz) {
                for (int dx = 0; dx < 16; ++dx) {
                    Cell cell = chunk.genCell(dx, dz);
                    float x = (float)(chunk.getBlockX() + dx) * this.zoom + this.translateX;
                    float z = (float)(chunk.getBlockZ() + dz) * this.zoom + this.translateZ;
                    heightmap.applyBase(seed, cell, x, z);
                    rivers = Rivermap.get(seed, cell, rivers, heightmap);
                    heightmap.applyRivers(seed, cell, x, z, rivers);
                    heightmap.applyClimate(seed, cell, x, z);
                }
            }
        }
    }
}
