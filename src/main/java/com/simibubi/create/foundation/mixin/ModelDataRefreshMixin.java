package com.simibubi.create.foundation.mixin;

//@Environment(EnvType.CLIENT)
//@Mixin(ModelDataManager.class)
//public class ModelDataRefreshMixin {
//
//	/**
//	 * Normally ModelDataManager will throw an exception if a tile entity tries
//	 * to refresh its model data from a world the client isn't currently in,
//	 * but we need that to not happen for tile entities in fake schematic
//	 * worlds, so in those cases just do nothing instead.
//	 */ // this is a mixin into a forge class, not needed
////	@Inject(at = @At("HEAD"), method = "requestModelDataRefresh", cancellable = true, remap = false)
//	private static void requestModelDataRefresh(TileEntity te, CallbackInfo ci) {
//		if (te != null) {
//			World world = te.getWorld();
//			if (world != Minecraft.getInstance().world && world instanceof SchematicWorld)
//				ci.cancel();
//		}
//	}
//
//}
