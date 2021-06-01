package com.simibubi.create.lib.extensions;

import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemExtensions {
	default Supplier<Item> create$getSupplier() {
		return () -> new Item(new Item.Properties());
	}

	default boolean create$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !oldStack.equals(newStack);
	}
}