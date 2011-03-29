package com.evervoid.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.evervoid.client.views.Bounds;
import com.evervoid.state.geometry.Dimension;
import com.jme3.math.Vector2f;

// TODO: Add smooth scrolling, with AnimatedTranslations and AniamtedAlpha for the top/bottom controls to fade in/out.
public class ScrollingControl extends UIControl
{
	private static final float sScrollMultiplier = 12;
	private boolean aAllFitsIn = true;
	private final List<UIControl> aDisplayedControls = new ArrayList<UIControl>();
	private final float aMaxHeight;
	private float aOffset = 0;
	private final List<UIControl> aScrollingChildren = new ArrayList<UIControl>();
	private float aTotalHeight = 0;

	public ScrollingControl(final Dimension desiredSize)
	{
		super(BoxDirection.VERTICAL);
		setDesiredDimension(desiredSize);
		aMaxHeight = desiredSize.getHeightFloat();
	}

	public ScrollingControl(final float minWidth, final float minHeight)
	{
		this(new Dimension(minWidth, minHeight));
	}

	@Override
	void addChildUI(final UIControl control, int spring)
	{
		if (control == null) {
			return;
		}
		if (spring != 0) {
			System.err.println("CAUTION: Trying to add a non-zero-spring control to a ScrollingArea. Overriding to 0 spring!");
			spring = 0;
		}
		if (aScrollingChildren.contains(control)) {
			System.err.println("Warning: Trying to add the same UIControl twice.");
		}
		aScrollingChildren.add(control);
		aTotalHeight += control.getMinimumHeight();
		recomputeAllBounds();
	}

	@Override
	public List<UIControl> getChildrenUIs()
	{
		return aDisplayedControls;
	}

	@Override
	protected boolean inBounds(final Vector2f point)
	{
		return point != null
				&& aComputedBounds != null
				&& (aComputedBounds.x <= point.x && aComputedBounds.y <= point.y
						&& aComputedBounds.x + aComputedBounds.width > point.x && aComputedBounds.y + aComputedBounds.height > point.y);
	}

	@Override
	public boolean onMouseWheelDown(final float delta, final Vector2f position)
	{
		if (aAllFitsIn || !inBounds(position)) {
			return false;
		}
		aOffset = Math.min(aTotalHeight - aComputedBounds.height, aOffset + delta * sScrollMultiplier);
		setBounds(getComputedBounds());
		return true;
	}

	@Override
	public boolean onMouseWheelUp(final float delta, final Vector2f position)
	{
		if (aAllFitsIn || !inBounds(position)) {
			return false;
		}
		aOffset = Math.max(0, aOffset - delta * sScrollMultiplier);
		setBounds(getComputedBounds());
		return true;
	}

	@Override
	public void setBounds(final Bounds bounds)
	{
		// Never change aMaxHeight
		aComputedBounds = new Bounds(bounds.x, bounds.y, bounds.width, aMaxHeight);
		delAllNodes();
		aDisplayedControls.clear();
		if (aScrollingChildren.isEmpty()) {
			aAllFitsIn = true;
			return; // Well, that's all folks
		}
		float heightSoFar = 0;
		int firstChild = 0;
		while (heightSoFar < aOffset && firstChild < aScrollingChildren.size()) {
			heightSoFar += aScrollingChildren.get(firstChild).getMinimumHeight();
			firstChild++;
		}
		float yOffset = heightSoFar - aOffset;
		int lastChild = firstChild;
		while (heightSoFar < aOffset + aMaxHeight && lastChild < aScrollingChildren.size()) {
			heightSoFar += aScrollingChildren.get(lastChild).getMinimumHeight();
			lastChild++;
		}
		lastChild = Math.max(firstChild + 1, lastChild);
		if (lastChild == aScrollingChildren.size()) {
			aAllFitsIn = true;
			aOffset = 0;
			yOffset = 0;
		}
		else {
			aAllFitsIn = false;
		}
		for (int i = firstChild; i < lastChild; i++) {
			final UIControl child = aScrollingChildren.get(i);
			aDisplayedControls.add(child);
			addNode(child);
			final int minHeight = child.getMinimumHeight();
			yOffset += minHeight;
			child.setBounds(new Bounds(0, bounds.height - yOffset, bounds.width, minHeight));
		}
	}
}