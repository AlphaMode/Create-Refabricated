package com.simibubi.create.foundation.command;

import java.util.Collection;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.content.contraptions.components.structureMovement.AssemblyException;
import com.simibubi.create.content.contraptions.components.structureMovement.IDisplayAssemblyExceptions;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class HighlightCommand {

	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("highlight")
			.requires(cs -> cs.hasPermissionLevel(0))
			.then(Commands.argument("pos", BlockPosArgument.blockPos())
				.then(Commands.argument("players", EntityArgument.players())
					.executes(ctx -> {
						Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(ctx, "players");
						BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");

						for (ServerPlayerEntity p : players) {
							AllPackets.channel.sendToClient(new HighlightPacket(pos), p);
//							AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> p), new HighlightPacket(pos));
						}

						return players.size();
					}))
				// .requires(AllCommands.sourceIsPlayer)
				.executes(ctx -> {
					BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
					AllPackets.channel.sendToClient(new HighlightPacket(pos), (ServerPlayerEntity) ctx.getSource().getEntity());
//					AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) ctx.getSource()
//						.getEntity()), new HighlightPacket(pos));

					return Command.SINGLE_SUCCESS;
				}))
			// .requires(AllCommands.sourceIsPlayer)
			.executes(ctx -> {
				ServerPlayerEntity player = ctx.getSource()
					.asPlayer();
				return highlightAssemblyExceptionFor(player, ctx.getSource());
			});

	}

	private static void sendMissMessage(CommandSource source) {
		source.sendFeedback(
			new StringTextComponent("Try looking at a Block that has failed to assemble a Contraption and try again."),
			true);
	}

	private static int highlightAssemblyExceptionFor(ServerPlayerEntity player, CommandSource source) {
		double distance = player.getAttribute(ReachEntityAttributes.REACH)
			.getValue();
		Vector3d start = player.getEyePosition(1);
		Vector3d look = player.getLook(1);
		Vector3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
		World world = player.world;

		BlockRayTraceResult ray = world.rayTraceBlocks(
			new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
		if (ray.getType() == RayTraceResult.Type.MISS) {
			sendMissMessage(source);
			return 0;
		}

		BlockPos pos = ray.getPos();
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof IDisplayAssemblyExceptions)) {
			sendMissMessage(source);
			return 0;
		}

		IDisplayAssemblyExceptions display = (IDisplayAssemblyExceptions) te;
		AssemblyException exception = display.getLastAssemblyException();
		if (exception == null) {
			sendMissMessage(source);
			return 0;
		}

		if (!exception.hasPosition()) {
			source.sendFeedback(new StringTextComponent("Can't highlight a specific position for this issue"), true);
			return Command.SINGLE_SUCCESS;
		}

		BlockPos p = exception.getPosition();
		String command = "/create highlight " + p.getX() + " " + p.getY() + " " + p.getZ();
		return player.server.getCommandManager()
			.handleCommand(source, command);
	}
}
