package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.lib.lba.item.IItemHandler;
import com.simibubi.create.lib.lba.item.ItemHandlerHelper;
import com.simibubi.create.lib.utility.LazyOptional;

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemTransferable;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InvManipulationBehaviour extends TileEntityBehaviour {

	// Extra types available for multibehaviour
	public static BehaviourType<InvManipulationBehaviour>

	TYPE = new BehaviourType<>(), EXTRACT = new BehaviourType<>(), INSERT = new BehaviourType<>();

	protected InterfaceProvider target;
	protected LazyOptional<IItemHandler> targetCapability;
	protected boolean simulateNext;
	protected boolean bypassSided;
	private boolean findNewNextTick;

	private BehaviourType<InvManipulationBehaviour> behaviourType;

	public static InvManipulationBehaviour forExtraction(SmartTileEntity te, InterfaceProvider target) {
		return new InvManipulationBehaviour(EXTRACT, te, target);
	}

	public static InvManipulationBehaviour forInsertion(SmartTileEntity te, InterfaceProvider target) {
		return new InvManipulationBehaviour(INSERT, te, target);
	}

	public InvManipulationBehaviour(SmartTileEntity te, InterfaceProvider target) {
		this(TYPE, te, target);
	}

	private InvManipulationBehaviour(BehaviourType<InvManipulationBehaviour> type, SmartTileEntity te,
		InterfaceProvider target) {
		super(te);
		behaviourType = type;
		setLazyTickRate(5);
		this.target = target;
		this.targetCapability = LazyOptional.empty();
		simulateNext = false;
		bypassSided = false;
	}

	public InvManipulationBehaviour bypassSidedness() {
		bypassSided = true;
		return this;
	}

	/**
	 * Only simulate the upcoming operation
	 */
	public InvManipulationBehaviour simulate() {
		simulateNext = true;
		return this;
	}

	public boolean hasInventory() {
		return targetCapability.isPresent();
	}

	@Nullable
	public IItemHandler getInventory() {
		return targetCapability.orElse(null);
	}

	public ItemStack extract() {
		return extract(getAmountFromFilter());
	}

	public ItemStack extract(int amount) {
		return extract(amount, Predicates.alwaysTrue());
	}

	public ItemStack extract(int amount, Predicate<ItemStack> filter) {
		return extract(amount, filter, ItemStack::getMaxStackSize);
	}

	public ItemStack extract(int amount, Predicate<ItemStack> filter, Function<ItemStack, Integer> amountThreshold) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;

		if (getWorld().isRemote)
			return ItemStack.EMPTY;
		IItemHandler inventory = targetCapability.orElse(null);
		if (inventory == null)
			return ItemStack.EMPTY;

		Predicate<ItemStack> test = getFilterTest(filter);

		ItemStack simulatedItems = extractAmountOrThresh(inventory, test, amount, amountThreshold, true);
		if (shouldSimulate || simulatedItems.isEmpty())
			return simulatedItems;

		return extractAmountOrThresh(inventory, test, amount, amountThreshold, false);
	}

	private static ItemStack extractAmountOrThresh(IItemHandler inventory, Predicate<ItemStack> test, int amount,
	   Function<ItemStack, Integer> amountThreshold, boolean shouldSimulate) {
		if (amount == -1)
			return ItemHelper.extract(inventory, test, amountThreshold, shouldSimulate);
		return ItemHelper.extract(inventory, test, amount, shouldSimulate);
	}

	public ItemStack insert(ItemStack stack) {
		boolean shouldSimulate = simulateNext;
		simulateNext = false;
		IItemHandler inventory = targetCapability.orElse(null);
		if (inventory == null)
			return stack;
		return ItemHandlerHelper.insertItemStacked(inventory, stack, shouldSimulate);
	}

	protected Predicate<ItemStack> getFilterTest(Predicate<ItemStack> customFilter) {
		Predicate<ItemStack> test = customFilter;
		FilteringBehaviour filter = tileEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	public void initialize() {
		super.initialize();
		findNewNextTick = true;
	}

	@Override
	public void onNeighborChanged(BlockPos neighborPos) {
		BlockFace targetBlockFace = target.getTarget(getWorld(), tileEntity.getPos(), tileEntity.getBlockState());
		if (targetBlockFace.getConnectedPos().equals(neighborPos))
			onHandlerInvalidated(targetCapability);
	}

	protected void onHandlerInvalidated(LazyOptional<IItemHandler> handler) {
		findNewNextTick = true;
		targetCapability = LazyOptional.empty();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (!targetCapability.isPresent())
			findNewCapability();
	}

	@Override
	public void tick() {
		super.tick();
		if (findNewNextTick || getWorld().getGameTime() % 64 == 0) {
			findNewNextTick = false;
			findNewCapability();
		}
	}

	public int getAmountFromFilter() {
		int amount = -1;
		FilteringBehaviour filter = tileEntity.getBehaviour(FilteringBehaviour.TYPE);
		if (filter != null && !filter.anyAmount())
			amount = filter.getAmount();
		return amount;
	}

	public void findNewCapability() {
		BlockFace targetBlockFace = target.getTarget(getWorld(), tileEntity.getPos(), tileEntity.getBlockState())
			.getOpposite();
		BlockPos pos = targetBlockFace.getPos();
		World world = getWorld();

		targetCapability = LazyOptional.empty();

		if (!world.isBlockPresent(pos))
			return;
		TileEntity invTE = world.getTileEntity(pos);
		if (invTE == null)
			return;
		// fixme: assume that if something is insertable, it is also extractable. probably a bad idea.
		ItemTransferable transferable = (ItemTransferable) (bypassSided
				? ItemAttributes.INSERTABLE.getFirstOrNull(invTE.getWorld(), invTE.getPos())
				: ItemAttributes.INSERTABLE.getFirstOrNull(invTE.getWorld(), invTE.getPos(), SearchOptions.inDirection(targetBlockFace.getFace())));
		targetCapability = transferable == null ? LazyOptional.empty() : LazyOptional.of(() -> (IItemHandler) transferable);
		if (targetCapability.isPresent())
			targetCapability.addListener(this::onHandlerInvalidated);
	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}

	@FunctionalInterface
	public interface InterfaceProvider {

		public static InterfaceProvider towardBlockFacing() {
			return (w, p, s) -> new BlockFace(p, s.contains(BlockStateProperties.FACING) ? s.get(BlockStateProperties.FACING)
				: s.get(BlockStateProperties.HORIZONTAL_FACING));
		}

		public static InterfaceProvider oppositeOfBlockFacing() {
			return (w, p, s) -> new BlockFace(p,
				(s.contains(BlockStateProperties.FACING) ? s.get(BlockStateProperties.FACING)
					: s.get(BlockStateProperties.HORIZONTAL_FACING)).getOpposite());
		}

		public BlockFace getTarget(World world, BlockPos pos, BlockState blockState);
	}

}
