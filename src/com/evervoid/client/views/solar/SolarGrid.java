package com.evervoid.client.views.solar;

import java.util.HashMap;
import java.util.Map;

import com.evervoid.client.graphics.GraphicsUtils;
import com.evervoid.client.graphics.Grid;
import com.evervoid.client.graphics.GridNode;
import com.evervoid.client.graphics.geometry.AnimatedAlpha;
import com.evervoid.client.views.solar.UIProp.PropState;
import com.evervoid.state.SolarSystem;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.geometry.Point;
import com.evervoid.state.prop.Prop;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

/**
 * This class represents the grid displayed when in the solar system view.
 */
public class SolarGrid extends Grid
{
	protected static final int sCellSize = 64;
	protected final SolarGridSelection aGridHover;
	private final Map<Prop, UIProp> aProps = new HashMap<Prop, UIProp>();
	protected Prop aSelectedProp = null;
	protected final SolarSystem aSolarSystem;
	protected final SolarView aSolarSystemView;
	protected final ColorRGBA aStarGlowColor;
	protected final ShipTrailManager aTrailManager = new ShipTrailManager(this);

	/**
	 * Default constructor generating
	 * 
	 * @param view
	 *            The solar system view to generate the grid in.
	 * @param ss
	 *            The solar system represented by this grid.
	 */
	public SolarGrid(final SolarView view, final SolarSystem ss)
	{
		super(ss.getDimension(), sCellSize, sCellSize, 1, new ColorRGBA(1f, 1f, 1f, 0.2f));
		aSolarSystemView = view;
		aSolarSystem = ss;
		aStarGlowColor = GraphicsUtils.getColorRGBA(ss.getSunShadowColor());
		aGridHover = new SolarGridSelection();
		addNode(aGridHover);
	}

	/**
	 * Adds a GridNode to the Grid. Called by UIProp itself. It is (always) a UIProp that gets added, so add the corresponding
	 * mapping too.
	 */
	@Override
	protected void addGridNode(final GridNode node)
	{
		super.addGridNode(node);
		if (node instanceof UIProp) {
			final UIProp prop = (UIProp) node;
			aProps.put(prop.getProp(), prop);
		}
	}

	@Override
	public void computeTransforms()
	{
		super.computeTransforms();
		aSolarSystemView.computeGridDimensions();
	}

	/**
	 * Deletes a GridNode from the Grid. Called by UIProp itself. It us (always) a UIProp that gets deleted, so delete the
	 * corresponding mapping too.
	 */
	@Override
	protected void delGridNode(final GridNode node)
	{
		super.delGridNode(node);
		if (node instanceof UIProp) {
			final UIProp prop = (UIProp) node;
			aProps.remove(prop.getProp());
		}
	}

	AnimatedAlpha getLineAlphaAnimation()
	{
		return aLines.getNewAlphaAnimation();
	}

	/**
	 * Finds if there is a UIProp at the given point
	 * 
	 * @param position
	 *            The point to look at
	 * @return The UIProp at the given point, or null if there is no prop there
	 */
	UIProp getPropAt(final Point point)
	{
		final Prop found = aSolarSystem.getPropAt(point);
		if (found == null) {
			return null;
		}
		return aProps.get(found);
	}

	/**
	 * @return A GridLocation where the sun is located.
	 */
	public GridLocation getSunLocation()
	{
		return aSolarSystem.getSunLocation();
	}

	/**
	 * @return The glow color of the sun
	 */
	public ColorRGBA getSunShadowColor()
	{
		return aStarGlowColor;
	}

	/**
	 * @return A Trail Manager for this solar system grid.
	 */
	public ShipTrailManager getTrailManager()
	{
		return aTrailManager;
	}

	/**
	 * Handle hover events on the grid
	 * 
	 * @param position
	 *            Grid-based position that was hovered
	 * @return Whether the cursor was on the grid or not
	 */
	boolean hover(final Vector2f position)
	{
		final Point pointed = getCellAt(position);
		if (pointed == null) {
			aGridHover.fadeOut();
			return false;
		}
		else {
			aGridHover.fadeIn();
		}
		// Take care of selection square
		final UIProp prop = getPropAt(pointed);
		if (prop == null) {
			aGridHover.goTo(new GridLocation(pointed));
		}
		else {
			aGridHover.goTo(prop.getLocation());
		}
		// tmpShip.faceTowards(hoveredPoint);
		return true;
	}

	public void selectProp(final Prop prop)
	{
		if (aSelectedProp != null) {
			aProps.get(aSelectedProp).setState(PropState.INACTIVE);
		}
		aProps.get(prop).setState(PropState.SELECTED);
		aSelectedProp = prop;
	}
}
