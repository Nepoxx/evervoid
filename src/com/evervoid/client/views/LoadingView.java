package com.evervoid.client.views;

import com.evervoid.client.ui.ImageControl;
import com.evervoid.state.data.SpriteData;

/**
 * The LoadingView is the view shown during loading screens.
 */
public class LoadingView extends EverView
{
	/**
	 * Constructor; nothing fancy here.
	 */
	public LoadingView()
	{
		final ImageControl loading = new ImageControl(new SpriteData("ui/loading.png", 0, 0, 1));
		addNode(loading);
	}
}
