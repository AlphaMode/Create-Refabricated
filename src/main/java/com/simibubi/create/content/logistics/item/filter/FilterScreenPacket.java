package com.simibubi.create.content.logistics.item.filter;

import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer.WhitelistMode;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.SimpleChannel.ResponseTarget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;

public class FilterScreenPacket implements C2SPacket {

	public enum Option {
		WHITELIST, WHITELIST2, BLACKLIST, RESPECT_DATA, IGNORE_DATA, UPDATE_FILTER_ITEM, ADD_TAG, ADD_INVERTED_TAG;
	}

	private Option option;
	private CompoundNBT data;

	protected FilterScreenPacket() {}

	public FilterScreenPacket(Option option) {
		this(option, new CompoundNBT());
	}

	public FilterScreenPacket(Option option, CompoundNBT data) {
		this.option = option;
		this.data = data;
	}

	public void read(PacketBuffer buffer) {
		option = Option.values()[buffer.readInt()];
		data = buffer.readCompoundTag();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(option.ordinal());
		buffer.writeCompoundTag(data);
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetHandler handler, ResponseTarget responseTarget) {
		server.execute(() -> {
			if (player == null)
				return;

			if (player.openContainer instanceof FilterContainer) {
				FilterContainer c = (FilterContainer) player.openContainer;
				if (option == Option.WHITELIST)
					c.blacklist = false;
				if (option == Option.BLACKLIST)
					c.blacklist = true;
				if (option == Option.RESPECT_DATA)
					c.respectNBT = true;
				if (option == Option.IGNORE_DATA)
					c.respectNBT = false;
				if (option == Option.UPDATE_FILTER_ITEM)
					c.ghostInventory.setStackInSlot(
							data.getInt("Slot"),
							net.minecraft.item.ItemStack.read(data.getCompound("Item")));
			}

			if (player.openContainer instanceof AttributeFilterContainer) {
				AttributeFilterContainer c = (AttributeFilterContainer) player.openContainer;
				if (option == Option.WHITELIST)
					c.whitelistMode = WhitelistMode.WHITELIST_DISJ;
				if (option == Option.WHITELIST2)
					c.whitelistMode = WhitelistMode.WHITELIST_CONJ;
				if (option == Option.BLACKLIST)
					c.whitelistMode = WhitelistMode.BLACKLIST;
				if (option == Option.ADD_TAG)
					c.appendSelectedAttribute(ItemAttribute.fromNBT(data), false);
				if (option == Option.ADD_INVERTED_TAG)
					c.appendSelectedAttribute(ItemAttribute.fromNBT(data), true);
			}

		});
	}

}
