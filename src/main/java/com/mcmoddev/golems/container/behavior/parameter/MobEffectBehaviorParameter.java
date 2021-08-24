package com.mcmoddev.golems.container.behavior.parameter;

import javax.annotation.concurrent.Immutable;

import com.mcmoddev.golems.entity.GolemBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

@Immutable
public class MobEffectBehaviorParameter extends BehaviorParameter {

  private final Target target;
  private final double chance;
  private final MobEffectInstance[] effects;
  
  public MobEffectBehaviorParameter(final CompoundTag tag) {
    super();
    this.target = Target.getByName(tag.getString("target"));
    this.chance = tag.getDouble("chance");
    this.effects = readEffectArray(tag.getList("effects", Tag.TAG_COMPOUND));
  }
  
  public Target getTarget() { return target; }
  
  public double getChance() { return chance; }
  
  public MobEffectInstance[] getEffects() { return effects; }
  
  public void apply(GolemBase self, LivingEntity other) {
    if(effects.length > 0 && self.getRandom().nextFloat() < chance) {
      LivingEntity effectTarget = (target == Target.SELF) ? self : other;
      if(effectTarget != null) {
        // apply a randomly chosen mob effects
        effectTarget.addEffect(effects[self.getRandom().nextInt(effects.length)]);
      }
    }
  }
}
