package com.simibubi.create.content.palettes;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class WindowBlock extends ConnectedGlassBlock {

	public WindowBlock(Properties p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock
			? (RenderTypeLookup.getBlockLayer(state) != RenderType.getTranslucent() && side.getAxis()
				.isHorizontal() || state.getBlock() == adjacentBlockState.getBlock())
			: super.isSideInvisible(state, adjacentBlockState, side);
	}

}
