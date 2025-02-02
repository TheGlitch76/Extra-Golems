package com.mcmoddev.golems.block;

import com.mcmoddev.golems.entity.GolemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GlowBlock extends UtilityBlock {

	public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light", 0, 15);
	/* Default value for TICK_RATE. Not necessary to define through config. */
	public static final int UPDATE_TICKS = 6;

	public GlowBlock(final Material m, final float defaultLight) {
		super(Properties.of(m).randomTicks().lightLevel(state -> state.getValue(LIGHT_LEVEL)), UPDATE_TICKS);
		int light = (int) (defaultLight * 15.0F);
		this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_LEVEL, light));
	}

	@Override
	public void tick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final RandomSource random) {
		// make a slightly expanded AABB to check for the entity
		final AABB toCheck = new AABB(pos).inflate(0.5D);
		// we'll probably only ever get one entity, but it doesn't hurt to be safe and
		// check them all
		final List<GolemBase> list = worldIn.getEntitiesOfClass(GolemBase.class, toCheck);
		boolean hasLightGolem = !list.isEmpty() && hasLightGolem(list);

		if (hasLightGolem) {
			// light entity is nearby, schedule another update
			worldIn.scheduleTick(pos, state.getBlock(), this.tickRate);
		} else {
			this.remove(worldIn, state, pos, 3);
		}
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIGHT_LEVEL);
	}

	/**
	 * @return if the given list contains any golems for whom
	 * {@link GolemBase#isProvidingLight()} returns true
	 **/
	public static boolean hasLightGolem(final List<GolemBase> golems) {
		for (GolemBase g : golems) {
			if (g.isProvidingLight()) {
				return true;
			}
		}
		return false;
	}
}
