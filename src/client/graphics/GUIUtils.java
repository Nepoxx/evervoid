package client.graphics;

import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;

import com.jme3.math.Vector2f;

public class GUIUtils
{
	public static enum Border
	{
		DOWN, LEFT, RIGHT, UP;
		public float getXDirection()
		{
			switch (this)
			{
				case LEFT:
					return -1;
				case RIGHT:
					return 1;
			}
			return 0;
		}

		public float getYDirection()
		{
			switch (this)
			{
				case DOWN:
					return -1;
				case UP:
					return 1;
			}
			return 0;
		}
	};

	public static Map<Border, Float> isInBorder(final Vector2f point, final Rectangle rectangle, final float boundary)
	{
		final Map<Border, Float> borderRatios = new EnumMap<Border, Float>(Border.class);
		final float x = point.x;
		final float y = point.y;
		final float borderWidth = rectangle.width * boundary;
		final float borderHeight = rectangle.height * boundary;
		if (rectangle.x <= x && x <= rectangle.x + borderWidth)
		{
			borderRatios.put(Border.LEFT, 1 - (x - rectangle.x) / borderWidth);
		}
		else if (rectangle.x + rectangle.width - borderWidth <= x && x <= rectangle.x + rectangle.width)
		{
			borderRatios.put(Border.RIGHT, 1 - (rectangle.x + rectangle.width - x) / borderWidth);
		}
		if (rectangle.y <= y && y <= rectangle.y + borderHeight)
		{
			borderRatios.put(Border.DOWN, 1 - (y - rectangle.y) / borderHeight);
		}
		else if (rectangle.y + rectangle.height - borderHeight <= y && y <= rectangle.y + rectangle.height)
		{
			borderRatios.put(Border.UP, 1 - (rectangle.y + rectangle.height - y) / borderHeight);
		}
		return borderRatios;
	}
}
