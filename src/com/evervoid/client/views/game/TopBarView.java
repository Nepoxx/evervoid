package com.evervoid.client.views.game;

import com.evervoid.client.EverVoidClient;
import com.evervoid.client.graphics.Sizeable;
import com.evervoid.client.graphics.geometry.Transform;
import com.evervoid.client.ui.ImageControl;
import com.evervoid.client.ui.PlainRectangle;
import com.evervoid.client.views.Bounds;
import com.evervoid.client.views.ComposedView;
import com.evervoid.state.geometry.Dimension;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class TopBarView extends ComposedView implements Sizeable
{
	private PlainRectangle aBlocker = null;
	private final ImageControl aLeftSprite;
	private final ImageControl aMiddleConnector;
	private final ImageControl aRightSprite;
	private final Transform aScreenOffset;

	protected TopBarView()
	{
		aLeftSprite = new ImageControl("ui/topbar/left.png");
		aMiddleConnector = new ImageControl("ui/topbar/middle.png", false);
		aRightSprite = new ImageControl("ui/topbar/right.png");
		addNode(aLeftSprite);
		addNode(aMiddleConnector);
		addNode(aRightSprite);
		aScreenOffset = getNewTransform();
		resolutionChanged();
	}

	@Override
	public Vector2f getDimensions()
	{
		return new Vector2f(getWidth(), getHeight());
	}

	@Override
	public float getHeight()
	{
		return aMiddleConnector.getHeight();
	}

	@Override
	public float getWidth()
	{
		// Don't add up all widths; simply get the offset of the right corner and add the width of the right corner
		return aRightSprite.getOffset().x + aRightSprite.getWidth();
	}

	@Override
	public void resolutionChanged()
	{
		super.resolutionChanged();
		final Dimension screen = EverVoidClient.getWindowDimension();
		setBounds(new Bounds(0, screen.height - getHeight(), screen.width, getHeight()));
	}

	@Override
	public void setBounds(final Bounds bounds)
	{
		super.setBounds(bounds);
		delNode(aBlocker);
		aBlocker = new PlainRectangle(new Vector3f(0, 0, -1), bounds.width, bounds.height, ColorRGBA.Black);
		addNode(aBlocker);
		aScreenOffset.translate(bounds.x, bounds.y, 10000);
		aRightSprite.setOffset(bounds.width - aRightSprite.getWidth(), 0);
		final float middleWidth = bounds.width - aLeftSprite.getWidth() - aRightSprite.getWidth();
		aMiddleConnector.setOffset(aLeftSprite.getWidth(), 0).setLength(middleWidth);
	}
}