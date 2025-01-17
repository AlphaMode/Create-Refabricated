package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;

public class WrenchModel extends CustomRenderedItemModel {

	public WrenchModel(IBakedModel template) {
		super(template, "wrench");
		addPartials("gear");
	}

	@Override
	public DynamicItemRenderer createRenderer() {
		return new WrenchItemRenderer();
	}

}
