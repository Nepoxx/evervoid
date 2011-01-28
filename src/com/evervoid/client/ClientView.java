package com.evervoid.client;

import com.evervoid.client.EverVoidClient.NodeType;
import com.jme3.math.Vector2f;

public abstract class ClientView extends EverNode implements InputListener
{
	public NodeType getNodeType()
	{
		return NodeType.TWODIMENSION;
	}

	@Override
	public boolean onMouseClick(final Vector2f position, final float tpf)
	{
		return false;
	}

	@Override
	public boolean onMouseMove(final float tpf, final Vector2f position)
	{
		return false;
	}

	@Override
	public boolean onMouseRelease(final Vector2f position, final float tpf)
	{
		return false;
	}

	@Override
	public boolean onMouseWheelDown(final float delta, final float tpf, final Vector2f position)
	{
		return false;
	}

	@Override
	public boolean onMouseWheelUp(final float delta, final float tpf, final Vector2f position)
	{
		return false;
	}
}
