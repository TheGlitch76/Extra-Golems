package com.mcmoddev.golems.renders;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mcmoddev.golems.ExtraGolems;
import com.mcmoddev.golems.util.GolemNames;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.client.renderer.RenderStateShard.CullStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public class GolemRenderType extends RenderType {
  
  private static final Map<ResourceLocation, DynamicTextureState> dynamicTextureMap = new HashMap<>();
  
  public static final ResourceLocation TEMPLATE = new ResourceLocation(ExtraGolems.MODID, "textures/entity/layer/template.png");
  public static final ResourceLocation MUSHROOM_TEMPLATE = new ResourceLocation(ExtraGolems.MODID, "textures/entity/" + GolemNames.MUSHROOM_GOLEM + "/template.png");
  public static final ResourceLocation WOOL_TEMPLATE = new ResourceLocation(ExtraGolems.MODID, "textures/entity/" + GolemNames.WOOL_GOLEM + "/template.png");

  public GolemRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode glQuads, int i2, boolean b1,
      boolean b2, Runnable r1, Runnable r2) {
    super(name, vertexFormat, glQuads, i2, b1, b2, r1, r2);
  }
  
  public static void reloadDynamicTextureMap() {
    final Map<ResourceLocation, DynamicTextureState> copy = new HashMap<>(dynamicTextureMap);
    copy.entrySet().forEach(e -> dynamicTextureMap.put(e.getKey(), new DynamicTextureState(e.getKey(), e.getValue().templateImage)));
  }
  
  private static TextureStateShard getTextureState(final ResourceLocation texture, final ResourceLocation template) {
    // lazy-load the texture state
    if(!dynamicTextureMap.containsKey(texture)) {
      dynamicTextureMap.put(texture, new DynamicTextureState(texture, template));
    }
    return dynamicTextureMap.get(texture).state;
  }
  
  public static RenderType getGolemCutout(final ResourceLocation texture, final boolean dynamic) {
    return getGolemCutout(texture, TEMPLATE, dynamic);
  }
  
  public static RenderType getGolemCutout(final ResourceLocation texture, final ResourceLocation template, final boolean dynamic) {        
    if(!dynamic) {
      return RenderType.entityCutoutNoCull(texture);
    }
    // make dynamic cutout type
    return create("golem_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .createCompositeState(true));
  }
  
  public static RenderType getGolemTransparent(final ResourceLocation texture, final boolean dynamic) {
    if(!dynamic) {
      return RenderType.entityTranslucent(texture);
    }
    // make dynamic translucent type
    return create("golem_transparent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, 
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY).createCompositeState(true));
  }
  
  public static RenderType getGolemOutline(final ResourceLocation texture, final boolean dynamic) {
    if(!dynamic) {
      return RenderType.outline(texture);
    }
    // make dynamic outline type
    return create("golem_outline", 
        DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, false, false,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_OUTLINE_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
            .setCullState(CullStateShard.NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .setOutputState(OUTLINE_TARGET)
            .createCompositeState(RenderType.OutlineProperty.IS_OUTLINE));
  }
}
