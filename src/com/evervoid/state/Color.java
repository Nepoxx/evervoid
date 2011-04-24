package com.evervoid.state;

import com.evervoid.json.Json;
import com.evervoid.json.Jsonable;
import com.jme3.math.FastMath;

public class Color implements Jsonable
{
	/**
	 * @return A completely random color.
	 */
	public static Color random()
	{
		return new Color(FastMath.rand.nextFloat(), FastMath.rand.nextFloat(), FastMath.rand.nextFloat(), 1, "random["
				+ FastMath.rand.nextDouble() + "]");
	}

	public final float alpha;
	public final float blue;
	public final float green;
	public final String name;
	public final float red;

	/**
	 * Creates a Color with the passed parameters.
	 * 
	 * @param red
	 *            The red value of this Color.
	 * @param green
	 *            The green value of this Color.
	 * @param blue
	 *            The blue value of this Color.
	 * @param alpha
	 *            The alpha value of this Color.
	 * @param colorName
	 *            The name associated with this Color.
	 */
	public Color(final float red, final float green, final float blue, final float alpha, final String colorName)
	{
		this.blue = blue;
		this.green = green;
		this.red = red;
		this.alpha = alpha;
		name = colorName;
	}

	/**
	 * Creates an unnamed Color based on the rgb values in the Json
	 * 
	 * @param j
	 */
	public Color(final Json j)
	{
		this("noname", j);
	}

	/**
	 * Creates a Color based on the rgb values in the Json.
	 * 
	 * @param colorName
	 *            The name this color will have.
	 * @param j
	 *            The Json object containing rgb alpha values.
	 */
	public Color(final String colorName, final Json j)
	{
		// get the rgba values from the Json
		red = j.getFloatAttribute("red");
		green = j.getFloatAttribute("green");
		blue = j.getFloatAttribute("blue");
		alpha = j.getFloatAttribute("alpha");
		// if parameter name is not null, use it. Otherwise attempt to get one from the Json.
		if (colorName == null && j.hasAttribute("name") && j.getStringAttribute("name") != null
				&& !j.getStringAttribute("name").isEmpty()) {
			name = j.getStringAttribute("name");
		}
		else {
			name = colorName;
		}
	}

	@Override
	public Json toJson()
	{
		return new Json().setFloatAttribute("red", red).setFloatAttribute("green", green).setFloatAttribute("blue", blue)
				.setFloatAttribute("alpha", alpha).setStringAttribute("name", name);
	}

	@Override
	public String toString()
	{
		return "Color " + name + " of rgba(" + red + ", + " + green + ", " + blue + ", " + alpha + ")";
	}
}
