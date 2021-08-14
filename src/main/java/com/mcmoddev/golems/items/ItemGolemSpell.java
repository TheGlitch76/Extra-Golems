package com.mcmoddev.golems.items;

import com.mcmoddev.golems.GolemItems;
import com.mcmoddev.golems.blocks.BlockGolemHead;
import com.mcmoddev.golems.util.config.ExtraGolemsConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class ItemGolemSpell extends Item {

  /*
   * This behavior enables a Dispenser to convert a Carved Pumpkin into a Golem
   * Head. That's all.
   *
   * public static final IBehaviorDispenseItem DISPENSER_BEHAVIOR = new
   * BehaviorDefaultDispenseItem() {
   * 
   * @Override protected ItemStack dispenseStack(final IBlockSource source, final
   * ItemStack stack) { World world = source.getWorld(); BlockPos blockpos =
   * source.getBlockPos().offset(source.getBlockState().get(BlockDispenser.FACING)
   * ); if (ExtraGolemsConfig.enableUseSpellItem() &&
   * world.getBlockState(blockpos).getBlock() == Blocks.CARVED_PUMPKIN) { if
   * (!world.isRemote) { final EnumFacing facing =
   * world.getBlockState(blockpos).get(BlockHorizontal.HORIZONTAL_FACING);
   * world.setBlockState(blockpos,
   * GolemItems.GOLEM_HEAD.getDefaultState().with(BlockHorizontal.
   * HORIZONTAL_FACING, facing), 3); } stack.shrink(1); } else { return
   * super.dispenseStack(source, stack); } return stack; } };
   */
  public ItemGolemSpell() {
    super(new Item.Properties().stacksTo(64).tab(CreativeModeTab.TAB_MISC));
    // dispenser behavior TODO: NOT WORKING
    // BlockDispenser.registerDispenseBehavior(this, DISPENSER_BEHAVIOR);
  }

  @Override
  public InteractionResult useOn(UseOnContext cxt) {
    if (ExtraGolemsConfig.enableUseSpellItem() && cxt.getClickedPos() != null && cxt.getItemInHand() != null && !cxt.getItemInHand().isEmpty()) {
      final Block b = cxt.getLevel().getBlockState(cxt.getClickedPos()).getBlock();
      if (b == Blocks.CARVED_PUMPKIN || (b == Blocks.PUMPKIN && ExtraGolemsConfig.pumpkinBuildsGolems())) {
        if (!cxt.getLevel().isClientSide()) {
          final Direction facing = cxt.getLevel().getBlockState(cxt.getClickedPos()).getValue(HorizontalDirectionalBlock.FACING);
          cxt.getLevel().setBlock(cxt.getClickedPos(), GolemItems.GOLEM_HEAD.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing), 3);
          BlockGolemHead.trySpawnGolem(cxt.getLevel(), cxt.getClickedPos());
          cxt.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public Component getName(ItemStack stack) {
    return new TranslatableComponent(this.getDescriptionId(stack)).withStyle(ChatFormatting.RED);
  }
}
