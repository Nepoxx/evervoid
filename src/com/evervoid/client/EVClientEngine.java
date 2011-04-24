package com.evervoid.client;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.evervoid.client.EVViewManager.ViewType;
import com.evervoid.client.interfaces.EVGameMessageListener;
import com.evervoid.client.interfaces.EVGlobalMessageListener;
import com.evervoid.client.interfaces.EVLobbyMessageListener;
import com.evervoid.client.views.game.GameView;
import com.evervoid.client.views.lobby.LobbyView;
import com.evervoid.json.Json;
import com.evervoid.network.ChatMessage;
import com.evervoid.network.EverMessage;
import com.evervoid.network.EverMessageHandler;
import com.evervoid.network.EverMessageListener;
import com.evervoid.network.GameStateMessage;
import com.evervoid.network.HandshakeMessage;
import com.evervoid.network.JoinErrorMessage;
import com.evervoid.network.LoadGameRequest;
import com.evervoid.network.PingMessage;
import com.evervoid.network.PlayerDefeatedMessage;
import com.evervoid.network.PlayerVictoryMessage;
import com.evervoid.network.ReadyMessage;
import com.evervoid.network.RequestGameState;
import com.evervoid.network.SaveGameStateReply;
import com.evervoid.network.ServerChatMessage;
import com.evervoid.network.ServerQuit;
import com.evervoid.network.StartGameMessage;
import com.evervoid.network.StartingGameMessage;
import com.evervoid.network.TurnMessage;
import com.evervoid.network.lobby.LobbyPlayer;
import com.evervoid.network.lobby.LobbyPlayerUpdate;
import com.evervoid.network.lobby.LobbyState;
import com.evervoid.network.lobby.LobbyStateMessage;
import com.evervoid.server.EVServerMessageObserver;
import com.evervoid.server.EverMessageSendingException;
import com.evervoid.server.EverVoidServer;
import com.evervoid.state.Color;
import com.evervoid.state.EVGameState;
import com.evervoid.state.action.Turn;
import com.evervoid.state.data.BadJsonInitialization;
import com.evervoid.utils.LoggerUtils;
import com.jme3.network.connection.Client;

public class EVClientEngine implements EverMessageListener
{
	private static EVClientEngine sInstance;

	public static void connect(final String pServerIP)
	{
		getInstance().doConnect(pServerIP);
	}

	/**
	 * Disconnects the EVClientEngine
	 */
	public static void disconnect()
	{
		if (sInstance != null) {
			sInstance.doDisconnect();
		}
		sInstance = new EVClientEngine();
		registerGlobalListener(EVViewManager.getInstance());
	}

	public static EVClientEngine getInstance()
	{
		if (sInstance == null) {
			sInstance = new EVClientEngine();
		}
		return sInstance;
	}

	public static void registerGameListener(final EVGameMessageListener listener)
	{
		sInstance.aGameObservers.add(listener);
	}

	public static void registerGlobalListener(final EVGlobalMessageListener listener)
	{
		sInstance.aGlobalObservers.add(listener);
	}

	public static void registerLobbyListener(final EVLobbyMessageListener listener)
	{
		sInstance.aLobbyObservers.add(listener);
	}

	public static void requestGameState(final EVGameState reference)
	{
		try {
			sInstance.aMessageHandler.send(new RequestGameState(reference));
		}
		catch (final EverMessageSendingException e) {
			LoggerUtils.severe("Could not send RequestGameState message.");
		}
	}

	public static void sendChatMessage(final String message)
	{
		try {
			sInstance.aMessageHandler.send(new ChatMessage(message));
		}
		catch (final EverMessageSendingException e) {
			// Not really critical
		}
	}

	public static void sendLoadGame(final File saveFile, final Runnable onFailure)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				EVGameState state;
				try {
					state = new EVGameState(Json.fromFile(saveFile));
					sInstance.aMessageHandler.send(new LoadGameRequest(state));
				}
				catch (final Exception e) {
					if (onFailure != null) {
						EVViewManager.schedule(onFailure);
					}
				}
			}
		}.start();
	}

	public static void sendLobbyPlayer(final LobbyPlayer player)
	{
		try {
			sInstance.aMessageHandler.send(new LobbyPlayerUpdate(player));
		}
		catch (final EverMessageSendingException e) {
			LoggerUtils.severe("Could not send LobbyPlayer message.");
		}
	}

	public static void sendReadyMessage()
	{
		try {
			sInstance.aMessageHandler.send(new ReadyMessage());
		}
		catch (final EverMessageSendingException e) {
			// how is that even possible?
		}
	}

	public static void sendStartGame()
	{
		try {
			sInstance.aMessageHandler.send(new StartGameMessage());
		}
		catch (final EverMessageSendingException e) {
			LoggerUtils.severe("Could not send startgame message.");
		}
	}

	public static void sendTurn(final Turn turn)
	{
		try {
			sInstance.aMessageHandler.send(new TurnMessage(turn));
		}
		catch (final EverMessageSendingException e) {
			// TODO: Show this on the UI somehow
			LoggerUtils.severe("Could not send turn: " + turn);
			e.printStackTrace();
		}
	}

	/**
	 * Launches a server locally.
	 */
	public static void startLocalServer()
	{
		try {
			EverVoidServer.ensureStarted();
			LoggerUtils.info("Local server started.");
		}
		catch (final Exception e) {
			LoggerUtils.severe("Couldn't launch local server.");
			e.printStackTrace();
		}
		// Sleep a bit; server takes a while to bind itself
		try {
			Thread.sleep(500);
		}
		catch (final InterruptedException e) {
			// Like this is ever going to happen
		}
	}

	public static void stopLocalServer()
	{
		EverVoidServer.stop();
	}

	private Client aClient;
	private boolean aConnected = false;
	private final Set<EVGameMessageListener> aGameObservers = new HashSet<EVGameMessageListener>();
	private final Set<EVGlobalMessageListener> aGlobalObservers = new HashSet<EVGlobalMessageListener>();
	private boolean aInLobby = false;
	private final Set<EVLobbyMessageListener> aLobbyObservers = new HashSet<EVLobbyMessageListener>();
	private EverMessageHandler aMessageHandler;
	private String aServerIP;

	public void deregisterObserver(final EVServerMessageObserver observer)
	{
		aGlobalObservers.remove(observer);
	}

	private void doConnect(final String pServerIP)
	{
		LoggerUtils.info("Client connecting to " + pServerIP + " on ports " + EverVoidServer.sGamePortTCP + "; "
				+ EverVoidServer.sGamePortUDP);
		aServerIP = new String(pServerIP);
		try {
			aClient = new Client(aServerIP, EverVoidServer.sGamePortTCP, EverVoidServer.sGamePortUDP);
		}
		catch (final IOException e) {
			LoggerUtils.severe("Could not establish connection to server. IOException caught.");
		}
		LoggerUtils.info("Client connected to " + pServerIP + ": " + aClient);
		aMessageHandler = new EverMessageHandler(aClient);
		aMessageHandler.addMessageListener(this);
		aClient.start();
		try {
			Thread.sleep(500);
		}
		catch (final InterruptedException e1) {
			// Like this is ever going to happen
		}
		LoggerUtils.info("Client started.");
		try {
			aMessageHandler.send(new HandshakeMessage(EverVoidClient.getSettings().getNickname()));
		}
		catch (final EverMessageSendingException e) {
			LoggerUtils.severe("Could not contact server.");
		}
		LoggerUtils.info("Client sent handshake to server.");
	}

	private void doDisconnect()
	{
		if (aConnected) {
			LoggerUtils.info("Disconnecting client.");
			try {
				aClient.disconnect();
			}
			catch (final Exception e) {
				LoggerUtils.severe("Failed to disconnect client!");
			}
			aConnected = false;
			aInLobby = false;
		}
	}

	/**
	 * Called on the next frame after receiving a message, in the UI thread.
	 * 
	 * @param message
	 *            The message that was received
	 */
	private void guiMessageReceived(final EverMessage message)
	{
		final String messageType = message.getType();
		final Json messageContents = message.getJson();
		if (messageType.equals(GameStateMessage.class.getName())) {
			try {
				if (!messageContents.isNull()) {
					for (final EVGlobalMessageListener observer : aGlobalObservers) {
						observer.receivedGameState(new EVGameState(messageContents.getAttribute("state")),
								messageContents.getStringAttribute("player"));
					}
				}
			}
			catch (final BadJsonInitialization e) {
				// we got a bad state from the Server, not a very good sign
				// TODO - warn the server
				Logger.getLogger(EverVoidClient.class.getName()).log(Level.SEVERE, "The Server has sent a bad game state", e);
			}
		}
		else if (messageType.equals(LobbyStateMessage.class.getName())) {
			aConnected = true;
			try {
				final LobbyState lobbyState = new LobbyState(messageContents);
				if (!aInLobby) {
					aInLobby = true;
					EVViewManager.registerView(ViewType.LOBBY, new LobbyView(lobbyState));
					EVViewManager.switchTo(ViewType.LOBBY);
				}
				for (final EVLobbyMessageListener observer : aLobbyObservers) {
					observer.receivedLobbyData(lobbyState);
				}
			}
			catch (final BadJsonInitialization e) {
				// Well crap
			}
		}
		else if (messageType.equals(JoinErrorMessage.class.getName())) {
			EVViewManager.displayError(messageContents.getString());
		}
		else if (message.getType().equals(ServerQuit.class.getName())) {
			// server has shut down, return to main menu
			System.out.println("quitting");
			if (aInLobby) {
				for (final EVLobbyMessageListener observer : aLobbyObservers) {
					observer.serverDied();
				}
			}
		}
		else if (messageType.equals(ChatMessage.class.getName()) || messageType.equals(ServerChatMessage.class.getName())) {
			for (final EVGlobalMessageListener observer : aGlobalObservers) {
				observer.receivedChat(messageContents.getStringAttribute("player"),
						new Color(messageContents.getAttribute("color")), messageContents.getStringAttribute("message"));
			}
		}
		else if (messageType.equals(StartingGameMessage.class.getName())) {
			EVViewManager.switchTo(ViewType.LOADING);
		}
		else if (messageType.equals(TurnMessage.class.getName())) {
			for (final EVGameMessageListener observer : aGameObservers) {
				observer.receivedTurn(new Turn(messageContents, GameView.getGameState()));
			}
		}
		else if (messageType.equals(PlayerDefeatedMessage.class.getName())) {
			for (final EVGameMessageListener observer : aGameObservers) {
				observer.playerLost(GameView.getGameState().getPlayerByName(messageContents.getString()));
			}
		}
		else if (messageType.equals(PlayerVictoryMessage.class.getName())) {
			for (final EVGameMessageListener observer : aGameObservers) {
				observer.playerWon(GameView.getGameState().getPlayerByName(messageContents.getString()));
			}
		}
		else if (messageType.equals(SaveGameStateReply.class.getName())) {
			for (final EVGameMessageListener observer : aGameObservers) {
				try {
					observer.receivedSaveGameReply(messageContents.isNull() ? null : new EVGameState(messageContents));
				}
				catch (final BadJsonInitialization e) {
					// If we really receive a bad game state from the server, something is very wrong
				}
			}
		}
	}

	public boolean isConnected()
	{
		return aConnected;
	}

	@Override
	public void messageReceived(final EverMessage message)
	{
		LoggerUtils.info("Client received: " + message + " | " + message.getJson().toPrettyString());
		if (message.getType().equals(PingMessage.class.getName())) {
			returnPing(message);
			return;
			// ping messages get to skip the queue
		}
		EVViewManager.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				guiMessageReceived(message);
			}
		});
	}

	private void returnPing(final EverMessage message)
	{
		try {
			sInstance.aMessageHandler.send(message);
		}
		catch (final EverMessageSendingException e) {
			// wut?
		}
	}
}
