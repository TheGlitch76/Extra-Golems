package com.mcmoddev.golems;

import com.mcmoddev.golems.render.GolemModel;
import com.mcmoddev.golems.render.GolemRenderType;
import com.mcmoddev.golems.render.GolemRenderer;
import com.mcmoddev.golems.screen.DispenserGolemScreen;
import com.mcmoddev.golems.screen.guide_book.GolemBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class EGClientEvents {

	public static void register() {
		MinecraftForge.EVENT_BUS.register(EGClientEvents.ForgeHandler.class);
		FMLJavaModLoadingContext.get().getModEventBus().register(EGClientEvents.ModHandler.class);
		ModHandler.addResources();
	}

	public static class ModHandler {

		@SubscribeEvent
		public static void setupClient(final FMLClientSetupEvent event) {
			event.enqueueWork(() -> MenuScreens.register(EGRegistry.DISPENSER_GOLEM_MENU.get(), DispenserGolemScreen::new));
		}

		@SubscribeEvent
		public static void registerEntityLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
			event.registerLayerDefinition(GolemRenderer.GOLEM_MODEL_RESOURCE, GolemModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
			event.registerEntityRenderer(EGRegistry.GOLEM.get(), GolemRenderer::new);
		}

		public static void addResources() {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			if (resourceManager instanceof ReloadableResourceManager) {
				// reload dynamic texture map
				((ReloadableResourceManager) resourceManager).registerReloadListener(new SimplePreparableReloadListener<ModelBakery>() {
					@Override
					protected ModelBakery prepare(ResourceManager arg0, ProfilerFiller arg1) {
						return null;
					}

					@Override
					protected void apply(ModelBakery arg0, ResourceManager arg1, ProfilerFiller arg2) {
						GolemRenderType.reloadDynamicTextureMap();
					}

					@Override
					public String getName() {
						return "Extra Golems textures";
					}
				});
			}
		}
	}

	public static final class ForgeHandler {

		@SubscribeEvent
		public static void onDatapackSync(final OnDatapackSyncEvent event) {
			EGClientEvents.ForgeHandler.onClearGolemModels();
		}

		public static void onClearGolemModels() {
			GolemRenderType.clearLoadedRenderSettings();
		}

		public static void loadBookGui(final Player playerIn, final ItemStack itemstack) {
			// only load client-side, of course
			if (!playerIn.getCommandSenderWorld().isClientSide()) {
				return;
			}
			// open the gui
			Minecraft.getInstance().setScreen(new GolemBookScreen(playerIn, itemstack));
		}
	}
}
