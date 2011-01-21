package com.evervoid.client.graphics;

import com.evervoid.client.EverNode;
import com.evervoid.client.graphics.materials.AlphaTextured;
import com.evervoid.gamedata.OffsetSprite;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

public class Sprite extends EverNode implements Sizeable
{
	public static double sGlobalSpriteDepth = 0f;
	public static final float sSpriteScale = 2f;

	public static float getNewZDepth()
	{
		final float current = (float) sGlobalSpriteDepth;
		sGlobalSpriteDepth += 0.1;
		return current;
	}

	private final AlphaTextured aMaterial;

	public Sprite(final OffsetSprite offSprite)
	{
		super();
		aMaterial = new AlphaTextured(offSprite.sprite);
		final Quad q = new Quad(aMaterial.getWidth() * Sprite.sSpriteScale, aMaterial.getHeight() * Sprite.sSpriteScale);
		final Geometry g = new Geometry("Sprite-" + offSprite.sprite + " @ " + hashCode(), q);
		g.setMaterial(aMaterial);
		final EverNode image = new EverNode(g);
		addNode(image);
		// Offset image so that the origin is around the center of the image
		image.getNewTransform()
				.translate(-aMaterial.getWidth() * Sprite.sSpriteScale / 2 + offSprite.x,
						-aMaterial.getHeight() * Sprite.sSpriteScale / 2 + offSprite.y).commit();
	}

	public Sprite(final String image)
	{
		this(new OffsetSprite(image));
	}

	public Sprite(final String sprite, final int x, final int y)
	{
		this(new OffsetSprite(sprite, x, y));
	}

	@Override
	public Vector2f getDimensions()
	{
		return aMaterial.getDimensions();
	}

	@Override
	public float getHeight()
	{
		return aMaterial.getHeight();
	}

	@Override
	public float getWidth()
	{
		return aMaterial.getWidth();
	}

	@Override
	protected void setAlpha(final float alpha)
	{
		aMaterial.setAlpha(alpha);
	}

	public void setHue(final ColorRGBA hue)
	{
		aMaterial.setHue(hue);
	}

	public void setHue(final ColorRGBA hue, final float multiplier)
	{
		aMaterial.setHue(hue, multiplier);
	}

	@Override
	public void setInternalAlpha(final float alpha)
	{
		aMaterial.setAlpha(alpha);
	}
}