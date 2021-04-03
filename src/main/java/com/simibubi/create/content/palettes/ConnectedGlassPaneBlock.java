// PORTED CREATE SOURCE

package com.simibubi.create.content.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ConnectedGlassPaneBlock extends GlassPaneBlock {
	public ConnectedGlassPaneBlock(Settings builder) {
		super(builder);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		if (side.getAxis()
			.isVertical())
			return adjacentBlockState == state;
		return super.isSideInvisible(state, adjacentBlockState, side);
	}
}
