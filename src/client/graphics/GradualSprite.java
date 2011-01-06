package client.graphics;

import java.util.ArrayList;
import java.util.List;

import client.EverNode;
import client.graphics.geometry.Transform;

public class GradualSprite extends MultiSprite
{
	protected List<EverNode> aGradualSprites = new ArrayList<EverNode>();
	private float aGradualState = 0f;

	@Override
	public EverNode addSprite(final String image, final float x, final float y)
	{
		final EverNode spr = super.addSprite(image, x, y);
		aGradualSprites.add(spr);
		computeGradual();
		return spr;
	}

	protected void computeGradual()
	{
		final float grad = aGradualState * getNumberOfFrames();
		for (final Transform t : aSpriteTransforms.values())
		{
			t.setAlpha(0);
		}
		if (grad <= 1)
		{
			getAlphaTransform(0).setAlpha(grad);
			return;
		}
		if (grad >= getNumberOfFrames())
		{
			getAlphaTransform(getNumberOfFrames() - 1).setAlpha(1);
			return;
		}
		final int currentSprite = (int) grad;
		getAlphaTransform(currentSprite).setAlpha(1);
		getAlphaTransform(currentSprite + 1).setAlpha(grad - currentSprite);
	}

	private Transform getAlphaTransform(final int index)
	{
		return aSpriteTransforms.get(aGradualSprites.get(index));
	}

	public void setGradualState(final float gradualState)
	{
		aGradualState = Math.min(1, Math.max(0, gradualState));
		computeGradual();
	}
}
