package com.duckblade.osrs.sailing.module;

import com.duckblade.osrs.sailing.features.barracudatrials.BarracudaTrialHelper;
import com.duckblade.osrs.sailing.features.util.BoatTracker;
import com.duckblade.osrs.sailing.features.LuffOverlay;
import com.duckblade.osrs.sailing.features.RapidsOverlay;
import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.charting.SeaChartOverlay;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class SailingModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ComponentManager.class);
	}

	@Provides
	Set<PluginLifecycleComponent> lifecycleComponents(
		BarracudaTrialHelper barracudaTrialHelper,
		BoatTracker boatTracker,
		LuffOverlay luffOverlay,
		RapidsOverlay rapidsOverlay,
		SeaChartOverlay seaChartOverlay
	)
	{
		return ImmutableSet.of(
			barracudaTrialHelper,
			boatTracker,
			luffOverlay,
			rapidsOverlay,
			seaChartOverlay
		);
	}

	@Provides
	@Singleton
	SailingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SailingConfig.class);
	}

}
