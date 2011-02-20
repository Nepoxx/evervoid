package com.evervoid.client.views.solar;

import com.evervoid.client.graphics.EverNode;
import com.evervoid.client.graphics.GridNode;
import com.evervoid.client.graphics.MultiSprite;
import com.evervoid.client.graphics.Sprite;
import com.evervoid.client.graphics.geometry.AnimatedAlpha;
import com.evervoid.client.graphics.geometry.AnimatedFloatingTranslation;
import com.evervoid.client.graphics.geometry.AnimatedRotation;
import com.evervoid.state.data.SpriteData;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.prop.Prop;

public abstract class UIProp extends GridNode
{
	protected static enum PropState
	{
		INACTIVE, MOVING, SELECTABLE, SELECTED;
	}

	protected AnimatedRotation aFaceTowards = getNewRotationAnimation();
	protected GridLocation aFacing = null;
	protected AnimatedFloatingTranslation aFloatingAnimation;
	protected Prop aProp;
	protected final AnimatedAlpha aPropAlpha = getNewAlphaAnimation();
	private PropState aPropState = PropState.SELECTABLE;
	protected SolarGrid aSolarSystemGrid;
	protected MultiSprite aSprite = new MultiSprite();
	protected boolean aSpriteReady = false;

	public UIProp(final SolarGrid grid, final GridLocation location, final Prop prop)
	{
		super(grid, location);
		addNode(aSprite);
		aSolarSystemGrid = grid;
		aProp = prop;
		addToGrid();
		aPropAlpha.setDuration(0.5f);
	}

	protected EverNode addSprite(final EverNode sprite)
	{
		return aSprite.addSprite(sprite);
	}

	protected EverNode addSprite(final EverNode sprite, final float x, final float y)
	{
		return aSprite.addSprite(sprite, x, y);
	}

	protected EverNode addSprite(final SpriteData info)
	{
		return aSprite.addSprite(info);
	}

	protected EverNode addSprite(final String image)
	{
		return aSprite.addSprite(new Sprite(image));
	}

	protected EverNode addSprite(final String image, final float x, final float y)
	{
		return aSprite.addSprite(new Sprite(image), x, y);
	}

	/**
	 * Called by subclasses when they have obtained sufficient data to be able to build their sprite.
	 */
	protected void buildProp()
	{
		buildSprite();
		aSpriteReady = true;
	}

	/**
	 * Overridden by subclasses; called when the sprite should be built.
	 */
	protected abstract void buildSprite();

	/**
	 * Called by subclasses when they desire to have a floating animation. Automatically starts the animation
	 * 
	 * @param duration
	 *            The duration of the floating animation
	 * @param offset
	 *            The maximum distance the animation may go
	 */
	protected void enableFloatingAnimation(final float duration, final float offset)
	{
		aFloatingAnimation = getNewFloatingTranslationAnimation();
		aFloatingAnimation.setToleratedOffset(offset).setDuration(duration).start();
	}

	public void faceTowards(final float angle)
	{
		aFacing = null;
		aFaceTowards.setTargetPitch(angle).start();
	}

	public void faceTowards(final GridLocation target)
	{
		if (target != null && !target.equals(aFacing)) {
			aFaceTowards.setTargetPoint2D(aGrid.getCellCenter(target).subtract(getCellCenter())).start();
			aFacing = target;
		}
	}

	public float getFacingDirection()
	{
		return aFaceTowards.getRotationPitch();
	}

	Prop getProp()
	{
		return aProp;
	}

	protected PropState getPropState()
	{
		return aPropState;
	}

	public SolarGrid getSolarSystemGrid()
	{
		return aSolarSystemGrid;
	}

	boolean isMovable()
	{
		return false; // Not movable by default
	}

	boolean isSelectable()
	{
		return aPropState.equals(PropState.SELECTABLE);
	}

	public void setState(final PropState propState)
	{
		aPropState = propState;
		if (aPropState.equals(PropState.INACTIVE)) {
			aPropAlpha.setTargetAlpha(0.5f).start();
		}
		else {
			aPropAlpha.setTargetAlpha(1).start();
		}
	}
}
