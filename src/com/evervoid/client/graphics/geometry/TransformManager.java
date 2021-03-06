package com.evervoid.client.graphics.geometry;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.evervoid.client.EVFrameManager;
import com.evervoid.client.graphics.EverNode;
import com.evervoid.client.graphics.FrameUpdate;
import com.evervoid.client.interfaces.EVFrameObserver;

/**
 * The TransformManager handles all {@link AnimatedTransform}s, to make them tick in time. It has a special link to the
 * {@link EVFrameManager} such that it always receives frame events last.
 */
public class TransformManager implements EVFrameObserver
{
	/**
	 * Singleton reference
	 */
	private static TransformManager sInstance = null;

	/**
	 * @return The singleton instance of the TransformManager. Created if it doesn't exist
	 */
	private static TransformManager get()
	{
		if (sInstance == null) {
			sInstance = new TransformManager();
		}
		return sInstance;
	}

	/**
	 * Mark an {@link EverNode} as needing an update.
	 * 
	 * @param node
	 *            The {@link EverNode} that needs an update.
	 */
	public static void needUpdate(final EverNode node)
	{
		get().nodeNeedsUpdate(node);
	}

	/**
	 * Called by animation Transforms when an animation is started. Registers the specified animation Transform to receive frame
	 * update event
	 * 
	 * @param animation
	 *            The animated Transform to register
	 */
	public static void register(final AnimatedTransform animation)
	{
		get().add(animation);
	}

	/**
	 * Unregister an animated Transform from this EverNode. Called by animated Transforms when they are finished and no longer
	 * need to receive frame update events
	 * 
	 * @param animation
	 *            The animated Transform to unregister
	 */
	public static void unregister(final AnimatedTransform animation)
	{
		get().remove(animation);
	}

	/**
	 * Set of undergoing animations
	 */
	private final Set<AnimatedTransform> aAnimations = new HashSet<AnimatedTransform>();
	/**
	 * Queue of animations that finished during the last tick. Used for cleaning up the animation queue
	 */
	private final BlockingQueue<AnimatedTransform> aFinishedAnimations = new LinkedBlockingQueue<AnimatedTransform>();
	/**
	 * Queue of animations that should be added on next tick
	 */
	private final BlockingQueue<AnimatedTransform> aNewAnimations = new LinkedBlockingQueue<AnimatedTransform>();
	/**
	 * Queue of nodes that need to be updated independently on next frame
	 */
	private final BlockingQueue<EverNode> aNodes = new LinkedBlockingQueue<EverNode>();

	/**
	 * Private constructor, since this is a singleton
	 */
	private TransformManager()
	{
		EVFrameManager.setTransformManager(this);
	}

	/**
	 * Add an {@link AnimatedTransform} to the set of managed {@link AnimatedTransform}s
	 * 
	 * @param t
	 *            The {@link AnimatedTransform} to add
	 */
	private void add(final AnimatedTransform t)
	{
		aNewAnimations.add(t);
	}

	@Override
	public void frame(final FrameUpdate f)
	{
		final Set<EverNode> toRecompute = new HashSet<EverNode>();
		for (final AnimatedTransform t : aAnimations) {
			if (t.frame(f.aTpf)) {
				toRecompute.add(t.getNode());
			}
		}
		while (!aNodes.isEmpty()) // Take care of static Transforms
		{
			toRecompute.add(aNodes.poll());
		}
		for (final EverNode n : toRecompute) {
			n.computeTransforms();
		}
		while (!aFinishedAnimations.isEmpty()) // Clean up finished animations
		{
			aAnimations.remove(aFinishedAnimations.poll());
		}
		while (!aNewAnimations.isEmpty()) { // Clean up finished animations
			aAnimations.add(aNewAnimations.poll());
		}
	}

	/**
	 * Mark an {@link EverNode} as needing an update (non-static version)
	 * 
	 * @param node
	 *            The {@link EverNode} that needs an update.
	 */
	private void nodeNeedsUpdate(final EverNode node)
	{
		aNodes.add(node);
	}

	/**
	 * Remove an {@link AnimatedTransform} from the set of managed {@link AnimatedTransform}s
	 * 
	 * @param t
	 *            The {@link AnimatedTransform} to remote
	 */
	private void remove(final AnimatedTransform t)
	{
		aFinishedAnimations.add(t);
	}
}
