package com.simibubi.create.content.contraptions.goggles;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.lba.fluid.FluidStack;
import com.simibubi.create.lib.lba.fluid.IFluidHandler;
import com.simibubi.create.lib.utility.MinecraftClientUtil;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveGoggleInformation {

	Format numberFormat = new Format();
	String spacing = "    ";
	ITextComponent componentSpacing = new StringTextComponent(spacing);

	/**
	 * this method will be called when looking at a TileEntity that implemented this
	 * interface
	 *
	 * @return {@code true} if the tooltip creation was successful and should be displayed,
	 * or {@code false} if the overlay should not be displayed
	* */
	default boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking){
		return false;
	}

	static String format(double d) {
		return numberFormat.get()
			.format(d).replace("\u00A0", " ");
	}

	default boolean containedFluidTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking, IFluidHandler handler) {
		tooltip.add(componentSpacing.copy().append(Lang.translate("gui.goggles.fluid_container")));
		TranslationTextComponent mb = Lang.translate("generic.unit.millibuckets");
		Optional<IFluidHandler> resolve = Optional.ofNullable(handler);
		if (!resolve.isPresent())
			return false;

		IFluidHandler tank = resolve.get();
		if (tank.getTanks() == 0)
			return false;

		ITextComponent indent = new StringTextComponent(spacing + " ");

		boolean isEmpty = true;
		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluidStack = tank.getFluidInTank(i);
			if (fluidStack.isEmpty())
				continue;

			ITextComponent fluidName = new TranslationTextComponent(fluidStack.getTranslationKey()).formatted(TextFormatting.GRAY);
			ITextComponent contained = new StringTextComponent(format(fluidStack.getAmount())).append(mb).formatted(TextFormatting.GOLD);
			ITextComponent slash = new StringTextComponent(" / ").formatted(TextFormatting.GRAY);
			ITextComponent capacity = new StringTextComponent(format(tank.getTankCapacity(i))).append(mb).formatted(TextFormatting.DARK_GRAY);

			tooltip.add(indent.copy()
					.append(fluidName));
			tooltip.add(indent.copy()
				.append(contained)
				.append(slash)
				.append(capacity));

			isEmpty = false;
		}

		if (tank.getTanks() > 1) {
			if (isEmpty)
				tooltip.remove(tooltip.size() - 1);
			return true;
		}

		if (!isEmpty)
			return true;

		ITextComponent capacity = Lang.translate("gui.goggles.fluid_container.capacity").formatted(TextFormatting.GRAY);
		ITextComponent amount = new StringTextComponent(format(tank.getTankCapacity(0))).append(mb).formatted(TextFormatting.GOLD);

		tooltip.add(indent.copy()
			.append(capacity)
			.append(amount));
		return true;
	}

	class Format {

		private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);;

		private Format() {}

		public NumberFormat get() {
			return format;
		}

		public void update() {
			format = NumberFormat.getInstance(MinecraftClientUtil.getLocale());
			format.setMaximumFractionDigits(2);
			format.setMinimumFractionDigits(0);
			format.setGroupingUsed(true);
		}

	}

}
