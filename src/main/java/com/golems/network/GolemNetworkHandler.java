package com.golems.network;

import com.golems.main.ExtraGolems;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class GolemNetworkHandler {
	
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ExtraGolems.MODID);
	
	public static void init() {
		INSTANCE.registerMessage(PacketTriggerGolemEntryLoad.PacketHandler.class, PacketTriggerGolemEntryLoad.class, 0, Side.CLIENT);
	}
}