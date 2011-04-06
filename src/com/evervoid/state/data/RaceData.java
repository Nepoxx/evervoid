package com.evervoid.state.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.evervoid.json.Json;
import com.evervoid.json.Jsonable;
import com.evervoid.state.geometry.Dimension;
import com.evervoid.state.player.Research;
import com.evervoid.state.player.ResourceAmount;

public class RaceData implements Jsonable
{
	private final Map<String, BuildingData> aBuildingData = new HashMap<String, BuildingData>();
	private final List<BuildingData> aInitialBuildings = new ArrayList<BuildingData>();
	private final ResourceAmount aInitialResources;
	private final Map<String, ResearchTree> aResearchTrees = new HashMap<String, ResearchTree>();
	private final Map<String, ShipData> aShipData = new HashMap<String, ShipData>();
	private final String aTitle;
	private final Map<String, TrailData> aTrailData = new HashMap<String, TrailData>();
	private final String aType;
	private final Map<String, WeaponData> aWeaponData = new HashMap<String, WeaponData>();

	RaceData(final String race, final Json j)
	{
		aType = race;
		aTitle = j.getStringAttribute("title");
		final Json shipJson = j.getAttribute("ships");
		for (final String ship : shipJson.getAttributes()) {
			aShipData.put(ship, new ShipData(ship, this, shipJson.getAttribute(ship)));
		}
		final Json trailJson = j.getAttribute("trails");
		for (final String trail : trailJson.getAttributes()) {
			aTrailData.put(trail, new TrailData(trail, aType, trailJson.getAttribute(trail)));
		}
		final Json weaponJson = j.getAttribute("weapons");
		for (final String weapon : weaponJson.getAttributes()) {
			aWeaponData.put(weapon, new WeaponData(weapon, aType, weaponJson.getAttribute(weapon)));
		}
		final Json researchJson = j.getAttribute("research");
		for (final String research : researchJson.getAttributes()) {
			aResearchTrees.put(research, new ResearchTree(research, aType, researchJson.getAttribute(research)));
		}
		final Json buildingJson = j.getAttribute("buildings");
		for (final String building : buildingJson.getAttributes()) {
			aBuildingData.put(building, new BuildingData(building, aType, buildingJson.getAttribute(building)));
		}
		aInitialResources = new ResourceAmount(j.getAttribute("initialResources"));
		for (final String buildType : j.getStringListAttribute("initialbuildings")) {
			final BuildingData data = getBuildingData(buildType);
			if (data != null) {
				aInitialBuildings.add(data);
			}
		}
	}

	public BuildingData getBuildingData(final String building)
	{
		return aBuildingData.get(building);
	}

	public Set<String> getBuildings()
	{
		return aBuildingData.keySet();
	}

	public List<BuildingData> getInitialBuildingData()
	{
		return aInitialBuildings;
	}

	public String getRaceIcon(final String style)
	{
		return "icons/races/" + aType + "/" + style + ".png";
	}

	public ResearchTree getResearchTree(final String researchTree)
	{
		return aResearchTrees.get(researchTree);
	}

	public SpriteData getShieldSprite(final Research research, final Dimension dimension)
	{
		// TODO - Take research into account, make it race-specific
		if (dimension.sameAs(1, 1)) {
			return new SpriteData("shields/shield_1x1.png");
		}
		if (dimension.sameAs(2, 2)) {
			return new SpriteData("shields/shield_2x2.png");
		}
		if (dimension.sameAs(3, 3)) {
			return new SpriteData("shields/shield_3x3.png");
		}
		if (dimension.sameAs(4, 4)) {
			return new SpriteData("shields/shield_3x3.png"); // FIXME
		}
		return null;
	}

	public ShipData getShipData(final String shipType)
	{
		return aShipData.get(shipType);
	}

	public Set<String> getShipTypes()
	{
		return aShipData.keySet();
	}

	public ResourceAmount getStartResources()
	{
		return aInitialResources;
	}

	public String getTitle()
	{
		return aTitle;
	}

	public TrailData getTrailData(final String trailType)
	{
		return aTrailData.get(trailType);
	}

	public String getType()
	{
		return aType;
	}

	public WeaponData getWeaponData(final String weaponType)
	{
		return aWeaponData.get(weaponType);
	}

	@Override
	public Json toJson()
	{
		final Json j = new Json();
		j.setMapAttribute("ships", aShipData);
		j.setMapAttribute("trails", aTrailData);
		j.setMapAttribute("research", aResearchTrees);
		j.setStringAttribute("title", aTitle);
		j.setAttribute("initialResources", aInitialResources);
		j.setMapAttribute("buildings", aBuildingData);
		j.setMapAttribute("weapons", aWeaponData);
		final List<String> initialBuildings = new ArrayList<String>(aInitialBuildings.size());
		for (final BuildingData data : aInitialBuildings) {
			initialBuildings.add(data.getType());
		}
		j.setStringListAttribute("initialbuildings", initialBuildings);
		return j;
	}
}
