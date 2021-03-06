package com.evervoid.client.views.solar;

import com.evervoid.client.graphics.SphericalSprite;
import com.evervoid.client.ui.RescalableControl;
import com.evervoid.client.ui.UIControl;
import com.evervoid.state.prop.Star;

public class UIStar extends UIProp
{
	private static final float sRotationSpeedPerUnit = 15;

	public static RescalableControl getStarUI(final Star star)
	{
		final SphericalSprite spr = new SphericalSprite(star.getSprite());
		spr.setRotationTime(star.getDimension().getAverageSize() * sRotationSpeedPerUnit);
		spr.setClipPixels(1);
		return new RescalableControl(spr).setAllowUpscale(false);
	}

	private final Star aStar;

	public UIStar(final SolarGrid grid, final Star star)
	{
		super(grid, star.getLocation(), star);
		aStar = star;
		buildProp();
	}

	@Override
	protected void buildSprite()
	{
		final SphericalSprite spr = new SphericalSprite(aStar.getSprite());
		spr.setRotationTime(aStar.getLocation().dimension.getAverageSize() * sRotationSpeedPerUnit);
		// All stars have a 1-pixel fixed edge, so we don't need to render the last outer pixel circle
		spr.setClipPixels(1);
		addSprite(spr);
		addSprite(aStar.getBorderSprite());
	}

	@Override
	protected void finishedMoving()
	{
		// Can't move
	}

	@Override
	protected UIControl getPanelUI()
	{
		// TODO: Make pretty star stuff?
		return null;
	}
}
