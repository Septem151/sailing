package com.duckblade.osrs.sailing.module;

import com.duckblade.osrs.sailing.SailingConfig;

public interface PluginLifecycleComponent
{

	default boolean isEnabled(SailingConfig config)
	{
		return true;
	}

	default void startUp()
	{
	}

	default void shutDown()
	{
	}

}
