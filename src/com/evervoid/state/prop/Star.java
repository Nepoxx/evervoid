package com.evervoid.state.prop;

import com.evervoid.client.graphics.geometry.MathUtils;
import com.evervoid.json.Json;
import com.evervoid.state.Color;
import com.evervoid.state.EVGameState;
import com.evervoid.state.data.SpriteData;
import com.evervoid.state.data.StarData;
import com.evervoid.state.geometry.Dimension;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.player.Player;

public class Star extends Prop
{
	/**
	 * Generates a random-type star
	 * 
	 * @param solarSystemDimension
	 *            Warning: This is the dimension of the ENTIRE solar system, not the dimension of the star. The Star will
	 *            automatically place itself in the middle of the solar system, hence the need to pass the total solar system
	 *            dimension
	 * @param state
	 *            The EverVoidGameState
	 * @return A star of a random type
	 */
	public static Star randomStar(final Dimension solarSystemDimension, final EVGameState state)
	{
		final String randomType = (String) MathUtils.getRandomElement(state.getStarTypes());
		final StarData data = state.getStarData(randomType);
		final int x = solarSystemDimension.width / 2 - data.getDimension().width / 2;
		final int y = solarSystemDimension.height / 2 - data.getDimension().height / 2;
		final GridLocation location = new GridLocation(x, y, data.getDimension());
		return new Star(state.getNextPropID(), state.getNullPlayer(), location, state.getStarData(randomType));
	}

	private final StarData aData;

	public Star(final int id, final Player player, final GridLocation location, final StarData data)
	{
		super(id, player, location, "star");
		aData = data;
	}

	public Star(final Json j, final Player player, final StarData data)
	{
		super(j, player, "star");
		aData = data;
	}

	@Override
	public Star clone()
	{
		// TODO clone
		return this;
	}

	public SpriteData getBorderSprite()
	{
		return aData.getBorderSprite();
	}

	public Color getGlowColor()
	{
		return aData.getGlowColor();
	}

	public Color getShadowColor()
	{
		return aData.getShadowColor();
	}

	public SpriteData getSprite()
	{
		return aData.getSprite();
	}

	@Override
	public Json toJson()
	{
		final Json j = super.toJson();
		return j.setStringAttribute("startype", aData.getType());
	}
}
