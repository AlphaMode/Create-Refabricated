package com.simibubi.create.foundation.tileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.lib.annotation.MethodsReturnNonnullByDefault;
import com.simibubi.create.lib.block.CustomDataPacketHandlingTileEntity;
import com.simibubi.create.lib.extensions.TileEntityExtensions;
import com.simibubi.create.lib.utility.NBTSerializable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.Chunk;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends TileEntity implements TileEntityExtensions, CustomDataPacketHandlingTileEntity, NBTSerializable {

	public SyncedTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public CompoundNBT create$getExtraCustomData() {
		return ((TileEntityExtensions) ((TileEntity) this)).create$getExtraCustomData();
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

//	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		fromTag(state, tag);
	}

	public void sendData() {
		if (world != null)
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 2 | 4 | 16);
	}

	public void causeBlockUpdate() {
		if (world != null)
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 1);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 1, writeToClient(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readClientUpdate(getBlockState(), pkt.getNbtCompound());
	}

	// Special handling for client update packets
	public void readClientUpdate(BlockState state, CompoundNBT tag) {
		fromTag(state, tag);
	}

	// Special handling for client update packets
	public CompoundNBT writeToClient(CompoundNBT tag) {
		return write(tag);
	}

	public void notifyUpdate() {
		markDirty();
		sendData();
	}

//	public PacketDistributor.PacketTarget packetTarget() {
//		return PacketDistributor.TRACKING_CHUNK.with(this::containedChunk);
//	}

	public Chunk containedChunk() {
		SectionPos sectionPos = SectionPos.from(pos);
		return world.getChunk(sectionPos.getSectionX(), sectionPos.getSectionZ());
	}

	@Override
	public CompoundNBT create$serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt);
		return nbt;
	}

	@Override
	public void create$deserializeNBT(CompoundNBT nbt) {
		create$deserializeNBT(null, nbt);
	}

	public void create$deserializeNBT(BlockState state, CompoundNBT nbt) {
		this.fromTag(state, nbt);
	}
}
