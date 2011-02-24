package com.evervoid.client.views;

import com.evervoid.client.graphics.geometry.Rectangle;

public class Bounds
{
	public final int height;
	public final int width;
	public final int x;
	public final int y;

	public Bounds(final float x, final float y, final float width, final float height)
	{
		this((int) x, (int) y, (int) width, (int) height);
	}

	public Bounds(final int x, final int y, final int width, final int height)
	{
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	@Override
	public Bounds clone()
	{
		return new Bounds(x, y, width, height);
	}

	public Rectangle getRectangle()
	{
		return new Rectangle(x, y, width, height);
	}

	@Override
	public String toString()
	{
		return "Bounds[" + x + "; " + y + " @ " + width + "x" + height + "]";
	}
}
