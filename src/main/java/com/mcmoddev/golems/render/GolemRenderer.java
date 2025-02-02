package com.mcmoddev.golems.render;

import com.mcmoddev.golems.ExtraGolems;
import com.mcmoddev.golems.container.render.GolemRenderSettings;
import com.mcmoddev.golems.entity.GolemBase;
import com.mcmoddev.golems.render.layer.ColoredTextureLayer;
import com.mcmoddev.golems.render.layer.GolemBannerLayer;
import com.mcmoddev.golems.render.layer.GolemCrackinessLayer;
import com.mcmoddev.golems.render.layer.GolemFlowerLayer;
import com.mcmoddev.golems.render.layer.GolemKittyLayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class GolemRenderer<T extends GolemBase> extends MobRenderer<T, GolemModel<T>> {

	public static final ModelLayerLocation GOLEM_MODEL_RESOURCE = new ModelLayerLocation(new ResourceLocation(ExtraGolems.MODID, "golem"), "main");

	protected static final ResourceLocation boneTexture = new ResourceLocation(ExtraGolems.MODID, "textures/entity/golem/bone_skeleton.png");
	protected static final ResourceLocation specialTexture = new ResourceLocation(ExtraGolems.MODID, "textures/entity/golem/special.png");
	protected static final ResourceLocation specialTexture2 = new ResourceLocation(ExtraGolems.MODID, "textures/entity/golem/special2.png");
	protected static final ResourceLocation specialTexture3 = new ResourceLocation(ExtraGolems.MODID, "textures/entity/golem/special3.png");

	protected boolean isAlphaLayer;

	/**
	 * @param context the entity render manager
	 **/
	public GolemRenderer(final EntityRendererProvider.Context context) {
		super(context, new GolemModel<>(context.bakeLayer(GOLEM_MODEL_RESOURCE)), 0.5F);
		this.addLayer(new ColoredTextureLayer<>(this, context.getModelSet()));
		this.addLayer(new GolemCrackinessLayer<>(this));
		this.addLayer(new GolemFlowerLayer<>(this));
		this.addLayer(new GolemKittyLayer<>(this));
		this.addLayer(new GolemBannerLayer<>(this));
	}

	@Override
	public void render(final T golem, final float entityYaw, final float partialTicks, final PoseStack matrixStackIn,
					   final MultiBufferSource bufferIn, final int packedLightIn) {
		if (golem.isInvisible()) {
			return;
		}
		// get render settings
		GolemRenderSettings settings = GolemRenderType.loadRenderSettings(golem.level.registryAccess(), golem.getMaterial());
		matrixStackIn.pushPose();
		// scale
		if (golem.isBaby()) {
			float scaleChild = 0.5F;
			matrixStackIn.scale(scaleChild, scaleChild, scaleChild);
		}
		// settings
		this.getModel().setSettings(settings, golem);
		// transparency flag
		isAlphaLayer = settings.isTranslucent();
		if (isAlphaLayer) {
			RenderSystem.enableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
		}
		// packed light
		final int packedLight = settings.getBaseLight().orElse(settings.getBaseLight().orElse(false)) ? 15728880 : packedLightIn;
		// render the entity
		super.render(golem, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLight);
		if (isAlphaLayer) {
			RenderSystem.disableBlend();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
		matrixStackIn.popPose();
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	public ResourceLocation getTextureLocation(final T golem) {
		GolemRenderSettings settings = getModel().getSettings();
		ResourceLocation texture = settings.getBase(golem).resource();
		boolean disableLayers = false;
		// special cases
		if (ExtraGolems.CONFIG.halloween() && isNightTime(golem)) {
			texture = boneTexture;
			disableLayers = true;
		} else if (golem.hasCustomName()) {
			final String name = ChatFormatting.stripFormatting(golem.getName().getString());
			if ("ganon".equalsIgnoreCase(name) || "ganondorf".equalsIgnoreCase(name)) {
				texture = specialTexture;
				disableLayers = true;
			} else if ("cookie".equalsIgnoreCase(name)) {
				texture = specialTexture2;
				disableLayers = true;
			} else if ("yeti".equalsIgnoreCase(name)) {
				texture = specialTexture3;
				disableLayers = true;
			}
		}
		this.getModel().disableLayers(disableLayers);
		return texture;
	}

	@Override
	@Nullable
	protected RenderType getRenderType(final T golem, boolean isVisible, boolean isVisibleToPlayer, boolean isGlowing) {
		GolemRenderSettings settings = getModel().getSettings();
		ResourceLocation texture = this.getTextureLocation(golem);
		ResourceLocation template = settings.getBaseTemplate();
		boolean dynamic = isDynamic(golem, texture, settings);
		if (isVisible || isVisibleToPlayer || isAlphaLayer) {
			return GolemRenderType.getGolemTranslucent(texture, template, dynamic);
		} else if (isGlowing) {
			return GolemRenderType.getGolemOutline(texture, template, dynamic);
		} else {
			return GolemRenderType.getGolemCutout(texture, template, dynamic);
		}
	}

	protected static boolean isSpecial(final ResourceLocation texture) {
		return texture == boneTexture || texture == specialTexture || texture == specialTexture2 || texture == specialTexture3;
	}

	protected static <T extends GolemBase> boolean isDynamic(final T entity, final ResourceLocation texture, final GolemRenderSettings settings) {
		return !isSpecial(texture) && !settings.getBase(entity).flag();
	}

	public static boolean isNightTime(final GolemBase golem) {
		final long time = golem.level.getDayTime() % 24000L;
		return time > 13000L && time < 23000L;
	}
}
