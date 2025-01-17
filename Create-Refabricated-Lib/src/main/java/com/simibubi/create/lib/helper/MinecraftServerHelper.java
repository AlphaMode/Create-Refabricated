package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.MinecraftServerAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveFormat;

public final class MinecraftServerHelper {
	public static SaveFormat.LevelSave getAnvilConverterForAnvilFile(MinecraftServer minecraftServer) {
		return get(minecraftServer).create$anvilConverterForAnvilFile();
	}

	private static MinecraftServerAccessor get(MinecraftServer minecraftServer) {
		return MixinHelper.cast(minecraftServer);
	}

	private MinecraftServerHelper() {}
}
