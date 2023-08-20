package raccoonman.reterraforged.common.level.levelgen.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import raccoonman.reterraforged.common.noise.Noise;
import raccoonman.reterraforged.common.noise.util.NoiseUtil;

public record MapRange(Noise source, float min, float max) implements Noise {
	public static final Codec<MapRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Noise.DIRECT_CODEC.fieldOf("source").forGetter(MapRange::source),
		Codec.FLOAT.fieldOf("min").forGetter(MapRange::min),
		Codec.FLOAT.fieldOf("max").forGetter(MapRange::max)
	).apply(instance, MapRange::new));
	
	@Override
	public Codec<MapRange> codec() {
		return CODEC;
	}

	@Override
	public float getValue(float x, float y, int seed) {
		return NoiseUtil.map(this.source.getValue(x, y, seed), this.min, this.max);
	}
}