package com.simibubi.create.content.curiosities.symmetry;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SymmetryHandler {

	private static int tickCounter = 0;

	public static ActionResultType onBlockPlaced(ItemUseContext context) {
		if (context.getWorld()
			.isRemote())
			return ActionResultType.PASS;
		if (!(context.getPlayer() instanceof PlayerEntity))
			return ActionResultType.PASS;

		PlayerEntity player = (PlayerEntity) context.getPlayer();
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i)
				.isEmpty()
				&& inv.getStackInSlot(i)
					.getItem() == AllItems.WAND_OF_SYMMETRY.get()) {
				SymmetryWandItem.apply(player.world, inv.getStackInSlot(i), player, context.getPos(),
					context.getWorld().getBlockState(context.getPos()));
			}
		}
		return ActionResultType.PASS;
	}

	public static void onBlockDestroyed(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te) {
		PlayerInventory inv = player.inventory;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!inv.getStackInSlot(i)
				.isEmpty() && AllItems.WAND_OF_SYMMETRY.isIn(inv.getStackInSlot(i))) {
				SymmetryWandItem.remove(player.world, inv.getStackInSlot(i), player, pos);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static void render(WorldRenderContext worldRenderContext) {
		MatrixStack ms = worldRenderContext.matrixStack();

		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (!AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = new BlockPos(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = MathHelper.sin((float) (AnimationTickHolder.getRenderTime() * speed)) / 5f;

			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance()
				.getBufferBuilders()
				.getEntityVertexConsumers();
			ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
			Vector3d view = info.getProjectedView();

			ms.push();
			ms.translate(-view.getX(), -view.getY(), -view.getZ());
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			ms.translate(0, yShift + .2f, 0);
			mirror.applyModelTransform(ms);
			IBakedModel model = mirror.getModel()
				.get();
			IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());

			mc.getBlockRendererDispatcher()
				.getBlockModelRenderer()
				.render(player.world, model, Blocks.AIR.getDefaultState(), pos, ms, builder, true,
					player.world.getRandom(), MathHelper.getPositionRandom(pos), OverlayTexture.DEFAULT_UV
					);

			buffer.draw();
			ms.pop();
		}
	}

	@Environment(EnvType.CLIENT)
	public static void onClientTick(Minecraft mc) {
		ClientPlayerEntity player = mc.player;

		if (mc.world == null)
			return;
		if (mc.isGamePaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);

				if (stackInSlot != null && AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					Vector3d pos = mirror.getPosition()
						.add(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vector3d speed = new Vector3d(0, r.nextDouble() * 1 / 8f, 0);
					mc.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.8f;
		Vector3d start = Vector3d.of(from).add(0.5, 0.5, 0.5);
		Vector3d end = Vector3d.of(to).add(0.5, 0.5, 0.5);
		Vector3d diff = end.subtract(start);

		Vector3d step = diff.normalize()
			.scale(density);
		int steps = (int) (diff.length() / step.length());

		Random r = new Random();
		for (int i = 3; i < steps - 1; i++) {
			Vector3d pos = start.add(step.scale(i));
			Vector3d speed = new Vector3d(0, r.nextDouble() * -40f, 0);

			Minecraft.getInstance().world.addParticle(new RedstoneParticleData(1, 1, 1, 1), pos.x, pos.y, pos.z,
				speed.x, speed.y, speed.z);
		}

		Vector3d speed = new Vector3d(0, r.nextDouble() * 1 / 32f, 0);
		Vector3d pos = start.add(step.scale(2));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);

		speed = new Vector3d(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, speed.x, speed.y,
			speed.z);
	}

}
