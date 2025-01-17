package com.simibubi.create.content.contraptions.components.millstone;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.lib.lba.item.IItemHandler;
import com.simibubi.create.lib.lba.item.IItemHandlerModifiable;
import com.simibubi.create.lib.lba.item.ItemStackHandler;
import com.simibubi.create.lib.utility.LazyOptional;
import com.simibubi.create.lib.utility.TransferUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class MillstoneBlock extends KineticBlock implements ITE<MillstoneTileEntity>, ICogWheel {

	public MillstoneBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return AllTileEntities.MILLSTONE.create();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.MILLSTONE;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.getHeldItem(handIn).isEmpty())
			return ActionResultType.PASS;
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;

		withTileEntityDo(worldIn, pos, millstone -> {
			boolean emptyOutput = true;
			IItemHandlerModifiable inv = millstone.outputInv;
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stackInSlot = inv.getStackInSlot(slot);
				if (!stackInSlot.isEmpty())
					emptyOutput = false;
				player.inventory.placeItemBackInInventory(worldIn, stackInSlot);
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}

			if (emptyOutput) {
				inv = millstone.inputInv;
				for (int slot = 0; slot < inv.getSlots(); slot++) {
					player.inventory.placeItemBackInInventory(worldIn, inv.getStackInSlot(slot));
					inv.setStackInSlot(slot, ItemStack.EMPTY);
				}
			}

			millstone.markDirty();
			millstone.sendData();
		});

		return ActionResultType.SUCCESS;
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);

		if (entityIn.world.isRemote)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;

		MillstoneTileEntity millstone = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.getBlockPos())) 
			if (millstone == null)
				millstone = getTileEntity(worldIn, pos);
		
		if (millstone == null)
			return;

		ItemEntity itemEntity = (ItemEntity) entityIn;
		LazyOptional<IItemHandler> capability = TransferUtil.getItemHandler(millstone);
		if (!capability.isPresent())
			return;

		ItemStack remainder = capability.orElse(new ItemStackHandler()).insertItem(0, itemEntity.getItem(), false);
		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < itemEntity.getItem().getCount())
			itemEntity.setItem(remainder);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock().hasBlockEntity() && state.getBlock() != newState.getBlock()) {
			withTileEntityDo(worldIn, pos, te -> {
				ItemHelper.dropContents(worldIn, pos, te.inputInv);
				ItemHelper.dropContents(worldIn, pos, te.outputInv);
			});

			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

	@Override
	public Class<MillstoneTileEntity> getTileEntityClass() {
		return MillstoneTileEntity.class;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
