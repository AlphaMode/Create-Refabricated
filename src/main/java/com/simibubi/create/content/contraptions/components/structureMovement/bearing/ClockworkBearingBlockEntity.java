// PORTED CREATE SOURCE

package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;

import com.simibubi.create.AllBlockEntities;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.DisplayAssemblyExceptionsProvider;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkContraption.HandType;
import com.simibubi.create.foundation.block.entity.BlockEntityBehaviour;
import com.simibubi.create.foundation.block.entity.behaviour.scrollvalue.NamedIconOptions;
import com.simibubi.create.foundation.block.entity.behaviour.scrollvalue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;

public class ClockworkBearingBlockEntity extends KineticBlockEntity implements BearingBlockEntity, DisplayAssemblyExceptionsProvider {

	protected ControlledContraptionEntity hourHand;
	protected ControlledContraptionEntity minuteHand;
	protected float hourAngle;
	protected float minuteAngle;
	protected float clientHourAngleDiff;
	protected float clientMinuteAngleDiff;

	protected boolean running;
	protected boolean assembleNextTick;
	protected AssemblyException lastException;

	protected ScrollOptionBehaviour<ClockHands> operationMode;

	public ClockworkBearingBlockEntity() {
		super(AllBlockEntities.CLOCKWORK_BEARING);
		setLazyTickRate(3);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		operationMode = new ScrollOptionBehaviour<>(ClockHands.class,
			Lang.translate("contraptions.clockwork.clock_hands"), this, getMovementModeSlot());
		operationMode.requiresWrench();
		behaviours.add(operationMode);
	}

	@Override
	public boolean isWoodenTop() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isClient) {
			clientMinuteAngleDiff /= 2;
			clientHourAngleDiff /= 2;
		}

		if (!world.isClient && assembleNextTick) {
			assembleNextTick = false;
			if (running) {
				boolean canDisassemble = true;
				if (speed == 0 && (canDisassemble || hourHand == null || hourHand.getContraption()
					.getBlocks()
					.isEmpty())) {
					if (hourHand != null)
						hourHand.getContraption()
							.stop(world);
					if (minuteHand != null)
						minuteHand.getContraption()
							.stop(world);
					disassemble();
				}
				return;
			} else
				assemble();
			return;
		}

		if (!running)
			return;

		if (!(hourHand != null && hourHand.isStalled())) {
			float newAngle = hourAngle + getHourArmSpeed();
			hourAngle = (float) (newAngle % 360);
		}

		if (!(minuteHand != null && minuteHand.isStalled())) {
			float newAngle = minuteAngle + getMinuteArmSpeed();
			minuteAngle = (float) (newAngle % 360);
		}

		applyRotations();
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return lastException;
	}

	protected void applyRotations() {
		BlockState blockState = getCachedState();
		Axis axis = Axis.X;
		
		if (blockState.contains(Properties.FACING))
			axis = blockState.get(Properties.FACING)
				.getAxis();
		
		if (hourHand != null) {
			hourHand.setAngle(hourAngle);
			hourHand.setRotationAxis(axis);
		}
		if (minuteHand != null) {
			minuteHand.setAngle(minuteAngle);
			minuteHand.setRotationAxis(axis);
		}
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (hourHand != null && !world.isClient)
			sendData();
	}

	public float getHourArmSpeed() {
		float speed = getAngularSpeed() / 2f;

		if (speed != 0) {
			ClockHands mode = ClockHands.values()[operationMode.getValue()];
			float hourTarget = mode == ClockHands.HOUR_FIRST ? getHourTarget(false)
				: mode == ClockHands.MINUTE_FIRST ? getMinuteTarget() : getHourTarget(true);
			float shortestAngleDiff = AngleHelper.getShortestAngleDiff(hourAngle, hourTarget);
			if (shortestAngleDiff < 0) {
				speed = Math.max(speed, shortestAngleDiff);
			} else {
				speed = Math.min(-speed, shortestAngleDiff);
			}
		}

		return speed + clientHourAngleDiff / 3f;
	}

	public float getMinuteArmSpeed() {
		float speed = getAngularSpeed();

		if (speed != 0) {
			ClockHands mode = ClockHands.values()[operationMode.getValue()];
			float minuteTarget = mode == ClockHands.MINUTE_FIRST ? getHourTarget(false) : getMinuteTarget();
			float shortestAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngle, minuteTarget);
			if (shortestAngleDiff < 0) {
				speed = Math.max(speed, shortestAngleDiff);
			} else {
				speed = Math.min(-speed, shortestAngleDiff);
			}
		}

		return speed + clientMinuteAngleDiff / 3f;
	}

	protected float getHourTarget(boolean cycle24) {
		int dayTime = (int) (world.getTimeOfDay() % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int offset = getCachedState().get(ClockworkBearingBlock.FACING)
			.getDirection()
			.offset();
		float hourTarget = (float) (offset * -360 / (cycle24 ? 24f : 12f) * (hours % (cycle24 ? 24 : 12)));
		return hourTarget;
	}

	protected float getMinuteTarget() {
		int dayTime = (int) (world.getTimeOfDay() % 24000);
		int minutes = (dayTime % 1000) * 60 / 1000;
		int offset = getCachedState().get(ClockworkBearingBlock.FACING)
			.getDirection()
			.offset();
		float minuteTarget = (float) (offset * -360 / 60f * (minutes));
		return minuteTarget;
	}

	public float getAngularSpeed() {
		float speed = -Math.abs(getSpeed() * 3 / 10f);
		/*if (world.isClient)
			speed *= ServerSpeedProvider.get();*/
		return speed;
	}

	public void assemble() {
		if (!(world.getBlockState(pos)
			.getBlock() instanceof ClockworkBearingBlock))
			return;

		Direction direction = getCachedState().get(Properties.FACING);

		// Collect Construct
		Pair<ClockworkContraption, ClockworkContraption> contraption;
		try {
			contraption = ClockworkContraption.assembleClockworkAt(world, pos, direction);
			lastException = null;
		} catch (AssemblyException e) {
			lastException = e;
			sendData();
			return;
		}
		if (contraption == null)
			return;
		if (contraption.getLeft() == null)
			return;
		if (contraption.getLeft()
			.getBlocks()
			.isEmpty())
			return;
		BlockPos anchor = pos.offset(direction);

		contraption.getLeft()
			.removeBlocksFromWorld(world, BlockPos.ORIGIN);
		hourHand = ControlledContraptionEntity.create(world, this, contraption.getLeft());
		hourHand.updatePosition(anchor.getX(), anchor.getY(), anchor.getZ());
		hourHand.setRotationAxis(direction.getAxis());
		world.spawnEntity(hourHand);

		//AllTriggers.triggerForNearbyPlayers(AllTriggers.CLOCKWORK_BEARING, world, pos, 5);

		if (contraption.getRight() != null) {
			anchor = pos.offset(direction, contraption.getRight().offset + 1);
			contraption.getRight()
				.removeBlocksFromWorld(world, BlockPos.ORIGIN);
			minuteHand = ControlledContraptionEntity.create(world, this, contraption.getRight());
			minuteHand.updatePosition(anchor.getX(), anchor.getY(), anchor.getZ());
			minuteHand.setRotationAxis(direction.getAxis());
			world.spawnEntity(minuteHand);
		}

		// Run
		running = true;
		hourAngle = 0;
		minuteAngle = 0;
		sendData();
	}

	public void disassemble() {
		if (!running && hourHand == null && minuteHand == null)
			return;

		hourAngle = 0;
		minuteAngle = 0;
		applyRotations();

		if (hourHand != null) {
			hourHand.disassemble();
		}
		if (minuteHand != null)
			minuteHand.disassemble();

		hourHand = null;
		minuteHand = null;
		running = false;
		sendData();
	}

	@Override
	public void attach(ControlledContraptionEntity contraption) {
		if (!(contraption.getContraption() instanceof ClockworkContraption))
			return;

		ClockworkContraption cc = (ClockworkContraption) contraption.getContraption();
		markDirty();
		Direction facing = getCachedState().get(Properties.FACING);
		BlockPos anchor = pos.offset(facing, cc.offset + 1);
		if (cc.handType == HandType.HOUR) {
			this.hourHand = contraption;
			hourHand.updatePosition(anchor.getX(), anchor.getY(), anchor.getZ());
		} else {
			this.minuteHand = contraption;
			minuteHand.updatePosition(anchor.getX(), anchor.getY(), anchor.getZ());
		}
		if (!world.isClient) {
			this.running = true;
			sendData();
		}
	}

	@Override
	public void toTag(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Running", running);
		compound.putFloat("HourAngle", hourAngle);
		compound.putFloat("MinuteAngle", minuteAngle);
		AssemblyException.write(compound, lastException);
		super.toTag(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		float hourAngleBefore = hourAngle;
		float minuteAngleBefore = minuteAngle;

		running = compound.getBoolean("Running");
		hourAngle = compound.getFloat("HourAngle");
		minuteAngle = compound.getFloat("MinuteAngle");
		lastException = AssemblyException.read(compound);
		super.fromTag(state, compound, clientPacket);

		if (!clientPacket)
			return;

		if (running) {
			clientHourAngleDiff = AngleHelper.getShortestAngleDiff(hourAngleBefore, hourAngle);
			clientMinuteAngleDiff = AngleHelper.getShortestAngleDiff(minuteAngleBefore, minuteAngle);
			hourAngle = hourAngleBefore;
			minuteAngle = minuteAngleBefore;
		} else {
			hourHand = null;
			minuteHand = null;
		}
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		assembleNextTick = true;
	}

	@Override
	public boolean isValid() {
		return !isRemoved();
	}

	@Override
	public float getInterpolatedAngle(float partialTicks) {
		if (hourHand == null || hourHand.isStalled())
			partialTicks = 0;
		return MathHelper.lerp(partialTicks, hourAngle, hourAngle + getHourArmSpeed());
	}

	@Override
	public void onStall() {
		if (!world.isClient)
			sendData();
	}

	@Override
	public void markRemoved() {
		if (!world.isClient)
			disassemble();
		super.markRemoved();
	}

	@Override
	public void collided() {}

	@Override
	public boolean isAttachedTo(AbstractContraptionEntity contraption) {
		if (!(contraption.getContraption() instanceof ClockworkContraption))
			return false;
		ClockworkContraption cc = (ClockworkContraption) contraption.getContraption();
		if (cc.handType == HandType.HOUR)
			return this.hourHand == contraption;
		else
			return this.minuteHand == contraption;
	}

	public boolean isRunning() {
		return running;
	}

	static enum ClockHands implements NamedIconOptions {

		HOUR_FIRST(AllIcons.I_HOUR_HAND_FIRST),
		MINUTE_FIRST(AllIcons.I_MINUTE_HAND_FIRST),
		HOUR_FIRST_24(AllIcons.I_HOUR_HAND_FIRST_24),

		;

		private String translationKey;
		private AllIcons icon;

		private ClockHands(AllIcons icon) {
			this.icon = icon;
			translationKey = "contraptions.clockwork." + Lang.asId(name());
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}

	}

	@Override
	public BlockPos getBlockPosition() {
		return pos;
	}

	@Override
	public boolean shouldRenderAsBE() {
		return true;
	}
}
