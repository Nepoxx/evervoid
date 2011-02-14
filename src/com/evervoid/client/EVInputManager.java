package com.evervoid.client;

import com.jme3.math.Vector2f;

public class EVInputManager
{
	public void onAction(final String name, final boolean isPressed, final float tpf, final Vector2f position)
	{
		if (name.equals("Mouse click")) {
			// Forward mouse clicks to game view
			if (isPressed) {
				EVViewManager.onMouseClick(position, tpf);
			}
			else {
				EVViewManager.onMouseRelease(position, tpf);
			}
		}
		final Key key = Key.fromMapping(name);
		if (key != null) {
			if (isPressed) {
				EVViewManager.onKeyPress(key, tpf);
			}
			else {
				EVViewManager.onKeyRelease(key, tpf);
			}
		}
	}

	public void onAnalog(final String name, final float delta, final float tpf, final Vector2f position)
	{
		if (name.equals("Mouse move")) {
			// Forward mouse movement to game view
			EVViewManager.onMouseMove(position, tpf);
		}
		else if (name.equals("Mouse wheel up")) {
			EVViewManager.onMouseWheelUp(delta, tpf, position);
		}
		else if (name.equals("Mouse wheel down")) {
			EVViewManager.onMouseWheelDown(delta, tpf, position);
		}
	}
}