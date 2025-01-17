package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.lib.lba.fluid.FluidStack;

import com.simibubi.create.lib.utility.ExtraDataUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidTankItem extends BlockItem {

	public FluidTankItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext ctx) {
		ActionResultType initialResult = super.tryPlace(ctx);
		if (!initialResult.isAccepted())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	@Override
	protected boolean onBlockPlaced(BlockPos p_195943_1_, World p_195943_2_, PlayerEntity p_195943_3_,
		ItemStack p_195943_4_, BlockState p_195943_5_) {
		MinecraftServer minecraftserver = p_195943_2_.getServer();
		if (minecraftserver == null)
			return false;
		CompoundNBT nbt = p_195943_4_.getChildTag("BlockEntityTag");
		if (nbt != null) {
			nbt.remove("Luminosity");
			nbt.remove("Size");
			nbt.remove("Height");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
			if (nbt.contains("TankContent")) {
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
				if (!fluid.isEmpty()) {
					fluid.setAmount(Math.min(FluidTankTileEntity.getCapacityMultiplier(), fluid.getAmount()));
					nbt.put("TankContent", fluid.writeToNBT(new CompoundNBT()));
				}
			}
		}
		return super.onBlockPlaced(p_195943_1_, p_195943_2_, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	private void tryMultiPlace(BlockItemUseContext ctx) {
		PlayerEntity player = ctx.getPlayer();
		if (player == null)
			return;
		if (player.isSneaking())
			return;
		Direction face = ctx.getFace();
		if (!face.getAxis()
			.isVertical())
			return;
		ItemStack stack = ctx.getItem();
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		BlockPos placedOnPos = pos.offset(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!FluidTankBlock.isTank(placedOnState))
			return;
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, placedOnPos);
		if (tankAt == null)
			return;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null)
			return;

		int width = controllerTE.width;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerTE.getPos()
			.down()
			: controllerTE.getPos()
				.up(controllerTE.height);

		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				if (!blockState.getMaterial()
					.isReplaceable())
					return;
				tanksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < tanksToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				BlockItemUseContext context = BlockItemUseContext.func_221536_a(ctx, offsetPos, face);
				ExtraDataUtil.getExtraData(player)
					.putBoolean("SilenceTankSound", true);
				super.tryPlace(context);
				ExtraDataUtil.getExtraData(player)
					.remove("SilenceTankSound");
			}
		}
	}

}
