// PORTED CREATE SOURCE

package com.simibubi.create.foundation.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

import net.minecraft.util.math.MathHelper;

import com.simibubi.create.foundation.config.util.Validatable;

public class CKinetics implements Validatable {
	@Tooltip
	public boolean disableStress = false;
	@Tooltip
	public int maxBeltLength = 20; // min 5
	@Tooltip
	public int crushingDamage = 4; // min 0
	@Tooltip
	public int maxMotorSpeed = 256; // min 64
	@Tooltip
	public int waterWheelBaseSpeed = 4; // min 1
	@Tooltip
	public int waterWheelFlowSpeed = 4; // min 1
	@Tooltip
	public int furnaceEngineSpeed = 16; // min 1
	@Tooltip
	public int maxRotationSpeed = 256; // min 64
	@Tooltip
	DeployerAggroSetting ignoreDeployerAttacks = DeployerAggroSetting.CREEPERS;
	@Tooltip
	public int kineticValidationFrequency = 60; // min 5
	@Tooltip
	public float crankHungerMultiplier = 0.01f; // min 0, max 1

	@Tooltip
	public int fanPushDistance = 20; // min 5
	@Tooltip
	public int fanPullDistance = 20; // min 5
	@Tooltip
	public int fanBlockCheckRate = 30; // min 10
	@Tooltip
	public int fanRotationArgmax = 256; // min 64
	@Tooltip
	public int generatingFanSpeed = 4; // min 0
	@Tooltip
	public int inWorldProcessingTime = 150; // min 0

	@Tooltip
	public int maxBlocksMoved = 2048; // min 1
	@Tooltip
	public int maxChassisRange = 16; // min 1
	@Tooltip
	public int maxPistonPoles = 64; // min 1
	@Tooltip
	public int maxRopeLength = 128; // min 1
	@Tooltip
	public int maxCartCouplingLength = 32; // min 1

	@Tooltip
	public float mediumSpeed = 30f; // min 0, max 4096
	@Tooltip
	public float fastSpeed = 100f;// min 0, max 65535
	@Tooltip
	public float mediumStressImpact = 4f;// min 0, max 4096
	@Tooltip
	public float highStressImpact = 8f;// min 0, max 65535
	@Tooltip
	public float mediumCapacity = 128f;// min 0, max 4096
	@Tooltip
	public float highCapacity = 512f;// min 0, max 65535

	@Override
	public void validate() throws ConfigData.ValidationException {
		maxBeltLength = Math.max(maxBeltLength, 5);
		crushingDamage = Math.max(crushingDamage, 0);
		maxMotorSpeed = Math.max(maxMotorSpeed, 64);
		waterWheelBaseSpeed = Math.max(waterWheelBaseSpeed, 1);
		waterWheelFlowSpeed = Math.max(waterWheelFlowSpeed, 1);
		furnaceEngineSpeed = Math.max(furnaceEngineSpeed, 1);
		maxRotationSpeed = Math.max(maxRotationSpeed, 64);
		kineticValidationFrequency = Math.max(kineticValidationFrequency, 5);
		crankHungerMultiplier = MathHelper.clamp(crankHungerMultiplier, 0, 1);
		fanPushDistance = Math.max(fanPushDistance, 5);
		fanPullDistance = Math.max(fanPullDistance, 5);
		fanBlockCheckRate = Math.max(fanBlockCheckRate, 10);
		fanRotationArgmax = Math.max(fanRotationArgmax, 64);
		generatingFanSpeed = Math.max(generatingFanSpeed, 0);
		inWorldProcessingTime = Math.max(inWorldProcessingTime, 0);
		maxBlocksMoved = Math.max(maxBlocksMoved, 1);
		maxChassisRange = Math.max(maxChassisRange, 1);
		maxPistonPoles = Math.max(maxPistonPoles, 1);
		maxRopeLength = Math.max(maxRopeLength, 1);
		maxCartCouplingLength = Math.max(maxCartCouplingLength, 1);
		mediumSpeed = MathHelper.clamp(mediumSpeed, 0f, 4096f);
		fastSpeed = MathHelper.clamp(fastSpeed, 0f, 65535f);
		mediumStressImpact = MathHelper.clamp(mediumStressImpact, 0f, 4096f);
		highStressImpact = MathHelper.clamp(highStressImpact, 0f, 65535f);
		mediumCapacity = MathHelper.clamp(mediumCapacity, 0f, 4096f);
		highCapacity = MathHelper.clamp(highCapacity, 0f, 65535f);
	}

	/*
		static String stats = "Configure speed/capacity levels for requirements and indicators.";
		static String rpm = "[in Revolutions per Minute]";
		static String su = "[in Stress Units]";
		static String stress = "Fine tune the kinetic stats of individual components";
	 */

	public enum DeployerAggroSetting {
		ALL, CREEPERS, NONE
	}
}
