package com.evervoid.state.prop;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evervoid.json.Json;
import com.evervoid.state.EVGameState;
import com.evervoid.state.building.Building;
import com.evervoid.state.data.PlanetData;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.observers.PlanetObserver;
import com.evervoid.state.player.Player;
import com.evervoid.state.player.ResourceAmount;

public class Planet extends Prop
{
	private Set<Building> aBuildings;
	private final PlanetData aData;
	private final Set<PlanetObserver> aObserverSet;

	public Planet(final int id, final Player player, final GridLocation location, final String type, final EVGameState state)
	{
		super(id, player, location, "planet", state);
		aData = state.getPlanetData(type);
		aLocation.dimension = aData.getDimension();
		aObserverSet = new HashSet<PlanetObserver>();
		aBuildings = new HashSet<Building>();
	}

	public Planet(final Json j, final Player player, final PlanetData data, final EVGameState state)
	{
		super(j, player, "planet", state);
		aData = data;
		aObserverSet = new HashSet<PlanetObserver>();
		aBuildings = new HashSet<Building>();
		final List<Json> buildingsJson = j.getListAttribute("buildings");
		for (final Json building : buildingsJson) {
			aBuildings.add(new Building(building, state));
		}
	}

	public void addBuilding(final Building building)
	{
		// TODO - check if the planet can build it
		// check that there is enough room to build
		aBuildings.add(building);
	}

	public void deleteBuildings()
	{
		for (final Building b : aBuildings) {
			b.deregister();
		}
		aBuildings = new HashSet<Building>();
	}

	public void deregisterObserver(final PlanetObserver pObserver)
	{
		aObserverSet.remove(pObserver);
	}

	public PlanetData getData()
	{
		return aData;
	}

	public ResourceAmount getResourceRate()
	{
		return aData.getResourceRate();
	}

	public void registerObserver(final PlanetObserver pObserver)
	{
		aObserverSet.add(pObserver);
	}

	@Override
	public Json toJson()
	{
		final Json j = super.toJson();
		j.setStringAttribute("planettype", aData.getType());
		j.setListAttribute("buildings", aBuildings);
		return j;
	}
}
