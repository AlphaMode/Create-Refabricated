package com.simibubi.create.content.contraptions.components.saw;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.lib.lba.item.RecipeWrapper;

import net.minecraft.world.World;

@ParametersAreNonnullByDefault
public class CuttingRecipe extends ProcessingRecipe<RecipeWrapper> {

	public CuttingRecipe(ProcessingRecipeParams params) {
		super(AllRecipeTypes.CUTTING, params);
	}

	@Override
	public boolean matches(RecipeWrapper inv, World worldIn) {
		if (inv.isEmpty())
			return false;
		return ingredients.get(0)
			.test(inv.getStackInSlot(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 4;
	}

}
