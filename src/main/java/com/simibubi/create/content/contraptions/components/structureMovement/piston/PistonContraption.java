// PORTED CREATE SOURCE

package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.create.AllBlocks.PISTON_EXTENSION_POLE;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPiston;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;
import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isStickyPiston;
import static net.minecraft.state.property.Properties.FACING;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementTraits;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionLighter;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionType;
import com.simibubi.create.content.contraptions.components.structureMovement.TranslatingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.foundation.utility.VecHelper;

public class PistonContraption extends TranslatingContraption {

	protected int extensionLength;
	protected int initialExtensionProgress;
	protected Direction orientation;

	private Box pistonExtensionCollisionBox;
	private boolean retract;

	@Override
	protected ContraptionType getType() {
		return ContraptionType.PISTON;
	}

	public PistonContraption() {}

	public PistonContraption(Direction direction, boolean retract) {
		orientation = direction;
		this.retract = retract;
	}

	@Override
	public boolean assemble(World world, BlockPos pos) throws AssemblyException {
		if (!collectExtensions(world, pos, orientation))
			return false;
		int count = blocks.size();
		if (!searchMovedStructure(world, anchor, retract ? orientation.getOpposite() : orientation))
			return false;
		if (blocks.size() == count) { // no new blocks added
			bounds = pistonExtensionCollisionBox;
		} else {
			bounds = bounds.union(pistonExtensionCollisionBox);
		}
		startMoving(world);
		return true;
	}

	private boolean collectExtensions(World world, BlockPos pos, Direction direction) throws AssemblyException {
		List<StructureBlockInfo> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		BlockState nextBlock = world.getBlockState(actualStart.offset(direction));
		int extensionsInFront = 0;
		BlockState blockState = world.getBlockState(pos);
		boolean sticky = isStickyPiston(blockState);

		if (!isPiston(blockState))
			return false;

		if (blockState.get(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			/*while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis()) || isPistonHead(nextBlock) && nextBlock.get(FACING) == direction) {

				actualStart = actualStart.offset(direction);
				poles.add(new StructureBlockInfo(actualStart, nextBlock.with(FACING, direction), null));
				extensionsInFront++;

				if (isPistonHead(nextBlock))
					break;

				nextBlock = world.getBlockState(actualStart.offset(direction));
				if (extensionsInFront > MechanicalPistonBlock.maxAllowedPistonPoles())
					throw AssemblyException.tooManyPistonPoles();
			}*/
		}

		if (extensionsInFront == 0)
			poles.add(new StructureBlockInfo(pos, MECHANICAL_PISTON_HEAD.getDefaultState()
				.with(FACING, direction)
				.with(Properties.PISTON_TYPE, sticky ? PistonType.STICKY : PistonType.DEFAULT), null));
		else
			poles.add(new StructureBlockInfo(pos, PISTON_EXTENSION_POLE.getDefaultState()
				.with(FACING, direction), null));

		BlockPos end = pos;
		nextBlock = world.getBlockState(end.offset(direction.getOpposite()));
		int extensionsInBack = 0;

		/*while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis())) {
			end = end.offset(direction.getOpposite());
			poles.add(new StructureBlockInfo(end, nextBlock.with(FACING, direction), null));
			extensionsInBack++;
			nextBlock = world.getBlockState(end.offset(direction.getOpposite()));

			if (extensionsInFront + extensionsInBack > MechanicalPistonBlock.maxAllowedPistonPoles())
				throw AssemblyException.tooManyPistonPoles();
		}*/

		anchor = pos.offset(direction, initialExtensionProgress + 1);
		extensionLength = extensionsInBack + extensionsInFront;
		initialExtensionProgress = extensionsInFront;
		pistonExtensionCollisionBox = new Box(
				BlockPos.ORIGIN.offset(direction, -1),
				BlockPos.ORIGIN.offset(direction, -extensionLength - 1)).stretch(1,
						1, 1);

		if (extensionLength == 0)
			throw AssemblyException.noPistonPoles();

		bounds = new Box(0, 0, 0, 0, 0, 0);

		for (StructureBlockInfo pole : poles) {
			BlockPos relPos = pole.pos.offset(direction, -extensionsInFront);
			BlockPos localPos = relPos.subtract(anchor);
			getBlocks().put(localPos, new StructureBlockInfo(localPos, pole.state, null));
			//pistonExtensionCollisionBox = pistonExtensionCollisionBox.union(new AxisAlignedBB(localPos));
		}

		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pistonExtensionCollisionBox.contains(VecHelper.getCenterOf(pos.subtract(anchor)));
	}

	@Override
	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction direction, Queue<BlockPos> frontier) throws AssemblyException {
		frontier.clear();
		boolean sticky = isStickyPiston(world.getBlockState(pos.offset(orientation, -1)));
		boolean retracting = direction != orientation;
		if (retracting && !sticky)
			return true;
		for (int offset = 0; offset <= Create.getConfig().kinetics.maxChassisRange; offset++) {
			if (offset == 1 && retracting)
				return true;
			BlockPos currentPos = pos.offset(orientation, offset + initialExtensionProgress);
			if (retracting && World.isOutOfBuildLimitVertically(currentPos))
				return true;
			if (!world.canSetBlock(currentPos))
				throw AssemblyException.unloadedChunk(currentPos);
			BlockState state = world.getBlockState(currentPos);
			if (!BlockMovementTraits.movementNecessary(state, world, currentPos))
				return true;
			if (BlockMovementTraits.isBrittle(state) && !(state.getBlock() instanceof CarpetBlock))
				return true;
			if (isPistonHead(state) && state.get(FACING) == direction.getOpposite())
				return true;
			if (!BlockMovementTraits.movementAllowed(state, world, currentPos))
				if (retracting)
					return true;
				else
					throw AssemblyException.unmovableBlock(currentPos, state);
			if (retracting && state.getPistonBehavior() == PistonBehavior.PUSH_ONLY)
				return true;
			frontier.add(currentPos);
			if (BlockMovementTraits.notSupportive(state, orientation))
				return true;
		}
		return true;
	}

	@Override
	public void addBlock(BlockPos pos, Pair<StructureBlockInfo, BlockEntity> capture) {
		super.addBlock(pos.offset(orientation, -initialExtensionProgress), capture);
	}

	@Override
	public BlockPos toLocalPos(BlockPos globalPos) {
		return globalPos.subtract(anchor)
			.offset(orientation, -initialExtensionProgress);
	}

	@Override
	protected boolean customBlockPlacement(WorldAccess world, BlockPos pos, BlockState state) {
		BlockPos pistonPos = anchor.offset(orientation, -1);
		BlockState pistonState = world.getBlockState(pistonPos);
		BlockEntity te = world.getBlockEntity(pistonPos);
		if (pos.equals(pistonPos)) {
			if (te == null || te.isRemoved())
				return true;
			if (!isExtensionPole(state) && isPiston(pistonState))
				world.setBlockState(pistonPos, pistonState.with(MechanicalPistonBlock.STATE, PistonState.RETRACTED),
					3 | 16);
			return true;
		}
		return false;
	}

	@Override
	protected boolean customBlockRemoval(WorldAccess world, BlockPos pos, BlockState state) {
		BlockPos pistonPos = anchor.offset(orientation, -1);
		BlockState blockState = world.getBlockState(pos);
		if (pos.equals(pistonPos) && isPiston(blockState)) {
			world.setBlockState(pos, blockState.with(MechanicalPistonBlock.STATE, PistonState.MOVING), 66 | 16);
			return true;
		}
		return false;
	}

	@Override
	public void readNBT(World world, CompoundTag nbt, boolean spawnData) {
		super.readNBT(world, nbt, spawnData);
		initialExtensionProgress = nbt.getInt("InitialLength");
		extensionLength = nbt.getInt("ExtensionLength");
		orientation = Direction.byId(nbt.getInt("Orientation"));
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialLength", initialExtensionProgress);
		tag.putInt("ExtensionLength", extensionLength);
		tag.putInt("Orientation", orientation.getId());
		return tag;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ContraptionLighter<?> makeLighter() {
		return new PistonLighter(this);
	}
}
