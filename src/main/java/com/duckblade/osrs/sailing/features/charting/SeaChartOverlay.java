package com.duckblade.osrs.sailing.features.charting;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.util.SailingUtil;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
public class SeaChartOverlay
	extends Overlay
	implements PluginLifecycleComponent
{

	private static final Color COLOR_CHARTABLE_COMPLETED = ColorUtil.colorWithAlpha(Color.YELLOW, 127);
	private static final Color COLOR_CHARTABLE_INCOMPLETE = Color.GREEN;

	private final Client client;
	private final ItemManager itemManager;
	private final SailingConfig config;

	private final Map<WorldPoint, SeaChartTask> tasksByLocation = new HashMap<>();
	private final Map<Integer, List<SeaChartTask>> tasksByGameObject = new HashMap<>();
	private final Map<Integer, List<SeaChartTask>> tasksByNpc = new HashMap<>();

	private final Map<GameObject, SeaChartTask> chartObjects = new HashMap<>();
	private final Map<NPC, SeaChartTask> chartNpcs = new HashMap<>();

	@Inject
	public SeaChartOverlay(Client client, ItemManager itemManager, SailingConfig config)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		return config.showCharts() != SailingConfig.ShowChartsMode.NONE;
	}

	public void startUp()
	{
		for (SeaChartTask task : SeaChartTask.values())
		{
			if (task.getLocation() != null)
			{
				tasksByLocation.put(task.getLocation(), task);
			}
			if (task.getObjectId() != -1)
			{
				tasksByGameObject.computeIfAbsent(task.getObjectId(), ArrayList::new).add(task);
			}
			else if (task.getNpcId() != -1)
			{
				tasksByNpc.computeIfAbsent(task.getNpcId(), ArrayList::new).add(task);
			}
		}
	}

	public void shutDown()
	{
		chartNpcs.clear();
		chartObjects.clear();
		tasksByLocation.clear();
		tasksByGameObject.clear();
		tasksByNpc.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		SailingConfig.ShowChartsMode mode = config.showCharts();
		if (!SailingUtil.isSailing(client) || mode == SailingConfig.ShowChartsMode.NONE)
		{
			return null;
		}

		for (Map.Entry<GameObject, SeaChartTask> tracked : chartObjects.entrySet())
		{
			GameObject obj = tracked.getKey();
			SeaChartTask task = tracked.getValue();

			boolean completed = isTaskCompleted(task);
			if ((completed && mode == SailingConfig.ShowChartsMode.UNCHARTED) ||
				(!completed && mode == SailingConfig.ShowChartsMode.CHARTED))
			{
				continue;
			}

			Polygon poly = obj.getCanvasTilePoly();
			if (poly != null)
			{
				Color color = completed ? COLOR_CHARTABLE_COMPLETED : COLOR_CHARTABLE_INCOMPLETE;
				OverlayUtil.renderPolygon(graphics, poly, color);
			}
			OverlayUtil.renderImageLocation(client, graphics, obj.getLocalLocation(), getTaskSprite(task), 0);
		}

		for (Map.Entry<NPC, SeaChartTask> tracked : chartNpcs.entrySet())
		{
			NPC npc = tracked.getKey();
			SeaChartTask task = tracked.getValue();

			boolean completed = isTaskCompleted(task);
			if (completed && mode == SailingConfig.ShowChartsMode.UNCHARTED)
			{
				continue;
			}

			Color color = isTaskCompleted(task) ? Color.YELLOW : Color.GREEN;
			OverlayUtil.renderActorOverlayImage(graphics, npc, getTaskSprite(task), color, npc.getLogicalHeight() / 2);
		}

		return null;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject o = e.getGameObject();
		SeaChartTask task = findTask(o);
		if (task != null)
		{
			chartObjects.put(o, task);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		chartObjects.remove(e.getGameObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		NPC npc = e.getNpc();
		SeaChartTask task = findTask(npc);
		if (task != null)
		{
			chartNpcs.put(npc, task);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e)
	{
		chartNpcs.remove(e.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			chartObjects.clear();
			chartNpcs.clear();
		}
	}

	private SeaChartTask findTask(GameObject obj)
	{
		List<SeaChartTask> tasks = tasksByGameObject.get(obj.getId());
		if (tasks == null || tasks.isEmpty())
		{
			return null;
		}
		if (tasks.size() == 1)
		{
			return tasks.get(0);
		}

		WorldPoint wp = obj.getWorldLocation();
		SeaChartTask task = tasksByLocation.get(wp);
		if (task != null)
		{
			return task;
		}

		for (int x = -5; x <= 5; x++)
		{
			for (int y = -5; y <= 5; y++)
			{
				SeaChartTask nearby = tasksByLocation.get(new WorldPoint(wp.getX() + x, wp.getY() + y, 0));
				if (nearby != null && nearby.getObjectId() == obj.getId())
				{
					log.debug("scan happened for game object {} @ {} = task {}", obj.getId(), obj.getWorldLocation(), nearby.getTaskId());
					return nearby;
				}
			}
		}

		log.warn("No task found for game object {} @ {}", obj.getId(), obj.getWorldLocation());
		return null;
	}

	private SeaChartTask findTask(NPC npc)
	{
		List<SeaChartTask> tasks = tasksByGameObject.get(npc.getId());
		if (tasks == null || tasks.isEmpty())
		{
			return null;
		}
		if (tasks.size() == 1)
		{
			return tasks.get(0);
		}

		WorldPoint wp = npc.getWorldLocation();
		SeaChartTask task = tasksByLocation.get(wp);
		if (task != null)
		{
			return task;
		}

		for (int x = -5; x <= 5; x++)
		{
			for (int y = -5; y <= 5; y++)
			{
				SeaChartTask nearby = tasksByLocation.get(new WorldPoint(wp.getX() + x, wp.getY() + y, 0));
				if (nearby != null && nearby.getNpcId() == npc.getId())
				{
					log.debug("scan required for game object {} @ {} = task {}", npc.getId(), npc.getWorldLocation(), nearby.getTaskId());
					return nearby;
				}
			}
		}

		log.warn("No task found for game object {} @ {}", npc.getId(), npc.getWorldLocation());
		return null;
	}

	private BufferedImage getTaskSprite(SeaChartTask task)
	{
		switch (task.getObjectId())
		{
			case ObjectID.SAILING_CHARTING_HINT_MARKER_SPYGLASS:
				return itemManager.getImage(ItemID.SAILING_CHARTING_SPYGLASS);

			case ObjectID.SAILING_CHARTING_HINT_MARKER_DUCK:
				return itemManager.getImage(ItemID.SAILING_CHARTING_CURRENT_DUCK);

			case ObjectID.SAILING_CHARTING_DRINK_CRATE:
				return itemManager.getImage(ItemID.SAILING_CHARTING_CROWBAR);

			case -1:
				break;

			default:
				return itemManager.getImage(ItemID.SAILING_LOG_INITIAL);
		}

		switch (task.getNpcId())
		{
			case NpcID.SAILING_CHARTING_MERMAID_GUIDE_1:
			case NpcID.SAILING_CHARTING_MERMAID_GUIDE_2:
			case NpcID.SAILING_CHARTING_MERMAID_GUIDE_3:
			case NpcID.SAILING_CHARTING_MERMAID_GUIDE_4:
			case NpcID.SAILING_CHARTING_MERMAID_GUIDE_5:
				return itemManager.getImage(ItemID.HUNDRED_PIRATE_DIVING_HELMET);

			case NpcID.SAILING_CHARTING_WEATHER_TROLL:
				return itemManager.getImage(ItemID.SAILING_CHARTING_WEATHER_STATION_EMPTY);

			default:
				return itemManager.getImage(ItemID.SAILING_LOG_INITIAL);
		}
	}

	private boolean isTaskCompleted(SeaChartTask task)
	{
		return client.getVarbitValue(task.getCompletionVarb()) != 0;
	}
}
