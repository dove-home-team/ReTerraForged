package raccoonman.reterraforged.common.registries;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import raccoonman.reterraforged.common.level.levelgen.surface.SurfaceGradientRuleSource;
import raccoonman.reterraforged.platform.registries.RegistryUtil;

public final class RTFSurfaceRules {

	public static void bootstrap() {
		register("surface_gradient", SurfaceGradientRuleSource.CODEC);
	}
	
	private static void register(String name, Codec<? extends RuleSource> value) {
		RegistryUtil.register(BuiltInRegistries.MATERIAL_RULE, name, value);
	}
}