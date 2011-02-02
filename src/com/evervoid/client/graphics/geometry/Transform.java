package com.evervoid.client.graphics.geometry;

import com.evervoid.client.EverNode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class Transform
{
	protected float aAlpha = 1f;
	private Vector3f aMaximumScale = null;
	private Vector3f aMaximumVector = null;
	private Vector3f aMinimumScale = null;
	private Vector3f aMinimumVector = null;
	protected final EverNode aNode;
	private boolean aNotifyOnChange = true;
	protected float aOldAlpha = 1f;
	protected float aOldRotation = 0f;
	protected Vector3f aOldScale = new Vector3f(1f, 1f, 1f);
	protected final Vector3f aOldVector = new Vector3f(0, 0, 0);
	protected float aRotation = 0f;
	protected Vector3f aScale = new Vector3f(1f, 1f, 1f);
	protected final Vector3f aVector = new Vector3f(0, 0, 0);

	/**
	 * Warning: Do NOT call this directly; call parent.getNewTransform() instead!
	 * 
	 * @param parent
	 *            The EverNode that this transformation will affect
	 */
	public Transform(final EverNode parent)
	{
		aNode = parent;
	}

	/**
	 * Force synchronous recomputation of this Transform
	 * 
	 * @return This
	 */
	public Transform commit()
	{
		aNode.computeTransforms();
		return this;
	}

	public Transform copy(final Transform t)
	{
		final boolean oldNotify = aNotifyOnChange;
		setNotifyOnChange(false);
		translate(t.getTranslation().clone());
		rotateTo(t.getRotation());
		setScale(t.getScaleAverage());
		setAlpha(t.getAlpha());
		setNotifyOnChange(oldNotify);
		updated();
		return this;
	}

	public Transform faceTowards(final Vector2f point)
	{
		final Float angle = MathUtils.getAngleTowards(point);
		if (angle != null) {
			rotateTo(angle);
		}
		return this;
	}

	public float getAlpha()
	{
		return aAlpha;
	}

	/**
	 * Returns the EverNode that this Transform is attached to
	 * 
	 * @return The attached EverNoe
	 */
	public EverNode getNode()
	{
		return aNode;
	}

	public float getRotation()
	{
		return aRotation;
	}

	public Vector3f getScale()
	{
		return aScale;
	}

	public float getScaleAverage()
	{
		return (aScale.x + aScale.y + aScale.z) / 3f;
	}

	public Vector3f getTranslation()
	{
		return aVector;
	}

	public Vector2f getTranslation2f()
	{
		return new Vector2f(aVector.x, aVector.y);
	}

	public Transform move(final double x, final double y)
	{
		return move((float) x, (float) y);
	}

	public Transform move(final double x, final double y, final double z)
	{
		return move((float) x, (float) y, (float) z);
	}

	public Transform move(final float x, final float y)
	{
		return move(new Vector3f(x, y, 0));
	}

	public Transform move(final float x, final float y, final float z)
	{
		return move(new Vector3f(x, y, z));
	}

	public Transform move(final Vector2f offset)
	{
		return move(new Vector3f(offset.x, offset.y, 0));
	}

	public Transform move(final Vector3f offset)
	{
		aVector.addLocal(offset);
		updated();
		return this;
	}

	public Transform multScale(final double scale)
	{
		return multScale((float) scale);
	}

	public Transform multScale(final float scale)
	{
		return setScale(scale * getScaleAverage());
	}

	public Transform rotateBy(final double angle)
	{
		return rotateBy((float) angle);
	}

	public Transform rotateBy(final float angle)
	{
		return rotateTo(aRotation + angle);
	}

	public Transform rotateTo(final double angle)
	{
		return rotateTo((float) angle);
	}

	public Transform rotateTo(final float angle)
	{
		aRotation = angle % FastMath.TWO_PI;
		updated();
		return this;
	}

	public Transform setAlpha(final double alpha)
	{
		return setAlpha((float) alpha);
	}

	public Transform setAlpha(final float alpha)
	{
		aAlpha = MathUtils.clampFloat(0, alpha, 1);
		updated();
		return this;
	}

	public Transform setMaximumScale(final float max)
	{
		return setMaximumScale(new Vector3f(max, max, max));
	}

	public Transform setMaximumScale(final Vector3f max)
	{
		aMaximumScale = max;
		updated();
		return this;
	}

	public Transform setMaximumTranslation(final float x, final float y)
	{
		return setMaximumTranslation(new Vector3f(x, y, 0));
	}

	public Transform setMaximumTranslation(final float x, final float y, final float z)
	{
		return setMaximumTranslation(new Vector3f(x, y, z));
	}

	public Transform setMaximumTranslation(final Vector2f max)
	{
		return setMaximumTranslation(new Vector3f(max.x, max.y, 0));
	}

	public Transform setMaximumTranslation(final Vector3f max)
	{
		aMaximumVector = max;
		updated();
		return this;
	}

	public Transform setMinimumScale(final float min)
	{
		return setMinimumScale(new Vector3f(min, min, min));
	}

	public Transform setMinimumScale(final Vector3f min)
	{
		aMinimumScale = min;
		updated();
		return this;
	}

	public Transform setMinimumTranslation(final float x, final float y)
	{
		return setMinimumTranslation(new Vector3f(x, y, 0));
	}

	public Transform setMinimumTranslation(final float x, final float y, final float z)
	{
		return setMinimumTranslation(new Vector3f(x, y, z));
	}

	public Transform setMinimumTranslation(final Vector2f max)
	{
		return setMinimumTranslation(new Vector3f(max.x, max.y, 0));
	}

	public Transform setMinimumTranslation(final Vector3f min)
	{
		aMinimumVector = min;
		updated();
		return this;
	}

	protected Transform setNotifyOnChange(final boolean notify)
	{
		aNotifyOnChange = notify;
		return this;
	}

	public Transform setScale(final double scale)
	{
		return setScale((float) scale);
	}

	public Transform setScale(final float scale)
	{
		return setScale(scale, scale, scale);
	}

	public Transform setScale(final float xScale, final float yScale)
	{
		return setScale(xScale, yScale, aScale.z);
	}

	public Transform setScale(final float xScale, final float yScale, final float zScale)
	{
		aScale.set(Math.max(0, xScale), Math.max(0, yScale), Math.max(0, zScale));
		updated();
		return this;
	}

	public Transform setScale(final Vector3f scale)
	{
		return setScale(scale.x, scale.y, scale.z);
	}

	@Override
	public String toString()
	{
		return "Transform(" + aVector + "; " + aRotation + "; " + aScale + "; " + aAlpha + ")";
	}

	public Transform translate(final double x, final double y)
	{
		return translate((float) x, (float) y);
	}

	public Transform translate(final double x, final double y, final double z)
	{
		return translate((float) x, (float) y, (float) z);
	}

	public Transform translate(final float x, final float y)
	{
		return translate(new Vector3f(x, y, 0));
	}

	public Transform translate(final float x, final float y, final float z)
	{
		return translate(new Vector3f(x, y, z));
	}

	public Transform translate(final Vector2f offset)
	{
		return translate(new Vector3f(offset.x, offset.y, 0));
	}

	public Transform translate(final Vector3f offset)
	{
		aVector.set(offset);
		updated();
		return this;
	}

	protected void updated()
	{
		if (!aNotifyOnChange) {
			return;
		}
		if (aMinimumVector != null) {
			MathUtils.clampVectorUpLocal(aMinimumVector, aVector);
		}
		if (aMaximumVector != null) {
			MathUtils.clampVectorDownLocal(aMaximumVector, aVector);
		}
		if (aMinimumScale != null) {
			MathUtils.clampVectorUpLocal(aMinimumScale, aScale);
		}
		if (aMaximumScale != null) {
			MathUtils.clampVectorDownLocal(aMaximumScale, aScale);
		}
		if (!aVector.equals(aOldVector) || !MathUtils.near(aOldRotation, aRotation) || !MathUtils.near(aAlpha, aOldAlpha)
				|| !aScale.equals(aOldScale)) {
			TransformManager.needUpdate(aNode);
			aOldVector.set(aVector);
			aOldRotation = aRotation;
			aOldScale.set(aScale);
			aOldAlpha = aAlpha;
		}
	}
}