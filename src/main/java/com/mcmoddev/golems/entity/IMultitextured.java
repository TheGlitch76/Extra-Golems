package com.mcmoddev.golems.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IMultitextured {
  
  static final String KEY_TEXTURE = "Texture";

  /**
   * Updates the current texture
   * 
   * @param toSet the index of the new texture
   **/
  void setTextureId(final byte toSet);

  /**
   * @return the index of the current texture
   **/
  int getTextureId();

  /**
   * @return the total number of possible textures
   **/
  int getTextureCount();
  
  default void saveTextureId(final CompoundTag tag) {
    tag.putByte(KEY_TEXTURE, (byte) getTextureId());
  }
  
  default void loadTextureId(final CompoundTag tag) {
    setTextureId(tag.getByte(KEY_TEXTURE));
  }

  /**
   * Selects a random texture to apply. Parameters are given in case the golem
   * randomizes texture based on location.
   * 
   * @param world the World
   * @param pos   an approximate position for the golem
   **/
  default void randomizeTexture(final Level world, final BlockPos pos) {
    final byte texture = (byte) world.getRandom().nextInt(Math.max(1, getTextureCount()));
    setTextureId(texture);
  }
  
  /**
   * Updates the texture to the next one in the array
   * @param player the player
   * @param hand the player's hand
   */
  default void cycleTexture(final Player player, final InteractionHand hand) {
    final int incremented = (this.getTextureId() + 1) % this.getTextureCount();
    this.setTextureId((byte) incremented);
    player.swing(hand);
  }
}