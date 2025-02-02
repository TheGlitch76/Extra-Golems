package com.mcmoddev.golems.container.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mcmoddev.golems.util.ResourcePair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;

import java.util.Map;

public class MultitextureRenderSettings {

	public static final MultitextureRenderSettings EMPTY = new MultitextureRenderSettings(Maps.newHashMap());

	public static final Codec<MultitextureRenderSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, i -> Integer.toString(i)), ResourcePair.CODEC).fieldOf("base_map").forGetter(MultitextureRenderSettings::getBaseMapRaw)
	).apply(instance, MultitextureRenderSettings::new));

	private final ImmutableMap<Integer, ResourcePair> baseMapRaw;
	private ImmutableMap<Integer, ResourcePair> baseMap;

	private MultitextureRenderSettings(Map<Integer, ResourcePair> baseMapRaw) {
		this.baseMapRaw = ImmutableMap.copyOf(baseMapRaw);
		this.baseMap = ImmutableMap.of();
	}


	/**
	 * @return a map of unresolved texture IDs and ResourceLocation/Boolean pairs
	 * @see #getBaseMap()
	 **/
	public Map<Integer, ResourcePair> getBaseMapRaw() {
		return baseMapRaw;
	}

	/**
	 * @return a map of texture IDs and ResourceLocation/Boolean pairs.
	 * The ResourceLocation is a block texture if the Boolean is true.
	 * The ResourceLocation is a prefab if the Boolean is false.
	 **/
	public Map<Integer, ResourcePair> getBaseMap() {
		return baseMap;
	}

	@Override
	public String toString() {
		return "MultitextureRenderSettings: ".concat("base_map[").concat(baseMapRaw.toString()).concat("] ");
	}

	public static class ClientUtils {

		public static void loadMultitextureSettings(final MultitextureRenderSettings settings) {
			ImmutableMap.Builder<Integer, ResourcePair> builder = ImmutableMap.builder();
			settings.baseMapRaw.forEach((num, pair) -> {
				builder.put(num, GolemRenderSettings.ClientUtils.buildPreferredTexture(ImmutableList.of(pair)));
			});
			settings.baseMap = builder.build();
		}
	}
}
