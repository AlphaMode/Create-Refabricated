package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class VoxelShaper {

	private Map<Direction, VoxelShape> shapes = new HashMap<>();

	public VoxelShape get(Direction direction) {
		return shapes.get(direction);
	}

	public VoxelShape get(Axis axis) {
		return shapes.get(axisAsFace(axis));
	}

	public static VoxelShaper forHorizontal(VoxelShape southShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction facing : Direction.values()) {
			if (facing.getAxis().isVertical())
				continue;
			voxelShaper.shapes.put(facing, rotatedCopy(southShape, 0, (int) -facing.getHorizontalAngle()));
		}
		return voxelShaper;
	}

	public static VoxelShaper forHorizontalAxis(VoxelShape zShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Axis axis : Axis.values()) {
			if (axis.isVertical())
				continue;
			Direction facing = axisAsFace(axis);
			voxelShaper.shapes.put(facing, rotatedCopy(zShape, 0, (int) -facing.getHorizontalAngle()));
		}
		return voxelShaper;
	}

	public static VoxelShaper forRotatedPillar(VoxelShape zShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Axis axis : Axis.values()) {
			Direction facing = axisAsFace(axis);
			voxelShaper.shapes.put(facing, rotatedCopy(zShape, 0, (int) -facing.getHorizontalAngle()));
		}
		return voxelShaper;
	}

	public static VoxelShaper forDirectional(VoxelShape southShape) {
		VoxelShaper voxelShaper = new VoxelShaper();
		for (Direction facing : Direction.values()) {
			int rotX = facing.getAxis().isVertical() ? (facing == Direction.UP ? 270 : 90) : 0;
			int rotY = facing.getAxis().isVertical() ? 0 : (int) -facing.getHorizontalAngle();
			voxelShaper.shapes.put(facing, rotatedCopy(southShape, rotX, rotY));
		}
		return voxelShaper;
	}

	public VoxelShaper withVerticalShapes(VoxelShape upShape) {
		shapes.put(Direction.UP, upShape);
		shapes.put(Direction.DOWN, rotatedCopy(upShape, 180, 0));
		return this;
	}

	private static Direction axisAsFace(Axis axis) {
		return Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
	}

	private static VoxelShape rotatedCopy(VoxelShape shape, int rotX, int rotY) {
		MutableObject<VoxelShape> result = new MutableObject<>(VoxelShapes.empty());

		shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			Vec3d center = new Vec3d(8, 8, 8);
			Vec3d v1 = new Vec3d(x1, y1, z1).scale(16).subtract(center);
			Vec3d v2 = new Vec3d(x2, y2, z2).scale(16).subtract(center);

			v1 = VecHelper.rotate(v1, rotX, Axis.X);
			v1 = VecHelper.rotate(v1, rotY, Axis.Y).add(center);
			v2 = VecHelper.rotate(v2, rotX, Axis.X);
			v2 = VecHelper.rotate(v2, rotY, Axis.Y).add(center);

			VoxelShape rotated = Block.makeCuboidShape(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
			result.setValue(VoxelShapes.or(result.getValue(), rotated));
		});

		return result.getValue();
	}

}