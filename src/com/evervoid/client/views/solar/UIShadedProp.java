package com.evervoid.client.views.solar;

import com.evervoid.client.graphics.Shadable;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.prop.Prop;
import com.evervoid.utils.MathUtils;
import com.jme3.math.Vector2f;

public abstract class UIShadedProp extends UIProp
{
	private Shadable aShade = null;

	public UIShadedProp(final SolarGrid grid, final GridLocation location, final Prop prop)
	{
		super(grid, location, prop);
	}

	@Override
	protected void populateTransforms()
	{
		super.populateTransforms();
		if (aShade != null) {
			final GridLocation sunLocation = getSolarSystemGrid().getSunLocation();
			final Vector2f sunDelta = aGrid.getCellCenter(sunLocation).subtract(aGridTranslation.getTranslation2f());
			if (aFaceTowards != null) {
				final Float angle = MathUtils.getAngleTowards(sunDelta);
				// Angle may be null if we're at the same location as the star for some reason
				if (angle != null) {
					aShade.setShadeAngle(angle - aFaceTowards.getRotationPitch());
				}
			}
			aShade.setShadePortion(sunDelta.length() / aGrid.getHalfDiagonal());
		}
	}

	protected void setShade(final Shadable shade)
	{
		aShade = shade;
		if (aShade != null) {
			aShade.setShadeColor(getSolarSystemGrid().getSunShadowColor());
		}
	}
}
