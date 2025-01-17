package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DrillMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(DrillBlock.FACING)
			.getOpposite());
	}

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.of(context.state.get(DrillBlock.FACING)
			.getDirectionVec()).scale(.65f);
	}

	@Override
	@Environment(value = EnvType.CLIENT)
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		if (!FastRenderDispatcher.available())
			DrillRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(ContraptionKineticRenderer kr, MovementContext context) {
		return new DrillActorInstance(kr, context);
	}

	@Override
	protected DamageSource getDamageSource() {
		return DrillBlock.damageSourceDrill;
	}

	@Override
	public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
		return super.canBreak(world, breakingPos, state) && !state.getCollisionShape(world, breakingPos)
			.isEmpty();
	}

}
