package com.mcmoddev.golems.container.behavior;

import com.mcmoddev.golems.entity.GolemBase;
import com.mcmoddev.golems.menu.PortableCraftingMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.concurrent.Immutable;
import java.util.List;

/**
 * This behavior allows an entity to open a crafting menu
 * when the player interacts with it
 **/
@Immutable
public class CraftingMenuBehavior extends GolemBehavior {

	public CraftingMenuBehavior(CompoundTag tag) {
		super(tag);
	}

	@Override
	public void onMobInteract(final GolemBase entity, final Player player, final InteractionHand hand) {
		if (!player.isCrouching() && player instanceof ServerPlayer) {
			// display crafting grid by sending request to server
			NetworkHooks.openScreen((ServerPlayer) player, new PortableCraftingMenu.Provider());
			player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
			player.swing(hand);
		}
	}

	@Override
	public void onAddDescriptions(List<Component> list) {
		list.add(Component.translatable("entitytip.crafting_menu").withStyle(ChatFormatting.BLUE));
	}
}
