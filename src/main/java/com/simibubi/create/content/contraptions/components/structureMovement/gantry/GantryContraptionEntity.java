// PORTED CREATE SOURCE

package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlockEntity;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

public class GantryContraptionEntity extends AbstractContraptionEntity {
	private Direction movementAxis;
	private double clientOffsetDiff;
	private double axisMotion;

	public GantryContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
	}

	public static GantryContraptionEntity create(World world, Contraption contraption, Direction movementAxis) {
		GantryContraptionEntity entity = new GantryContraptionEntity(AllEntityTypes.GANTRY_CONTRAPTION, world);
		entity.setContraption(contraption);
		entity.movementAxis = movementAxis;
		return entity;
	}

	@Override
	protected void tickContraption() {
		if (!(contraption instanceof GantryContraption))
			return;

		double prevAxisMotion = axisMotion;
		if (world.isClient) {
			clientOffsetDiff *= .75f;
			updateClientMotion();
		}

		checkPinionShaft();
		tickActors();
		Vec3d movementVec = getVelocity();

		if (ContraptionCollider.collideBlocks(this)) {
			if (!world.isClient)
				disassemble();
			return;
		}

		if (!isStalled() && age > 2)
			move(movementVec.x, movementVec.y, movementVec.z);

		if (!world.isClient && (prevAxisMotion != axisMotion || age % 3 == 0))
			sendPacket();
	}

	protected void checkPinionShaft() {
		Vec3d movementVec;
		Direction facing = ((GantryContraption) contraption).getFacing();
		Vec3d currentPosition = getAnchorVec().add(.5, .5, .5);
		BlockPos gantryShaftPos = new BlockPos(currentPosition).offset(facing.getOpposite());

		BlockEntity te = world.getBlockEntity(gantryShaftPos);
		if (!(te instanceof GantryShaftBlockEntity) || AllBlocks.GANTRY_SHAFT != te.getCachedState().getBlock()) {
			if (!world.isClient) {
				setContraptionMotion(Vec3d.ZERO);
				disassemble();
			}
			return;
		}

		BlockState blockState = te.getCachedState();
		Direction direction = blockState.get(GantryShaftBlock.FACING);
		GantryShaftBlockEntity gantryShaftTileEntity = (GantryShaftBlockEntity) te;

		float pinionMovementSpeed = gantryShaftTileEntity.getPinionMovementSpeed();
		movementVec = Vec3d.of(direction.getVector()).multiply(pinionMovementSpeed);

		if (blockState.get(GantryShaftBlock.POWERED) || pinionMovementSpeed == 0) {
			setContraptionMotion(Vec3d.ZERO);
			if (!world.isClient)
				disassemble();
			return;
		}

		Vec3d nextPosition = currentPosition.add(movementVec);
		double currentCoord = direction.getAxis()
			.choose(currentPosition.x, currentPosition.y, currentPosition.z);
		double nextCoord = direction.getAxis()
			.choose(nextPosition.x, nextPosition.y, nextPosition.z);

		if ((MathHelper.floor(currentCoord) + .5f < nextCoord != (pinionMovementSpeed * direction.getDirection()
			.offset() < 0)))
			if (!gantryShaftTileEntity.canAssembleOn()) {
				setContraptionMotion(Vec3d.ZERO);
				if (!world.isClient)
					disassemble();
				return;
			}

		if (world.isClient)
			return;
		
		axisMotion = pinionMovementSpeed;
		setContraptionMotion(movementVec);
	}

	@Override
	protected void writeAdditional(CompoundTag compound, boolean spawnPacket) {
		NBTHelper.writeEnum(compound, "GantryAxis", movementAxis);
		super.writeAdditional(compound, spawnPacket);
	}

	protected void readAdditional(CompoundTag compound, boolean spawnData) {
		movementAxis = NBTHelper.readEnum(compound, "GantryAxis", Direction.class);
		super.readAdditional(compound, spawnData);
	}

	@Override
	public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
		return localPos;
	}

	@Override
	public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
		return localPos;
	}

	@Override
	protected StructureTransform makeStructureTransform() {
		return new StructureTransform(new BlockPos(getAnchorVec().add(.5, .5, .5)), 0, 0, 0);
	}

	@Override
	protected float getStalledAngle() {
		return 0;
	}

	@Override
	public void requestTeleport(double p_70634_1_, double p_70634_3_, double p_70634_5_) {}

	@Override
	@Environment(EnvType.CLIENT)
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yw, float pt, int inc, boolean t) {}

	@Override
	protected void handleStallInformation(float x, float y, float z, float angle) {
		setPos(x, y, z);
		clientOffsetDiff = 0;
	}

	@Override
	public ContraptionRotationState getRotationState() {
		return ContraptionRotationState.NONE;
	}

	@Override
	public void doLocalTransforms(float partialTicks, MatrixStack[] matrixStacks) { }

	public void updateClientMotion() {
		float modifier = movementAxis.getDirection()
			.offset();
		setContraptionMotion(Vec3d.of(movementAxis.getVector())
			.multiply((axisMotion + clientOffsetDiff * modifier / 2f) * ServerSpeedProvider.get()));
	}

	public double getAxisCoord() {
		Vec3d anchorVec = getAnchorVec();
		return movementAxis.getAxis()
			.choose(anchorVec.x, anchorVec.y, anchorVec.z);
	}

	public void sendPacket() {
		AllPackets.CHANNEL.sendToClientsTracking(new GantryContraptionUpdatePacket(getEntityId(), getAxisCoord(), axisMotion), this);
	}

	@Environment(EnvType.CLIENT)
	public static void handlePacket(GantryContraptionUpdatePacket packet) {
		Entity entity = MinecraftClient.getInstance().world.getEntityById(packet.entityID);
		if (!(entity instanceof GantryContraptionEntity))
			return;
		GantryContraptionEntity ce = (GantryContraptionEntity) entity;
		ce.axisMotion = packet.motion;
		ce.clientOffsetDiff = packet.coord - ce.getAxisCoord();
	}
}
