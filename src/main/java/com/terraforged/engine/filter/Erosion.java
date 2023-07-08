/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.filter;

import java.util.function.IntFunction;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.settings.FilterSettings;
import com.terraforged.engine.tile.Size;
import com.terraforged.engine.util.FastRandom;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.noise.util.NoiseUtil;

public class Erosion implements Filter {
//	TODO reference these instead of constants
//    private static final int erosionRadius = 4;
//    private static final float inertia = 0.05f;
//    private static final float sedimentCapacityFactor = 4.0f;
//    private static final float minSedimentCapacity = 0.01f;
//    private static final float evaporateSpeed = 0.01f;
//    private static final float gravity = 3.0f;
    private final float erodeSpeed;
    private final float depositSpeed;
    private final float initialSpeed;
    private final float initialWaterVolume;
    private final int maxDropletLifetime;
    private final int[][] erosionBrushIndices;
    private final float[][] erosionBrushWeights;
    private final int seed;
    private final int mapSize;
    private final Modifier modifier;

    public Erosion(int seed, int mapSize, FilterSettings.Erosion settings, Modifier modifier) {
        this.seed = seed;
        this.mapSize = mapSize;
        this.modifier = modifier;
        this.erodeSpeed = settings.erosionRate;
        this.depositSpeed = settings.depositeRate;
        this.initialSpeed = settings.dropletVelocity;
        this.initialWaterVolume = settings.dropletVolume;
        this.maxDropletLifetime = settings.dropletLifetime;
        this.erosionBrushIndices = new int[mapSize * mapSize][];
        this.erosionBrushWeights = new float[mapSize * mapSize][];
        
        this.initBrushes(mapSize, 4);
    }

    public int getSize() {
        return this.mapSize;
    }

    @Override
    public void apply(Filterable map, int regionX, int regionZ, int iterationsPerChunk) {
        int chunkX = map.getBlockX() >> 4;
        int chunkZ = map.getBlockZ() >> 4;
        int lengthChunks = map.getSize().total >> 4;
        int borderChunks = map.getSize().border >> 4;
        Size size = map.getSize();
        int mapSize = size.total;
        float maxPos = mapSize - 2;
        Cell[] cells = map.getBacking();
        TerrainPos gradient1 = new TerrainPos();
        TerrainPos gradient2 = new TerrainPos();
        FastRandom random = new FastRandom();
        for (int i = 0; i < iterationsPerChunk; ++i) {
            long iterationSeed = NoiseUtil.seed(this.seed, i);
            for (int cz = 0; cz < lengthChunks; ++cz) {
                int relZ = cz << 4;
                int seedZ = chunkZ + cz - borderChunks;
                for (int cx = 0; cx < lengthChunks; ++cx) {
                    int relX = cx << 4;
                    int seedX = chunkX + cx - borderChunks;
                    long chunkSeed = NoiseUtil.seed(seedX, seedZ);
                    random.seed(chunkSeed, iterationSeed);
                    float posX = relX + random.nextInt(16);
                    float posZ = relZ + random.nextInt(16);
                    posX = NoiseUtil.clamp(posX, 1.0f, maxPos);
                    posZ = NoiseUtil.clamp(posZ, 1.0f, maxPos);
                    this.applyDrop(posX, posZ, cells, mapSize, gradient1, gradient2);
                }
            }
        }
    }

    private void applyDrop(float posX, float posY, Cell[] cells, int mapSize, TerrainPos gradient1, TerrainPos gradient2) {
        float dirX = 0.0f;
        float dirY = 0.0f;
        float sediment = 0.0f;
        float speed = this.initialSpeed;
        float water = this.initialWaterVolume;
        gradient1.reset();
        gradient2.reset();
        for (int lifetime = 0; lifetime < this.maxDropletLifetime; ++lifetime) {
            int nodeX = (int)posX;
            int nodeY = (int)posY;
            int dropletIndex = nodeY * mapSize + nodeX;
            float cellOffsetX = posX - (float)nodeX;
            float cellOffsetY = posY - (float)nodeY;
            gradient1.at(cells, mapSize, posX, posY);
            dirX = dirX * 0.05f - gradient1.gradientX * 0.95f;
            dirY = dirY * 0.05f - gradient1.gradientY * 0.95f;
            float len = (float)Math.sqrt(dirX * dirX + dirY * dirY);
            if (Float.isNaN(len)) {
                len = 0.0f;
            }
            if (len != 0.0f) {
                dirX /= len;
                dirY /= len;
            }
            posX += dirX;
            posY += dirY;
            if (dirX == 0.0f && dirY == 0.0f || posX < 0.0f || posX >= (float)(mapSize - 1) || posY < 0.0f || posY >= (float)(mapSize - 1)) {
                return;
            }
            float newHeight = gradient2.at(cells, mapSize, posX, posY).height;
            float deltaHeight = newHeight - gradient1.height;
            float sedimentCapacity = Math.max(-deltaHeight * speed * water * 4.0f, 0.01f);
            if (sediment > sedimentCapacity || deltaHeight > 0.0f) {
                float amountToDeposit = deltaHeight > 0.0f ? Math.min(deltaHeight, sediment) : (sediment - sedimentCapacity) * this.depositSpeed;
                sediment -= amountToDeposit;
                this.deposit(cells[dropletIndex], amountToDeposit * (1.0f - cellOffsetX) * (1.0f - cellOffsetY));
                this.deposit(cells[dropletIndex + 1], amountToDeposit * cellOffsetX * (1.0f - cellOffsetY));
                this.deposit(cells[dropletIndex + mapSize], amountToDeposit * (1.0f - cellOffsetX) * cellOffsetY);
                this.deposit(cells[dropletIndex + mapSize + 1], amountToDeposit * cellOffsetX * cellOffsetY);
            } else {
                float amountToErode = Math.min((sedimentCapacity - sediment) * this.erodeSpeed, -deltaHeight);
                for (int brushPointIndex = 0; brushPointIndex < this.erosionBrushIndices[dropletIndex].length; ++brushPointIndex) {
                    int nodeIndex = this.erosionBrushIndices[dropletIndex][brushPointIndex];
                    Cell cell = cells[nodeIndex];
                    float brushWeight = this.erosionBrushWeights[dropletIndex][brushPointIndex];
                    float weighedErodeAmount = amountToErode * brushWeight;
                    float deltaSediment = Math.min(cell.value, weighedErodeAmount);
                    this.erode(cell, deltaSediment);
                    sediment += deltaSediment;
                }
            }
            speed = (float)Math.sqrt(speed * speed + deltaHeight * 3.0f);
            water *= 0.99f;
            if (!Float.isNaN(speed)) continue;
            speed = 0.0f;
        }
    }

    private void initBrushes(int size, int radius) {
        int[] xOffsets = new int[radius * radius * 4];
        int[] yOffsets = new int[radius * radius * 4];
        float[] weights = new float[radius * radius * 4];
        float weightSum = 0.0f;
        int addIndex = 0;
        for (int i = 0; i < this.erosionBrushIndices.length; ++i) {
            int centreX = i % size;
            int centreY = i / size;
            if (centreY <= radius || centreY >= size - radius || centreX <= radius + 1 || centreX >= size - radius) {
                weightSum = 0.0f;
                addIndex = 0;
                for (int y = -radius; y <= radius; ++y) {
                    for (int x = -radius; x <= radius; ++x) {
                        float sqrDst = x * x + y * y;
                        if (!(sqrDst < (float)(radius * radius))) continue;
                        int coordX = centreX + x;
                        int coordY = centreY + y;
                        if (coordX < 0 || coordX >= size || coordY < 0 || coordY >= size) continue;
                        float weight = 1.0f - (float)Math.sqrt(sqrDst) / (float)radius;
                        weightSum += weight;
                        weights[addIndex] = weight;
                        xOffsets[addIndex] = x;
                        yOffsets[addIndex] = y;
                        ++addIndex;
                    }
                }
            }
            int numEntries = addIndex;
            this.erosionBrushIndices[i] = new int[numEntries];
            this.erosionBrushWeights[i] = new float[numEntries];
            for (int j = 0; j < numEntries; ++j) {
                this.erosionBrushIndices[i][j] = (yOffsets[j] + centreY) * size + xOffsets[j] + centreX;
                this.erosionBrushWeights[i][j] = weights[j] / weightSum;
            }
        }
    }

    private void deposit(Cell cell, float amount) {
        if (!cell.erosionMask) {
            float change = this.modifier.modify(cell, amount);
            cell.value += change;
            cell.sediment += change;
        }
    }

    private void erode(Cell cell, float amount) {
        if (!cell.erosionMask) {
            float change = this.modifier.modify(cell, amount);
            cell.value -= change;
            cell.erosion -= change;
        }
    }

    public static IntFunction<Erosion> factory(GeneratorContext context) {
        return new Factory(context.seed.root(), context.settings.filters, context.levels);
    }

    private static class Factory implements IntFunction<Erosion> {
        private static final int SEED_OFFSET = 12768;
        private final int seed;
        private final Modifier modifier;
        private final FilterSettings.Erosion settings;

        private Factory(int seed, FilterSettings filters, Levels levels) {
            this.seed = seed + SEED_OFFSET;
            this.settings = filters.erosion.copy();
            this.modifier = Modifier.range(levels.ground, levels.ground(15));
        }

        @Override
        public Erosion apply(int size) {
            return new Erosion(this.seed, size, this.settings, this.modifier);
        }
    }

    private static class TerrainPos {
        private float height;
        private float gradientX;
        private float gradientY;

        private TerrainPos() {
        }

        private TerrainPos at(Cell[] nodes, int mapSize, float posX, float posY) {
            int coordX = (int)posX;
            int coordY = (int)posY;
            float x = posX - (float)coordX;
            float y = posY - (float)coordY;
            int nodeIndexNW = coordY * mapSize + coordX;
            float heightNW = nodes[nodeIndexNW].value;
            float heightNE = nodes[nodeIndexNW + 1].value;
            float heightSW = nodes[nodeIndexNW + mapSize].value;
            float heightSE = nodes[nodeIndexNW + mapSize + 1].value;
            this.gradientX = (heightNE - heightNW) * (1.0f - y) + (heightSE - heightSW) * y;
            this.gradientY = (heightSW - heightNW) * (1.0f - x) + (heightSE - heightNE) * x;
            this.height = heightNW * (1.0f - x) * (1.0f - y) + heightNE * x * (1.0f - y) + heightSW * (1.0f - x) * y + heightSE * x * y;
            return this;
        }

        private void reset() {
            this.height = 0.0f;
            this.gradientX = 0.0f;
            this.gradientY = 0.0f;
        }
    }
}
