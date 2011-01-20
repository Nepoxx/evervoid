package com.evervoid.client.graphics;

import com.evervoid.client.graphics.geometry.AnimatedTransform.DurationMode;
import com.evervoid.client.graphics.geometry.MathUtils.MovementDelta;
import com.evervoid.client.views.solar.SolarSystemGrid;
import com.evervoid.state.GridLocation;
import com.evervoid.state.prop.Ship;
import com.jme3.math.ColorRGBA;

public class UIShip extends UIProp implements Colorable
{
	protected static enum ShipState
	{
		INACTIVE, MOVING, SELECTABLE, SELECTED;
	}

	private Sprite aColorableSprite;
	private MovementDelta aMovementDelta;
	private final Ship aShip;
	private ShipState aState = ShipState.INACTIVE;
	private ShipLinearTrail aTrail;

	public UIShip(final SolarSystemGrid grid, final Ship ship)
	{
		super(grid, ship.getLocation());
		aShip = ship;
		buildProp();
		aGridTranslation.setDuration(ship.getMovingTime());
		// Set rotation speed and mode:
		aFaceTowards.setSpeed(ship.getRotationSpeed()).setDurationMode(DurationMode.CONTINUOUS);
		setHue(GraphicsUtils.getPlayerColor(ship.getColor()));
	}

	@Override
	protected void buildSprite()
	{
		final Sprite baseSprite = new Sprite(aShip.getBaseSprite());
		addSprite(baseSprite);
		aColorableSprite = new Sprite(aShip.getColorOverlay());
		addSprite(aColorableSprite);
		aTrail = new ShipLinearTrail();
		addSprite(aTrail, Sprite.sSpriteScale * baseSprite.getWidth() + 2, 0);
		enableFloatingAnimation(1f, 2f);
	}

	@Override
	public void computeTransforms()
	{
		super.computeTransforms();
		if (aSpriteReady) {
			aTrail.setGradualState(aGridTranslation.getMovingSpeed());
		}
	}

	@Override
	public void faceTowards(final GridLocation target)
	{
		if (aState == ShipState.SELECTED) {
			super.faceTowards(target);
		}
	}

	@Override
	public void hasMoved()
	{
		super.hasMoved();
		if (aMovementDelta != null) {
			faceTowards(aMovementDelta.getAngle());
		}
		// FIXME: Should be "inactive" after moving
		// but selected for now because it's more convenient for testing
		aState = ShipState.SELECTED;
	}

	public void moveShip(final GridLocation destination)
	{
		if (aState == ShipState.SELECTED) {
			faceTowards(destination);
			aMovementDelta = MovementDelta.fromDelta(aGridLocation, destination);
			super.smoothMoveTo(destination);
			aState = ShipState.MOVING;
		}
	}

	public void select()
	{
		aState = ShipState.SELECTED;
	}

	@Override
	public void setHue(final ColorRGBA hue)
	{
		aColorableSprite.setHue(hue);
	}

	@Override
	public void setHue(final ColorRGBA hue, final float multiplier)
	{
		aColorableSprite.setHue(hue, multiplier);
	}
}
