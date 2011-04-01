package com.evervoid.state.observers;

import com.evervoid.state.building.Building;
import com.evervoid.state.player.Player;
import com.evervoid.state.prop.Planet;
import com.evervoid.state.prop.Ship;

public interface PlanetObserver
{
	public void buildingConstructed(Building building, int progress);

	public void captured(Player player);

	public void planetChangedOwner(Planet planet);

	public void shipConstructed(Ship ship, int progress);
}
