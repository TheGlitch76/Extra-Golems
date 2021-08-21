package com.mcmoddev.golems.container;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcmoddev.golems.ExtraGolems;
import com.mcmoddev.golems.container.behavior.GolemBehavior;
import com.mcmoddev.golems.container.behavior.GolemBehaviors;
import com.mcmoddev.golems.container.client.GolemRenderSettings;
import com.mcmoddev.golems.entity.GolemBase;
import com.mcmoddev.golems.util.ResourcePair;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class stores characteristics and other vital information
 * about a single entity. These attributes are then referenced from
 * {@link GolemBase} as needed.
 * Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 **/
public final class GolemContainer {
  
  public static final GolemContainer EMPTY = new GolemContainer(
      AttributeSettings.EMPTY, SwimMode.SINK, 0, 0, true, SoundEvents.STONE_STEP, Optional.empty(), 
      Lists.newArrayList(), Maps.newHashMap(), Optional.of(MultitextureSettings.EMPTY), Lists.newArrayList());

  public static final Codec<GolemContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      AttributeSettings.CODEC.fieldOf("attributes").forGetter(GolemContainer::getAttributes),
      SwimMode.CODEC.optionalFieldOf("swim_ability", SwimMode.SINK).forGetter(GolemContainer::getSwimAbility),
      Codec.INT.optionalFieldOf("glow", 0).forGetter(GolemContainer::getMaxLightLevel),
      Codec.INT.optionalFieldOf("power", 0).forGetter(GolemContainer::getMaxPowerLevel),
      Codec.BOOL.optionalFieldOf("hidden", false).forGetter(GolemContainer::isHidden),
      SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.STONE_STEP).forGetter(GolemContainer::getSound),
      ParticleTypes.CODEC.optionalFieldOf("particle").forGetter(GolemContainer::getParticle),
      Codec.either(ResourcePair.CODEC, ResourcePair.CODEC.listOf())
      .xmap(either -> either.map(ImmutableList::of, Function.identity()), 
            list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list))
      .optionalFieldOf("blocks", Lists.newArrayList()).forGetter(GolemContainer::getBlocksRaw),
      Codec.unboundedMap(ResourcePair.CODEC, Codec.DOUBLE).optionalFieldOf("heal_items", Maps.newHashMap()).forGetter(GolemContainer::getHealItemsRaw),
      MultitextureSettings.CODEC.optionalFieldOf("multitexture").forGetter(GolemContainer::getMultitexture),
      CompoundTag.CODEC.listOf().optionalFieldOf("behavior", Lists.newArrayList(new CompoundTag())).forGetter(GolemContainer::getBehaviorsRaw)
    ).apply(instance, GolemContainer::new));
  
  private final AttributeSettings attributes;
  private final SwimMode swimAbility;
  private final int glow;
  private final int power;
  private final boolean hidden;
  private final SoundEvent sound;
  private final Optional<ParticleOptions> particle;
  
  private final List<ResourcePair> blocksRaw;
  private final ImmutableSet<ResourceLocation> blocks;
  private final ImmutableSet<ResourceLocation> blockTags;
  
  private final Map<ResourcePair, Double> healItemsRaw;
  private final ImmutableMap<ResourceLocation, Double> healItems;
  private final ImmutableMap<ResourceLocation, Double> healItemTags;
  
  private final List<CompoundTag> behaviorsRaw;
  private final ImmutableMap<ResourceLocation, ImmutableList<GolemBehavior>> behaviors;
  
  private final Optional<MultitextureSettings> multitexture;

  private GolemContainer(AttributeSettings attributes, SwimMode swimAbility, int glow, int power, boolean hidden,
      SoundEvent sound, Optional<ParticleOptions> particle, List<ResourcePair> blocksRaw, Map<ResourcePair, Double> healItemsRaw,
      Optional<MultitextureSettings> multitexture, List<CompoundTag> goalsRaw) {
    this.attributes = attributes;
    this.swimAbility = swimAbility;
    this.glow = glow;
    this.power = power;
    this.hidden = hidden;
    this.sound = sound;
    this.particle = particle;
    this.blocksRaw = blocksRaw;
    this.healItemsRaw = healItemsRaw;
    this.multitexture = multitexture;
    this.behaviorsRaw = goalsRaw;
    
    // populate blocks and block tags
    ImmutableSet.Builder<ResourceLocation> bblocks = ImmutableSet.builder();
    ImmutableSet.Builder<ResourceLocation> bblockTags = ImmutableSet.builder();
    if(this.multitexture.isPresent()) {
      // when using multitexture, add all blocks from each entry
      this.multitexture.get().getBlockMap().keySet().forEach(pair -> {
        // add to correct set for block name or block tag
        if(pair.flag()) bblockTags.add(pair.resource());
        else bblocks.add(pair.resource());
      });
    } else {
      // when not using multitexture, add all blocks from blocks list
      this.blocksRaw.forEach(pair -> {
        // add to correct set for block name or block tag
        if(pair.flag()) bblockTags.add(pair.resource());
        else bblocks.add(pair.resource());
      });
    }
    // build block maps
    this.blocks = bblocks.build();
    this.blockTags = bblockTags.build();
    
    // populate heal item and heal item tags
    ImmutableMap.Builder<ResourceLocation, Double> bhealItems = ImmutableMap.builder();
    ImmutableMap.Builder<ResourceLocation, Double> bhealItemTags = ImmutableMap.builder();
    this.healItemsRaw.forEach((s, d) -> {
      // add to correct map for item name or item tag
      if(s.flag()) bhealItemTags.put(s.resource(), d);
      else bhealItems.put(s.resource(), d);
    });
    // build heal item maps
    this.healItems = bhealItems.build();
    this.healItemTags = bhealItemTags.build();
    
    // populate behaviors
    Map<ResourceLocation, List<GolemBehavior>> bbehaviors = new HashMap<>();
    ResourceLocation goalId;
    Optional<GolemBehavior> behavior;
    for(final CompoundTag goalTag : goalsRaw) {
      if(goalTag.contains("type", Tag.TAG_STRING)) {
        // determine the GolemBehavior type
        goalId = new ResourceLocation(goalTag.getString("type"));
        // create the behavior using the tag
        behavior = GolemBehaviors.create(goalId, goalTag);
        if(behavior.isPresent()) {
          // create list if necessary
          bbehaviors.putIfAbsent(goalId, Lists.newArrayList());
          // add behavior to existing list
          bbehaviors.get(goalId).add(behavior.get());
        } else ExtraGolems.LOGGER.warn("GolemContainer: behavior '" + goalId + "' does not exist!");
      }
    }
    // build behavior map
    ImmutableMap.Builder<ResourceLocation, ImmutableList<GolemBehavior>> bbehaviors2 = ImmutableMap.builder();
    bbehaviors.forEach((id, list) -> {
      bbehaviors2.put(id, ImmutableList.copyOf(list));
    });
    behaviors = bbehaviors2.build();
  }
  
  ////////// GETTERS //////////

  /** @return the Golem base attributes **/
  public AttributeSettings getAttributes() { return attributes; }
  
  /** @return the {@link SwimMode} of the Golem **/
  public SwimMode getSwimAbility() { return swimAbility; }

  /** @return true if the Golem can swim on top of water **/
  public boolean canSwim() { return this.swimAbility == SwimMode.FLOAT; }

  /** @return true if the Golem should not appear in the entity guide book **/
  public boolean isHidden() { return hidden; }
  
  /** @return a default SoundEvent to play when the Golem moves or is attacked **/
  public SoundEvent getSound() { return sound; }

  /** @return the ambient particle, if any **/
  public Optional<ParticleOptions> getParticle() { return particle; }

  /** @return a List of string representations of blocks or block tags **/
  private List<ResourcePair> getBlocksRaw() { return blocksRaw; }
  
  /** @return a Set of Block IDs that can be used to build the Golem **/
  public ImmutableSet<ResourceLocation> getBlocks() { return blocks; }
  
  /** @return a Set of Block Tags that can be used to build the Golem **/
  public ImmutableSet<ResourceLocation> getBlockTags() { return blockTags; }

  /** @return a Map of string representations of item IDs and tags to heal percentage **/
  private Map<ResourcePair, Double> getHealItemsRaw() { return healItemsRaw; }
  
  /** @return a Map of Item IDs and the heal percentage that item provides **/
  public ImmutableMap<ResourceLocation, Double> getHealItems() { return healItems; }

  /** @return a Map of Item Tags and the heal percentage that Tag provides **/
  public ImmutableMap<ResourceLocation, Double> getHealItemTags() { return healItemTags; }
  
  /** @return a CompoundTag representation of the Golem behaviors **/
  private List<CompoundTag> getBehaviorsRaw() { return behaviorsRaw; }

  /** @return an ImmutableMap of Behavior IDs and GolemBehaviors **/
  public ImmutableMap<ResourceLocation, ImmutableList<GolemBehavior>> getBehaviors() { return behaviors; }
  
  /** @return an Optional containing multitexture settings if applicable **/
  public Optional<MultitextureSettings> getMultitexture() { return multitexture; }
  
  /** @return the Golem's base light level **/
  public int getMaxLightLevel() { return glow; }
  
  /** @return the Golem's base redstone power level **/
  public int getMaxPowerLevel() { return power; }

  // CONVENIENCE METHODS //
  
  /** @return true if at least one block is registered for the Golem **/
  public boolean hasBlocks() {
    // check if blocks is not empty
    if(blocks.size() > 0) {
      return true;
    }
    // check if at least one block tag is not empty
    for(final ResourceLocation tagId : blockTags) {
      if(BlockTags.getAllTags().getTagOrEmpty(tagId).getValues().size() > 0) {
        return true;
      }
    }
    // neither blocks nor blockTags contained anything
    return false;
  }
  
  /**
   * Resolve block tags and join them to the set of blocks
   * @return a Set representing all blocks that can build the Golem
   */
  public Set<Block> getAllBlocks() {
    return getAllBlocks(blocks, blockTags);
  }
 
  public boolean matches(Block body, Block legs, Block arm1, Block arm2) {
    final Set<Block> blocks = getAllBlocks();
    return blocks.contains(body) && blocks.contains(legs) && blocks.contains(arm1) && blocks.contains(arm2);
  }

  /**
   * @param item the item used to heal the Golem
   * @return the amount of health to restore (can be 0)
   */
  public double getHealAmount(final Item item) {
    // first check the item ID map
    final ResourceLocation id = item.getRegistryName();
    if(healItems.containsKey(id)) {
      return healItems.get(id);
    }
    // next check the item Tag map
    for(final ResourceLocation tag : item.getTags()) {
      if(healItemTags.containsKey(tag)) {
        return healItemTags.get(tag);
      }
    }
    // no heal item matches
    return 0.0D;
  }
  
  /**
   * @param name the behavior ID
   * @return true if the requested behavior is present in the Golem
   */
  public boolean hasBehavior(final ResourceLocation name) {
    return this.getBehaviors().getOrDefault(name, ImmutableList.of()).isEmpty();
  }
  
  /** @return a new attribute map supplier for the Golem **/
  public Supplier<AttributeSupplier.Builder> getAttributeSupplier() {
    return () -> GolemBase.createMobAttributes()
         .add(Attributes.MAX_HEALTH, this.attributes.getHealth())
         .add(Attributes.MOVEMENT_SPEED, this.attributes.getSpeed())
         .add(Attributes.KNOCKBACK_RESISTANCE, this.attributes.getKnockbackResist())
         .add(Attributes.ATTACK_KNOCKBACK, this.attributes.getAttackKnockback())
         .add(Attributes.ARMOR, this.attributes.getArmor())
         .add(Attributes.ATTACK_DAMAGE, this.attributes.getAttack());
   }
  
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("GolemContainer: ");
    b.append("attributes[").append(attributes).append("] ");
    b.append("swim_ability[").append(swimAbility).append("] ");
    b.append("hidden[").append(hidden).append("] ");
    b.append("sound[").append(sound.getLocation()).append("] ");
    b.append("particle[").append(particle).append("] ");
    b.append("blocks[").append(blocksRaw).append("] ");
    b.append("healItems[").append(healItemsRaw).append("] ");
    b.append("behavior[").append(behaviors).append("] ");
    b.append("multitexture[").append(multitexture).append("] ");
    return b.toString();
  }
  
  public static Set<Block> getAllBlocks(Collection<ResourceLocation> blocks, Collection<ResourceLocation> blockTags) {
    final Set<Block> all = new HashSet<>();
    // add blocks from ID
    Block tmpBlock;
    for(final ResourceLocation blockId : blocks) {
      tmpBlock = ForgeRegistries.BLOCKS.getValue(blockId);
      if(tmpBlock != null) {
        all.add(tmpBlock);
      }
    }
    // add blocks from Tag
    for(final ResourceLocation tagId : blockTags) {
      all.addAll(BlockTags.getAllTags().getTagOrEmpty(tagId).getValues());
    }
    return all;
  }

  /**
   * There are three distinct behaviors when a entity is in contact with water:
   * <br>
   * {@code SINK} = the entity does not swim at all <br>
   * {@code FLOAT} = the entity swims on top of water <br>
   * {@code SWIM} = the entity navigates up and down in the water
   **/
  public static enum SwimMode implements StringRepresentable {
    SINK("sink"), 
    FLOAT("float"), 
    SWIM("swim");
    
    private static final Map<String, SwimMode> valueMap = new HashMap<>();
    static {
      for(final SwimMode t : values()) {
        valueMap.put(t.getSerializedName(), t);
      }
    }
    
    public static final Codec<SwimMode> CODEC = Codec.STRING.comapFlatMap(
        s -> DataResult.success(SwimMode.getByName(s)), SwimMode::getSerializedName).stable();
    
    private final String name;
    
    private SwimMode(final String nameIn) {
      this.name = nameIn;
    }

    @Override
    public String getSerializedName() {  return name; }
    
    /**
     * @param nameIn the name representation of the SwimMode
     * @return the SwimMode with this name, or SINK as a fallback
     */
    public static SwimMode getByName(final String nameIn) {
      return valueMap.getOrDefault(nameIn, SINK);
    }
  }
}