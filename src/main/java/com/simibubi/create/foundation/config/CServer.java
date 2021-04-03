// PORTED CREATE SOURCE

package com.simibubi.create.foundation.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import com.simibubi.create.foundation.config.util.Validatable;

public class CServer implements Validatable {
	// infrastructure           public ConfigGroup infrastructure = group(0, "infrastructure", Comments.infrastructure);
	@ConfigEntry.Gui.Tooltip(count = 2)
	public int tickrateSyncTimer = 20; // min 5

	@Override
	public void validate() throws ConfigData.ValidationException {
		tickrateSyncTimer = Math.max(tickrateSyncTimer, 5);
	}
}
