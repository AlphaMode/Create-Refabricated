// PORTED CREATE SOURCE

package com.simibubi.create.foundation.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.ColorHelper;

public class AllIcons {

	public static final Identifier ICON_ATLAS = Create.id("textures/gui/icons.png");
	private static int x = 0, y = -1;
	public static final AllIcons
		I_ADD = newRow(),
		I_TRASH = next(),
		I_3x3 = next(),
		I_TARGET = next(),
		I_PRIORITY_VERY_LOW = next(),
		I_PRIORITY_LOW = next(),
		I_PRIORITY_HIGH = next(),
		I_PRIORITY_VERY_HIGH = next(),
		I_BLACKLIST = next(),
		I_WHITELIST = next(),
		I_WHITELIST_OR = next(),
		I_WHITELIST_AND = next(),
		I_WHITELIST_NOT = next(),
		I_RESPECT_NBT = next(),
		I_IGNORE_NBT = next();
	public static final AllIcons
		I_CONFIRM = newRow(),
		I_NONE = next(),
		I_OPEN_FOLDER = next(),
		I_REFRESH = next(),
		I_ACTIVE = next(),
		I_PASSIVE = next(),
		I_ROTATE_PLACE = next(),
		I_ROTATE_PLACE_RETURNED = next(),
		I_ROTATE_NEVER_PLACE = next(),
		I_MOVE_PLACE = next(),
		I_MOVE_PLACE_RETURNED = next(),
		I_MOVE_NEVER_PLACE = next(),
		I_CART_ROTATE = next(),
		I_CART_ROTATE_PAUSED = next(),
		I_CART_ROTATE_LOCKED = next();
	public static final AllIcons
		I_DONT_REPLACE = newRow(),
		I_REPLACE_SOLID = next(),
		I_REPLACE_ANY = next(),
		I_REPLACE_EMPTY = next(),
		I_CENTERED = next(),
		I_ATTACHED = next(),
		I_INSERTED = next(),
		I_FILL = next(),
		I_PLACE = next(),
		I_REPLACE = next(),
		I_CLEAR = next(),
		I_OVERLAY = next(),
		I_FLATTEN = next();
	public static final AllIcons
		I_TOOL_DEPLOY = newRow(),
		I_SKIP_MISSING = next(),
		I_SKIP_TILES = next(),
		I_DICE = next(),
		I_TUNNEL_SPLIT = next(),
		I_TUNNEL_FORCED_SPLIT = next(),
		I_TUNNEL_ROUND_ROBIN = next(),
		I_TUNNEL_FORCED_ROUND_ROBIN = next(),
		I_TUNNEL_PREFER_NEAREST = next(),
		I_TUNNEL_RANDOMIZE = next(),
		I_TUNNEL_SYNCHRONIZE = next(),

	I_TOOL_MOVE_XZ = newRow(),
		I_TOOL_MOVE_Y = next(),
		I_TOOL_ROTATE = next(),
		I_TOOL_MIRROR = next(),
		I_ARM_ROUND_ROBIN = next(),
		I_ARM_FORCED_ROUND_ROBIN = next(),
		I_ARM_PREFER_FIRST = next(),

	I_ADD_INVERTED_ATTRIBUTE = next(),
		I_FLIP = next(),

	I_PLAY = newRow(),
		I_PAUSE = next(),
		I_STOP = next(),
		I_PLACEMENT_SETTINGS = next(),
		I_ROTATE_CCW = next(),
		I_HOUR_HAND_FIRST = next(),
		I_MINUTE_HAND_FIRST = next(),
		I_HOUR_HAND_FIRST_24 = next(),

	I_PATTERN_SOLID = newRow(),
		I_PATTERN_CHECKERED = next(),
		I_PATTERN_CHECKERED_INVERSED = next(),
		I_PATTERN_CHANCE_25 = next(),

	I_PATTERN_CHANCE_50 = newRow(),
		I_PATTERN_CHANCE_75 = next(),
		I_FOLLOW_DIAGONAL = next(),
		I_FOLLOW_MATERIAL = next(),

	I_SCHEMATIC = newRow();
	private final int iconX;
	private final int iconY;

	public AllIcons(int x, int y) {
		iconX = x * 16;
		iconY = y * 16;
	}

	private static AllIcons next() {
		return new AllIcons(++x, y);
	}

	private static AllIcons newRow() {
		return new AllIcons(x = 0, ++y);
	}

	@Environment(EnvType.CLIENT)
	public void bind() {
		MinecraftClient.getInstance()
			.getTextureManager()
			.bindTexture(ICON_ATLAS);
	}

	@Environment(EnvType.CLIENT)
	public void draw(MatrixStack ms, DrawableHelper screen, int x, int y) {
		bind();
		screen.drawTexture(ms, x, y, iconX, iconY, 16, 16);
	}

	@Environment(EnvType.CLIENT)
	public void draw(MatrixStack ms, int x, int y) {
		draw(ms, new DrawableHelper() {
		}, x, y);
	}

	@Environment(EnvType.CLIENT)
	public void draw(MatrixStack ms, VertexConsumerProvider buffer, int color) {
		VertexConsumer builder = buffer.getBuffer(RenderLayer.getTextSeeThrough(ICON_ATLAS));
		float sheetSize = 256;
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		MatrixStack.Entry peek = ms.peek();
		Vector3f rgb = ColorHelper.getRGB(color);

		Vec3d vec4 = new Vec3d(1, 1, 0);
		Vec3d vec3 = new Vec3d(0, 1, 0);
		Vec3d vec2 = new Vec3d(0, 0, 0);
		Vec3d vec1 = new Vec3d(1, 0, 0);

		float u1 = (iconX + 16) / sheetSize;
		float u2 = iconX / sheetSize;
		float v1 = iconY / sheetSize;
		float v2 = (iconY + 16) / sheetSize;

		vertex(peek, builder, j, k, rgb, vec1, u1, v1);
		vertex(peek, builder, j, k, rgb, vec2, u2, v1);
		vertex(peek, builder, j, k, rgb, vec3, u2, v2);
		vertex(peek, builder, j, k, rgb, vec4, u1, v2);
	}

	@Environment(EnvType.CLIENT)
	private void vertex(MatrixStack.Entry peek, VertexConsumer builder, int j, int k, Vector3f rgb, Vec3d vec, float u, float v) {
		builder.vertex(peek.getModel(), (float) vec.x, (float) vec.y, (float) vec.z)
			.color(rgb.getX(), rgb.getY(), rgb.getZ(), 1)
			.texture(u, v)
			.light(j, k)
			.next(); // TODO next INSTEAD OF end?
	}
}
