package com.duckblade.osrs.sailing.features.util;

import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SailingUtil
{

	public static boolean isSailing(Client client)
	{
		return client.getLocalPlayer() != null &&
			!client.getLocalPlayer().getWorldView().isTopLevel();
	}

}
