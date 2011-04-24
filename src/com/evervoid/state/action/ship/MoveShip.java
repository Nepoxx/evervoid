package com.evervoid.state.action.ship;

import java.util.List;

import com.evervoid.json.Json;
import com.evervoid.state.EVGameState;
import com.evervoid.state.SolarSystem;
import com.evervoid.state.action.IllegalEVActionException;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.geometry.Point;
import com.evervoid.state.prop.Pathfinder;
import com.evervoid.state.prop.Prop;
import com.evervoid.state.prop.Ship;
import com.evervoid.state.prop.ShipPath;
import com.evervoid.utils.EVContainer;

public class MoveShip extends ShipAction
{
	private GridLocation aDestination;
	private ShipPath aFinalPath = null;

	public MoveShip(final Json j, final EVGameState state) throws IllegalEVActionException
	{
		super(j, state);
		aDestination = new GridLocation(j.getAttribute("destination"));
	}

	public MoveShip(final Ship ship, final Point destination) throws IllegalEVActionException
	{
		super(ship);
		aDestination = new GridLocation(destination, ship.getData().getDimension());
	}

	@Override
	public boolean execute()
	{
		if (getShip().isDead()) { // Don't even try to move it the ship is dead
			return false;
		}
		if (!isValid()) {
			aDestination = aDestination.getClosest(getShip().getValidDestinations(), getShip().getLocation());
			aFinalPath = null;
		}
		executeAction();
		return true;
	}

	@Override
	protected void executeAction()
	{
		getShip().move(aDestination, getFinalPath());
	}

	@Override
	public String getDescription()
	{
		return "is moving";
	}

	public GridLocation getDestination()
	{
		return aDestination;
	}

	public ShipPath getFinalPath()
	{
		if (aFinalPath == null) {
			aFinalPath = new ShipPath(getShip().getLocation(), aDestination, getSamplePath());
		}
		return aFinalPath;
	}

	/**
	 * @return A POSSIBLE path for the ship to the destination. Does not correspond to the final path at all
	 */
	public List<GridLocation> getSamplePath()
	{
		return new Pathfinder().findPath(getShip(), aDestination);
	}

	@Override
	public boolean isValidShipAction()
	{
		final EVContainer<Prop> container = getShip().getContainer();
		if (!(container instanceof SolarSystem)) {
			// Ship not in solar system
			return false;
		}
		return getShip().getValidDestinations().contains(aDestination);
	}

	@Override
	public Json toJson()
	{
		return super.toJson().setAttribute("destination", aDestination);
	}
}
