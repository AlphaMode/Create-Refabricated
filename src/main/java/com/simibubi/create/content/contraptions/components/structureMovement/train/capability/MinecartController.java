package com.simibubi.create.content.contraptions.components.structureMovement.train.capability;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.train.CouplingHandler;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.lib.helper.AbstractMinecartEntityHelper;
import com.simibubi.create.lib.utility.Constants.NBT;
import com.simibubi.create.lib.utility.ListenerProvider;
import com.simibubi.create.lib.utility.MinecartAndRailUtil;
import com.simibubi.create.lib.utility.NBTSerializable;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;


/**
 * Extended code for Minecarts, this allows for handling stalled carts and
 * coupled trains
 */
public class MinecartController implements NBTSerializable, ListenerProvider, com.simibubi.create.lib.utility.MinecartController {
	public Set<NonNullConsumer> listeners = new HashSet<>();
	@Override
	public Set<NonNullConsumer> getListeners() {
		return listeners;
	}

	@Override
	public com.simibubi.create.lib.utility.MinecartController create(AbstractMinecartEntity cart) {
		return new MinecartController(cart);
	}

	public static MinecartController EMPTY;
	private boolean needsEntryRefresh;
	private WeakReference<AbstractMinecartEntity> weakRef;

	/*
	 * Stall information, <Internal (waiting couplings), External (stalled
	 * contraptions)>
	 */
	private Couple<Optional<StallData>> stallData;

	/*
	 * Coupling information, <Main (helmed by this cart), Connected (handled by
	 * other cart)>
	 */
	private Couple<Optional<CouplingData>> couplings;

	public MinecartController(AbstractMinecartEntity minecart) {
		if (minecart == null) { // this should never be null, is only used for the InitController initialization in the MinecartController interface
			return;
		}
		weakRef = new WeakReference<>(minecart);
		stallData = Couple.create(Optional::empty);
		couplings = Couple.create(Optional::empty);
		needsEntryRefresh = true;
	}

	public void tick() {
		AbstractMinecartEntity cart = cart();
		World world = getWorld();

		if (needsEntryRefresh) {
			CapabilityMinecartController.queuedAdditions.get(world).add(cart);
			needsEntryRefresh = false;
		}

		stallData.forEach(opt -> opt.ifPresent(sd -> sd.tick(cart)));

		MutableBoolean internalStall = new MutableBoolean(false);
		couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {

			UUID idOfOther = cd.idOfCart(!main);
			MinecartController otherCart = CapabilityMinecartController.getIfPresent(world, idOfOther);
			internalStall.setValue(
				internalStall.booleanValue() || otherCart == null || !otherCart.isPresent() || otherCart.isStalled(false));

		}));
		if (!world.isRemote) {
			setStalled(internalStall.booleanValue(), true);
			disassemble(cart);
		}
	}

	private void disassemble(AbstractMinecartEntity cart) {
		if (cart instanceof MinecartEntity) {
			return;
		}
		List<Entity> passengers = cart.getPassengers();
		if (passengers.isEmpty() || !(passengers.get(0) instanceof AbstractContraptionEntity)) {
			return;
		}
		World world = cart.world;
		int i = MathHelper.floor(cart.getX());
		int j = MathHelper.floor(cart.getY());
		int k = MathHelper.floor(cart.getZ());
		if (world.getBlockState(new BlockPos(i, j - 1, k))
				.isIn(BlockTags.RAILS)) {
			--j;
		}
		BlockPos blockpos = new BlockPos(i, j, k);
		BlockState blockstate = world.getBlockState(blockpos);
		if (AbstractMinecartEntityHelper.canCartUseRail(cart) && blockstate.isIn(BlockTags.RAILS)
				&& blockstate.getBlock() instanceof PoweredRailBlock
				&& (MinecartAndRailUtil.isActivatorRail(
						blockstate.getBlock()))) {
			if (cart.isBeingRidden()) {
				cart.removePassengers();
			}

			if (cart.getRollingAmplitude() == 0) {
				cart.setRollingDirection(-cart.getRollingDirection());
				cart.setRollingAmplitude(10);
				cart.setDamage(50.0F);
				cart.velocityChanged = true;
			}
		}
	}

	public boolean isFullyCoupled() {
		return isLeadingCoupling() && isConnectedToCoupling();
	}

	public boolean isLeadingCoupling() {
		return couplings.get(true)
			.isPresent();
	}

	public boolean isConnectedToCoupling() {
		return couplings.get(false)
			.isPresent();
	}

	public boolean isCoupledThroughContraption() {
		for (boolean current : Iterate.trueAndFalse)
			if (hasContraptionCoupling(current))
				return true;
		return false;
	}

	public boolean hasContraptionCoupling(boolean current) {
		Optional<CouplingData> optional = couplings.get(current);
		return optional.isPresent() && optional.get().contraption;
	}

	public float getCouplingLength(boolean leading) {
		Optional<CouplingData> optional = couplings.get(leading);
		if (optional.isPresent())
			return optional.get().length;
		return 0;
	}

	public void decouple() {
		couplings.forEachWithContext((opt, main) -> opt.ifPresent(cd -> {
			UUID idOfOther = cd.idOfCart(!main);
			MinecartController otherCart = CapabilityMinecartController.getIfPresent(getWorld(), idOfOther);
			if (otherCart == null)
				return;

			removeConnection(main);
			otherCart.removeConnection(!main);
		}));
	}

	public void removeConnection(boolean main) {
		if (hasContraptionCoupling(main) && !getWorld().isRemote) {
			List<Entity> passengers = cart().getPassengers();
			if (!passengers.isEmpty()) {
				Entity entity = passengers.get(0);
				if (entity instanceof AbstractContraptionEntity)
					((AbstractContraptionEntity) entity).disassemble();
			}
		}

		couplings.set(main, Optional.empty());
		needsEntryRefresh |= main;
		sendData();
	}

	public void prepareForCoupling(boolean isLeading) {
		// reverse existing chain if necessary
		if (isLeading && isLeadingCoupling() || !isLeading && isConnectedToCoupling()) {

			List<MinecartController> cartsToFlip = new ArrayList<>();
			MinecartController current = this;
			boolean forward = current.isLeadingCoupling();
			int safetyCount = 1000;

			while (true) {
				if (safetyCount-- <= 0) {
					Create.LOGGER.warn("Infinite loop in coupling iteration");
					return;
				}
				cartsToFlip.add(current);
				current = CouplingHandler.getNextInCouplingChain(getWorld(), current, forward);
				if (current == null || current == MinecartController.EMPTY)
					break;
			}

			for (MinecartController minecartController : cartsToFlip) {
				MinecartController mc = minecartController;
				mc.couplings.forEachWithContext((opt, leading) -> opt.ifPresent(cd -> {
					cd.flip();
					if (!cd.contraption)
						return;
					List<Entity> passengers = mc.cart()
						.getPassengers();
					if (passengers.isEmpty())
						return;
					Entity entity = passengers.get(0);
					if (!(entity instanceof OrientedContraptionEntity))
						return;
					OrientedContraptionEntity contraption = (OrientedContraptionEntity) entity;
					UUID couplingId = contraption.getCouplingId();
					if (couplingId == cd.mainCartID) {
						contraption.setCouplingId(cd.connectedCartID);
						return;
					}
					if (couplingId == cd.connectedCartID) {
						contraption.setCouplingId(cd.mainCartID);
						return;
					}
				}));
				mc.couplings = mc.couplings.swap();
				mc.needsEntryRefresh = true;
				if (mc == this)
					continue;
				mc.sendData();
			}
		}
	}

	public void coupleWith(boolean isLeading, UUID coupled, float length, boolean contraption) {
		UUID mainID = isLeading ? cart().getUniqueID() : coupled;
		UUID connectedID = isLeading ? coupled : cart().getUniqueID();
		couplings.set(isLeading, Optional.of(new CouplingData(mainID, connectedID, length, contraption)));
		needsEntryRefresh |= isLeading;
		sendData();
	}

	@Nullable
	public UUID getCoupledCart(boolean asMain) {
		Optional<CouplingData> optional = couplings.get(asMain);
		if (!optional.isPresent())
			return null;
		CouplingData couplingData = optional.get();
		return asMain ? couplingData.connectedCartID : couplingData.mainCartID;
	}

	public boolean isStalled() {
		return isStalled(true) || isStalled(false);
	}

	private boolean isStalled(boolean internal) {
		return stallData.get(internal)
			.isPresent();
	}

	public void setStalledExternally(boolean stall) {
		setStalled(stall, false);
	}

	private void setStalled(boolean stall, boolean internal) {
		if (isStalled(internal) == stall)
			return;

		AbstractMinecartEntity cart = cart();
		if (stall) {
			stallData.set(internal, Optional.of(new StallData(cart)));
			sendData();
			return;
		}

		if (!isStalled(!internal))
			stallData.get(internal)
				.get()
				.release(cart);
		stallData.set(internal, Optional.empty());

		sendData();
	}

	public void sendData() {
		if (getWorld().isRemote)
			return;
		AllPackets.channel.sendToClientsTracking(
			new MinecartControllerUpdatePacket(this), this.cart());
	}

	@Override
	public CompoundNBT create$serializeNBT() {
		CompoundNBT compoundNBT = new CompoundNBT();

		stallData.forEachWithContext((opt, internal) -> opt
			.ifPresent(sd -> compoundNBT.put(internal ? "InternalStallData" : "StallData", sd.serialize())));
		couplings.forEachWithContext((opt, main) -> opt
			.ifPresent(cd -> compoundNBT.put(main ? "MainCoupling" : "ConnectedCoupling", cd.serialize())));

		return compoundNBT;
	}

	@Override
	public void create$deserializeNBT(CompoundNBT nbt) {
		Optional<StallData> internalSD = Optional.empty();
		Optional<StallData> externalSD = Optional.empty();
		Optional<CouplingData> mainCD = Optional.empty();
		Optional<CouplingData> connectedCD = Optional.empty();

		if (nbt.contains("InternalStallData"))
			internalSD = Optional.of(StallData.read(nbt.getCompound("InternalStallData")));
		if (nbt.contains("StallData"))
			externalSD = Optional.of(StallData.read(nbt.getCompound("StallData")));
		if (nbt.contains("MainCoupling"))
			mainCD = Optional.of(CouplingData.read(nbt.getCompound("MainCoupling")));
		if (nbt.contains("ConnectedCoupling"))
			connectedCD = Optional.of(CouplingData.read(nbt.getCompound("ConnectedCoupling")));

		stallData = Couple.create(internalSD, externalSD);
		couplings = Couple.create(mainCD, connectedCD);
		needsEntryRefresh = true;
	}

	public boolean isPresent() {
		return weakRef.get() != null && cart().isAlive();
	}

	public AbstractMinecartEntity cart() {
		return weakRef.get();
	}

	public static MinecartController empty() {
		return EMPTY != null ? EMPTY : (EMPTY = new MinecartController(null));
	}

	private World getWorld() {
		return cart().getEntityWorld();
	}

	private static class CouplingData {

		private UUID mainCartID;
		private UUID connectedCartID;
		private float length;
		private boolean contraption;

		public CouplingData(UUID mainCartID, UUID connectedCartID, float length, boolean contraption) {
			this.mainCartID = mainCartID;
			this.connectedCartID = connectedCartID;
			this.length = length;
			this.contraption = contraption;
		}

		void flip() {
			UUID swap = mainCartID;
			mainCartID = connectedCartID;
			connectedCartID = swap;
		}

		CompoundNBT serialize() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Main", NBTUtil.fromUuid(mainCartID));
			nbt.put("Connected", NBTUtil.fromUuid(connectedCartID));
			nbt.putFloat("Length", length);
			nbt.putBoolean("Contraption", contraption);
			return nbt;
		}

		static CouplingData read(CompoundNBT nbt) {
			UUID mainCartID = NBTUtil.readUniqueId(NBTHelper.getINBT(nbt, "Main"));
			UUID connectedCartID = NBTUtil.readUniqueId(NBTHelper.getINBT(nbt, "Connected"));
			float length = nbt.getFloat("Length");
			boolean contraption = nbt.getBoolean("Contraption");
			return new CouplingData(mainCartID, connectedCartID, length, contraption);
		}

		public UUID idOfCart(boolean main) {
			return main ? mainCartID : connectedCartID;
		}

	}

	private static class StallData {
		Vector3d position;
		Vector3d motion;
		float yaw, pitch;

		private StallData() {}

		StallData(AbstractMinecartEntity entity) {
			position = entity.getPositionVec();
			motion = entity.getMotion();
			yaw = entity.rotationYaw;
			pitch = entity.rotationPitch;
			tick(entity);
		}

		void tick(AbstractMinecartEntity entity) {
			entity.setPosition(position.x, position.y, position.z);
			entity.setMotion(Vector3d.ZERO);
			entity.rotationYaw = yaw;
			entity.rotationPitch = pitch;
		}

		void release(AbstractMinecartEntity entity) {
			entity.setMotion(motion);
		}

		CompoundNBT serialize() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Pos", VecHelper.writeNBT(position));
			nbt.put("Motion", VecHelper.writeNBT(motion));
			nbt.putFloat("Yaw", yaw);
			nbt.putFloat("Pitch", pitch);
			return nbt;
		}

		static StallData read(CompoundNBT nbt) {
			StallData stallData = new StallData();
			stallData.position = VecHelper.readNBT(nbt.getList("Pos", NBT.TAG_DOUBLE));
			stallData.motion = VecHelper.readNBT(nbt.getList("Motion", NBT.TAG_DOUBLE));
			stallData.yaw = nbt.getFloat("Yaw");
			stallData.pitch = nbt.getFloat("Pitch");
			return stallData;
		}
	}

}
