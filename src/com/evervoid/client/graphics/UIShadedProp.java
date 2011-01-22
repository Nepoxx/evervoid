package com.evervoid.client.graphics;

import com.evervoid.client.graphics.geometry.MathUtils;
import com.evervoid.client.views.solar.SolarSystemGrid;
import com.evervoid.state.GridLocation;
import com.jme3.math.Vector2f;

public abstract class UIShadedProp extends UIProp
{
	private Shadable aShade = null;

	public UIShadedProp(final SolarSystemGrid grid, final GridLocation location)
	{
		super(grid, location);
	}

	@Override
	protected void populateTransforms()
	{
		super.populateTransforms();
		if (aShade != null) {
			final GridLocation sunLocation = getSolarSystemGrid().getSunLocation();
			final Vector2f sunDelta = aGrid.getCellCenter(sunLocation).subtract(aGridTranslation.getTranslation2f());
			aShade.setShadeAngle(MathUtils.getAngleTowards(sunDelta) - aFaceTowards.getRotation());
			aShade.setShadePortion(sunDelta.length() / aGrid.getHalfDiagonal());
		}
	}

	protected void setShade(final Shadable shade)
	{
		aShade = shade;
	}
}
