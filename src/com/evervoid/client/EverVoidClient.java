package com.evervoid.client;

import static com.evervoid.utils.ResourceUtils.getResourceDir;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.evervoid.client.graphics.EverFileLocator;
import com.evervoid.client.graphics.EverNode;
import com.evervoid.client.graphics.FrameUpdate;
import com.evervoid.client.graphics.GraphicManager;
import com.evervoid.client.sound.EVSoundEngine;
import com.evervoid.state.geometry.Dimension;
import com.evervoid.utils.LoggerUtils;
import com.jme3.asset.AssetManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.BloomFilter.GlowMode;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

/**
 * everVoid game client providing the user with a user interface to play the game.
 */
public class EverVoidClient extends EVjMonkeyApp implements ActionListener, AnalogListener
{
	/**
	 * Possible types of EverNodes. This is used because 3D and 2D nodes need to be attached to different nodes in order to be
	 * displayed to the screen. Doing it this way allows methods to use NodeType.getNode().attachChild() and not check for the
	 * type of the node to determine the correct root to attach to.
	 */
	public enum NodeType
	{
		/**
		 * A three dimensional node.
		 */
		THREEDIMENSION,
		/**
		 * A two dimensional node.
		 */
		TWODIMENSION;
		/**
		 * Finds the correct parent not to attach to in the client based on the type of Node being attached.
		 * 
		 * @param client
		 *            The Client containing the parent nodes.
		 * @return The Node to which one should be attached to in order to display correctly.
		 */
		public Node getNode(final EverVoidClient client)
		{
			switch (this) {
				case TWODIMENSION:
					return client.getGuiNode();
				case THREEDIMENSION:
					return client.getRootNode();
			}
			return null;
		}
	}

	/**
	 * A list of the sizes available for the everVoid icon.
	 */
	private static final int[] sAvailableIconSizes = { 512, 256, 128, 64, 32, 16 };
	/**
	 * Instance of the everVoidClient
	 */
	private static EverVoidClient sClient;
	/**
	 * The cursor's current position on the screen.
	 */
	public static Vector2f sCursorPosition = new Vector2f();
	/**
	 * The input manager used to relay all input
	 */
	private static final EVInputManager sInputManager = EVInputManager.getInstance();
	/**
	 * The dimensions of the screen.
	 */
	private static Dimension sScreenDimension = new Dimension(0, 0);
	/**
	 * The current vesion of everVoid.
	 */
	public static final double VERSION = 2.0;

	/**
	 * Add a root node to the main window
	 * 
	 * @param type
	 *            Type of node to add
	 * @param node
	 *            The node to add
	 */
	public static void addRootNode(final NodeType type, final EverNode node)
	{
		type.getNode(sClient).attachChild(node);
	}

	/**
	 * Delete a root node from the main window
	 * 
	 * @param node
	 *            The node to delete (type doesn't matter)
	 */
	public static void delRootNode(final EverNode node)
	{
		// Try detaching from both; no side-effects
		sClient.getGuiNode().detachChild(node);
		sClient.getRootNode().detachChild(node);
	}

	/**
	 * @return The assetManager for the client.
	 */
	public static AssetManager getAssetManger()
	{
		return sClient.assetManager;
	}

	/**
	 * Creates a 3D Ray covered the area directly under point on the screen designated by the vector. This should be used when
	 * in a 3D view to convert from 2D to 3D.
	 * 
	 * @param vector
	 *            The screen location under which the ray should be formed.
	 * @return The Ray projecting for the click location going straight in to the camera.
	 */
	public static Ray getRayFromVector(final Vector2f vector)
	{
		// calculates the point you clicked and it's projection at the z = 10 level
		final Vector3f clickPoint = sClient.cam.getWorldCoordinates(vector, 10f);
		final Vector3f projectedPoint = sClient.cam.getWorldCoordinates(vector, -10f);
		// creates a ray from the original point and the projection
		return new Ray(clickPoint, projectedPoint.subtractLocal(clickPoint).normalizeLocal());
	}

	/**
	 * @return The user's client settings
	 */
	public static EVClientSettings getSettings()
	{
		return sClient.aClientSettings;
	}

	/**
	 * @return The version number times 100
	 */
	public static int getVersionAsInt()
	{
		return (int) (VERSION * 100);
	}

	/**
	 * Creates a Dimension containing the window width and height in x and y respectively. More specifically represents the
	 * number of pixels of the window in that particular dimension.
	 * 
	 * @return The Dimensions of the window at this instant.
	 */
	public static Dimension getWindowDimension()
	{
		return sScreenDimension;
	}

	/**
	 * Called when the EverJMEApp fails to load its Display. Tries to change settings and launch again.
	 * 
	 * @param t
	 *            The reason the Display failed to load.
	 */
	public static void handleFailedDisplay(final Throwable t)
	{
		LoggerUtils.info("Failed to create everVoid display, checking for cause");
		if (t.getMessage().equals("Unable to find fullscreen display mode matching settings")) {
			LoggerUtils.info("Fullscreen resolution not supported, trying in windowed mode");
			EverVoidClient.startFullScreen(false);
		}
		else if (t.getMessage().equals("No support for WGL_ARB_multisample")) {
			LoggerUtils.info("Anti-Aliasing not supported by graphics card, trying again without samples");
			EverVoidClient.startLowSettings();
		}
		else {
			LoggerUtils.severe("Could not diagnose the error, bailing");
			quit();
		}
	}

	/**
	 * everVoid Client program
	 * 
	 * @param args
	 *            Arguments passed to the program.
	 */
	public static void main(final String[] args)
	{
		Logger.getLogger("").setLevel(Level.SEVERE);
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true"); // Change to true for undecorated
		sClient = new EverVoidClient();
		sClient.setShowSettings(false);
		final AppSettings options = new AppSettings(true);
		final Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		options.setResolution(screenSize.width, screenSize.height);
		options.setFullscreen(false);
		options.setSamples(4);
		options.setVSync(true);
		options.setTitle("everVoid");
		try {
			final BufferedImage[] icons = new BufferedImage[sAvailableIconSizes.length];
			int index = 0;
			for (final int size : sAvailableIconSizes) {
				icons[index] = ImageIO.read(new File("res/gfx/icons/void/icon" + size + ".png"));
				index++;
			}
			options.setIcons(icons);
		}
		catch (final IOException e) {
			// Too bad, no icon for you buddy
		}
		sClient.setSettings(options);
		sClient.start();
	}

	/**
	 * Quits all everVoid processes.
	 */
	public static void quit()
	{
		sClient.requestClose(false);
	}

	/**
	 * Creates a display with default settings and in fullscreen or windowed modes depending on the boolean.
	 * 
	 * @param pFullscreen
	 *            Whether to start in fullscreen mode.
	 */
	private static void startFullScreen(final boolean pFullscreen)
	{
		final AppSettings options = new AppSettings(true);
		final Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		options.setResolution(screenSize.width, screenSize.height);
		options.setFullscreen(pFullscreen);
		options.setTitle("everVoid");
		// options.setAudioRenderer(null);
		options.setSamples(4);
		options.setVSync(true);
		// all done with settings
		sClient.setSettings(options);
		sClient.start();
	}

	/**
	 * Starts a JME display but without anti-aliasing and vsync
	 */
	private static void startLowSettings()
	{
		final AppSettings options = new AppSettings(true);
		final Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		options.setTitle("everVoid");
		// options.setAudioRenderer(null);
		options.setResolution(screenSize.width, screenSize.height);
		options.setFullscreen(false);
		// no anti-aliasing
		options.setSamples(0);
		options.setVSync(false);
		// all done with settings
		sClient.setSettings(options);
		sClient.start();
	}

	/**
	 * The user's settings for the client.
	 */
	private final EVClientSettings aClientSettings;
	/**
	 * Quick reference to the the view manager. Note that this is not a static variable and instantiated in SimpleInitApp()
	 * because it needs a running instance of the EverVoidClietn in order to attach itself as a frame listener.
	 */
	private EVViewManager aViewManager;

	/**
	 * Private constructor for the everVoidClient
	 */
	private EverVoidClient()
	{
		sClient = this;
		aClientSettings = new EVClientSettings();
	}

	/**
	 * Creates mappings for all events that everVoid listens for.
	 */
	private void createAllMappings()
	{
		inputManager.addMapping("Mouse move", new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(
				MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_Y, false), new MouseAxisTrigger(
				MouseInput.AXIS_Y, true));
		inputManager.addListener(this, "Mouse move");
		createMapping("Mouse wheel up", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		createMapping("Mouse wheel down", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		createMapping("Mouse left", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		createMapping("Mouse right", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		KeyboardKey.setMappings(inputManager, this);
	}

	/**
	 * Creates a new event that everVoid listens for.
	 * 
	 * @param pMappingName
	 *            The name everVoid will associate with the event.
	 * @param pTrigger
	 *            The trigger associated with the event.
	 */
	private void createMapping(final String pMappingName, final Trigger pTrigger)
	{
		inputManager.addListener(this, pMappingName);
		inputManager.addMapping(pMappingName, pTrigger);
	}

	@Override
	public void onAction(final String name, final boolean isPressed, final float tpf)
	{
		sInputManager.onAction(name, isPressed, tpf, sCursorPosition);
	}

	@Override
	public void onAnalog(final String name, final float delta, final float tpf)
	{
		sCursorPosition = inputManager.getCursorPosition();
		sInputManager.onAnalog(name, delta, tpf, sCursorPosition);
	}

	@Override
	public void requestClose(final boolean esc)
	{
		super.requestClose(esc);
		EVClientEngine.stopLocalServer(); // Kill it, if any
		EVSoundEngine.cleanup(); // Terminate music thread.
		stop();
	}

	@Override
	public void simpleInitApp()
	{
		assetManager.registerLocator(new File(getResourceDir()).getAbsolutePath(), EverFileLocator.class);
		GraphicManager.setAssetManager(assetManager);
		sScreenDimension = new Dimension(cam.getWidth(), cam.getHeight());
		final FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		final BloomFilter bloom = new BloomFilter(GlowMode.Objects);
		bloom.setDownSamplingFactor(4);
		bloom.setBloomIntensity(1.2f);
		fpp.addFilter(bloom);
		getRootViewPort().addProcessor(fpp);
		createAllMappings();
		aViewManager = EVViewManager.getInstance();
		// start the client engine
		EVClientEngine.getInstance();
		// register the gui as a listener
		EVClientEngine.registerGlobalListener(aViewManager);
		EVSoundEngine.init();
	}

	@Override
	public void simpleUpdate(final float tpf)
	{
		EVFrameManager.tick(new FrameUpdate(tpf));
	}
}
