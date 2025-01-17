package com.simibubi.create.lib.extensions;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.vector.Matrix4f;

public interface Matrix4fExtensions {
	void create$set(@NotNull Matrix4f other);

	@ApiStatus.Internal
	@Contract(mutates = "this")
	void create$fromFloatArray(float[] floats);

	float[] create$writeMatrix();
}
