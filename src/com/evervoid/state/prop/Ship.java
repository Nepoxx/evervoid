package com.evervoid.state.prop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evervoid.json.Json;
import com.evervoid.state.Color;
import com.evervoid.state.EVContainer;
import com.evervoid.state.EVGameState;
import com.evervoid.state.SolarSystem;
import com.evervoid.state.data.ShipData;
import com.evervoid.state.data.TrailData;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.observers.ShipObserver;
import com.evervoid.state.player.Player;

public class Ship extends Prop
{
	private final ShipData aData;
	private final Set<ShipObserver> aObserverList;
	private final int health;

	public Ship(final int id, final Player player, final EVContainer<Prop> container, final GridLocation location,
			final String shipType)
	{
		super(id, player, location, "ship");
		aData = aPlayer.getRaceData().getShipData(shipType);
		// Overwrite GridLocation dimension with data from ship data
		aLocation.dimension = aData.getDimension();
		aObserverList = new HashSet<ShipObserver>();
		aContainer = container;
		health = aData.getMaximumHealth();
	}

	public Ship(final Json j, final Player player, final EVGameState state)
	{
		super(j, player, "ship", state);
		aData = aPlayer.getRaceData().getShipData(j.getStringAttribute("shiptype"));
		// Overwrite GridLocation dimension with data from ship data
		aLocation.dimension = aData.getDimension();
		aObserverList = new HashSet<ShipObserver>();
		health = j.getIntAttribute("health");
	}

	public void deregisterObserver(final ShipObserver observer)
	{
		aObserverList.remove(observer);
	}

	@Override
	public void enterContainer(final EVContainer<Prop> container)
	{
		super.enterContainer(container);
		aObserverList.add((ShipObserver) container);
	}

	public Color getColor()
	{
		return aPlayer.getColor();
	}

	public ShipData getData()
	{
		return aData;
	}

	public int getSpeed()
	{
		// TODO: Get speed multiplier from research
		return aData.getSpeed();
	}

	public TrailData getTrailData()
	{
		// TODO: Make this depend on research
		// FIXME: Haaaax
		return aPlayer.getRaceData().getTrailData("engine_0");
	}

	public Set<GridLocation> getValidDestinations()
	{
		return new Pathfinder().getValidDestinations(this);
	}

	public void jumpToSolarSystem(final SolarSystem ss, final List<GridLocation> leavingMove,
			final GridLocation destinationLocation, final Portal portal)
	{
		for (final ShipObserver observer : aObserverList) {
			observer.shipJumped(aContainer, new ArrayList<GridLocation>(leavingMove), ss, portal);
		}
		leaveContainer();
		aLocation = destinationLocation;
		enterContainer(ss);
	}

	@Override
	public void leaveContainer()
	{
		super.leaveContainer();
		aObserverList.remove(aContainer);
		aContainer = null;
	}

	public void move(final List<GridLocation> path)
	{
		final GridLocation oldLocation = aLocation;
		if (path == null || path.isEmpty()) {
			System.err.println("Warning: Ship " + this + " got an empty or null path: " + path);
			return;
		}
		aLocation = path.get(path.size() - 1);
		for (final ShipObserver observer : aObserverList) {
			observer.shipMoved(this, oldLocation.clone(), new ArrayList<GridLocation>(path));
		}
	}

	public void registerObserver(final ShipObserver sObserver)
	{
		aObserverList.add(sObserver);
	}

	@Override
	public Json toJson()
	{
		final Json j = super.toJson();
		j.setIntAttribute("health", health);
		return j.setStringAttribute("shiptype", aData.getType());
	}
}
