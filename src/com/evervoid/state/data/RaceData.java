package com.evervoid.state.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.evervoid.json.Json;
import com.evervoid.json.Jsonable;
import com.evervoid.state.player.ResourceAmount;

public class RaceData implements Jsonable
{
	private final Map<String, BuildingData> aBuildingData = new HashMap<String, BuildingData>();
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
			aShipData.put(ship, new ShipData(ship, race, shipJson.getAttribute(ship)));
		}
		final Json trailJson = j.getAttribute("trails");
		for (final String trail : trailJson.getAttributes()) {
			aTrailData.put(trail, new TrailData(trail, race, trailJson.getAttribute(trail)));
		}
		final Json weaponJson = j.getAttribute("weapons");
		for (final String weapon : weaponJson.getAttributes()) {
			aWeaponData.put(weapon, new WeaponData(weapon, race, weaponJson.getAttribute(weapon)));
		}
		final Json researchJson = j.getAttribute("research");
		for (final String research : researchJson.getAttributes()) {
			aResearchTrees.put(research, new ResearchTree(research, race, researchJson.getAttribute(research)));
		}
		final Json buildingJson = j.getAttribute("buildings");
		for (final String building : buildingJson.getAttributes()) {
			aBuildingData.put(building, new BuildingData(building, buildingJson.getAttribute(building)));
		}
		aInitialResources = new ResourceAmount(j.getAttribute("initialResources"));
	}

	public BuildingData getBuildingData(final String building)
	{
		return aBuildingData.get(building);
	}

	public Set<String> getBuildings()
	{
		return aBuildingData.keySet();
	}

	public String getRaceIcon(final String style)
	{
		return "icons/races/" + aType + "/" + style + ".png";
	}

	public ResearchTree getResearchTree(final String researchTree)
	{
		return aResearchTrees.get(researchTree);
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
		return j;
	}
}
