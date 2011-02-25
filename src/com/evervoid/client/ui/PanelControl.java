package com.evervoid.client.ui;

import com.jme3.math.ColorRGBA;

/**
 * A pretty panel with a title and proper margins
 */
public class PanelControl extends WrapperControl
{
	public static ColorRGBA sPanelTitleColor = new ColorRGBA(0.8f, 0.8f, 1f, 1f);
	private final StaticTextControl aPanelTitle;
	private final UIControl aPanelTitleBox;

	public PanelControl(final String title)
	{
		super(new UIControl(BoxDirection.VERTICAL));
		final BoxControl box = new BoxControl(BoxDirection.VERTICAL);
		aPanelTitleBox = new UIControl(BoxDirection.HORIZONTAL);
		aPanelTitle = new StaticTextControl(title, sPanelTitleColor, "redensek", 28);
		aPanelTitle.setKeepBoundsOnChange(false);
		aPanelTitleBox.addUI(aPanelTitle);
		aPanelTitleBox.addFlexSpacer(1);
		box.addUI(aPanelTitleBox);
		box.addUI(new SpacerControl(1, 16));
		// Add line here
		box.addUI(aContained, 1);
		final MarginSpacer margins = new MarginSpacer(8, 8, 8, 8, box);
		addChildUI(margins, 1);
	}

	protected UIControl getTitleBox()
	{
		return aPanelTitleBox;
	}

	public void setTitle(final String title)
	{
		aPanelTitle.setText(title);
	}
}
