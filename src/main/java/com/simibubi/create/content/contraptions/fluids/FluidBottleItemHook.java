package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.item.Item;

public class FluidBottleItemHook extends Item {

	public FluidBottleItemHook(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

//	public static void preventWaterBottlesFromCreatesFluids(PlayerInteractEvent.RightClickItem event) {
//		ItemStack itemStack = event.getItemStack();
//		if (itemStack.isEmpty())
//			return;
//		if (!(itemStack.getItem() instanceof GlassBottleItem))
//			return;
//
//		World world = event.getWorld();
//		PlayerEntity player = event.getPlayer();
//		RayTraceResult raytraceresult = rayTrace(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
//		if (raytraceresult.getType() != RayTraceResult.Type.BLOCK)
//			return;
//		BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getPos();
//		if (!world.isBlockModifiable(player, blockpos))
//			return;
//
//		FluidState fluidState = world.getFluidState(blockpos);
//		if (fluidState.isTagged(FluidTags.WATER) && fluidState.getFluid()
//			.getRegistryName()
//			.getNamespace()
//			.equals(Create.ID)) {
//			event.setCancellationResult(ActionResultType.PASS);
//			event.setCanceled(true);
//			return;
//		}
//
//		return;
//	}

}
