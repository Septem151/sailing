package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BarracudaTrialHelper
	extends Overlay
	implements PluginLifecycleComponent
{

	private static final Set<Integer> LOST_CARGO_IDS = ImmutableSet.of(
		ObjectID.SAILING_BT_GWENITH_GLIDE_COLLECTABLE_SUPPLIES,
		ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_SUPPLIES,
		ObjectID.SAILING_BT_TEMPOR_TANTRUM_COLLECTABLE_SUPPLIES
	);

	private final Set<GameObject> lostCargo = new HashSet<>();
	private Color crateColour;

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		crateColour = config.barracudaHighlightLostCratesColour();
		return config.barracudaHighlightLostCrates();
	}

	@Override
	public void shutDown()
	{
		lostCargo.clear();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject o = e.getGameObject();
		if (LOST_CARGO_IDS.contains(o.getId()))
		{
			lostCargo.add(o);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		lostCargo.remove(e.getGameObject());
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		for (GameObject o : lostCargo)
		{
			OverlayUtil.renderTileOverlay(g, o, "", crateColour);
		}

		return null;
	}
}
