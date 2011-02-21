package com.evervoid.client.ui;

/**
 * Wraps a UIControl to make it vertically centered
 */
public class VerticalCenteredControl extends WrapperControl
{
	public VerticalCenteredControl(final Resizeable contained)
	{
		super(contained, BoxDirection.VERTICAL);
		addUI(new UIControl(), 1);
		addUI(contained, 0);
		addUI(new UIControl(), 1);
	}
}
