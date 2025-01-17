package com.simibubi.create.lib.utility;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class StickinessUtil {
	public static boolean canStickTo(BlockState state, BlockState other) {
		if (state.getBlock() == Blocks.HONEY_BLOCK && other.getBlock() == Blocks.SLIME_BLOCK) return false;
		if (state.getBlock() == Blocks.SLIME_BLOCK && other.getBlock() == Blocks.HONEY_BLOCK) return false;
		return (state.getBlock() == Blocks.SLIME_BLOCK || state.getBlock() == Blocks.HONEY_BLOCK) ||
				(other.getBlock() == Blocks.SLIME_BLOCK || other.getBlock() == Blocks.HONEY_BLOCK);
	}
}
