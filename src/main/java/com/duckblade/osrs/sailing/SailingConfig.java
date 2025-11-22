package com.duckblade.osrs.sailing;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SailingConfig.CONFIG_GROUP)
public interface SailingConfig extends Config
{

	String CONFIG_GROUP = "sailing";

	@ConfigSection(
		name = "Barracuda Trials",
		description = "Settings for Barracuda Trials",
		position = 100,
		closedByDefault = true
	)
	String SECTION_BARRACUDA_TRIALS = "barracudaTrials";

	enum ShowChartsMode
	{
		NONE,
		UNCHARTED,
		CHARTED,
		ALL,
		;
	}

	@ConfigItem(
		keyName = "showCharts",
		name = "Highlight Sea Charting Locations",
		description = "Highlight nearby sea charting locations."
	)
	default ShowChartsMode showCharts()
	{
		return ShowChartsMode.UNCHARTED;
	}

	@ConfigItem(
		keyName = "highlightTrimmableSails",
		name = "Highlight Trimmable Sails",
		description = "Highlight sails when they require trimming."
	)
	default boolean highlightTrimmableSails()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightRapids",
		name = "Highlight Rapids",
		description = "Highlight rapids."
	)
	default boolean highlightRapids()
	{
		return true;
	}

	@ConfigItem(
		keyName = "disableSailsWhenNotAtHelm",
		name = "Sails At Helm Only",
		description = "Deprioritizes sail options when not at the helm."
	)
	default boolean disableSailsWhenNotAtHelm()
	{
		return true;
	}

	@ConfigItem(
		keyName = "barracudaHighlightLostCrates",
		name = "Highlight Crates",
		description = "Highlight lost crates that need to be collected during Barracuda Trials.",
		section = SECTION_BARRACUDA_TRIALS,
		position = 1
	)
	default boolean barracudaHighlightLostCrates()
	{
		return true;
	}

	@ConfigItem(
		keyName = "barracudaHighlightLostCratesColour",
		name = "Crate Colour",
		description = "The colour to highlight lost crates.",
		section = SECTION_BARRACUDA_TRIALS,
		position = 2
	)
	@Alpha
	default Color barracudaHighlightLostCratesColour()
	{
		return Color.ORANGE;
	}

}
