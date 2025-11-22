package com.duckblade.osrs.sailing.features.mes;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.util.SailingUtil;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DeprioSailsOffHelm
	implements PluginLifecycleComponent
{

	private static final int FACILITY_HELM = 3;
	private static final String MENU_TARGET_SAILS = "<col=ffff>Sails";

	private final Client client;

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		return config.disableSailsWhenNotAtHelm();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!SailingUtil.isSailing(client))
		{
			// not in a boat
			return;
		}

		// todo getSailingFacility
		// todo crewmate support?
		if (client.getVarbitValue(VarbitID.SAILING_BOAT_FACILITY_LOCKEDIN) == FACILITY_HELM)
		{
			// at sails
			return;
		}

		if (MENU_TARGET_SAILS.contains(e.getTarget()))
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}

}
