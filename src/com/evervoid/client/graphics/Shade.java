package com.evervoid.client.graphics;

import com.evervoid.client.EverNode;
import com.evervoid.client.graphics.materials.AlphaShaded;
import com.evervoid.gamedata.OffsetSprite;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

public class Shade extends EverNode implements Sizeable
{
	private final AlphaShaded aMaterial;

	public Shade(final OffsetSprite sprite)
	{
		super();
		aMaterial = new AlphaShaded(sprite.sprite);
		final Quad q = new Quad(aMaterial.getWidth() * Sprite.sSpriteScale, aMaterial.getHeight() * Sprite.sSpriteScale);
		final Geometry g = new Geometry("Shade of " + sprite.sprite + " @ " + hashCode(), q);
		g.setMaterial(aMaterial);
		attachChild(g);
		getNewTransform().translate(-aMaterial.getWidth() * Sprite.sSpriteScale / 2,
				-aMaterial.getHeight() * Sprite.sSpriteScale / 2).move(sprite.x, sprite.y);
	}

	public Shade(final String image)
	{
		this(new OffsetSprite(image));
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

	public Shade setGradientPortion(final float gradientPortion)
	{
		aMaterial.setGradientPortion(gradientPortion);
		return this;
	}

	public Shade setShadeAngle(final float shadeAngle)
	{
		aMaterial.setShadeAngle(shadeAngle);
		return this;
	}

	public Shade setShadePortion(final float shadePortion)
	{
		aMaterial.setShadePortion(shadePortion);
		return this;
	}
}
