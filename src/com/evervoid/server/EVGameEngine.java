package com.evervoid.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.evervoid.json.Json;
import com.evervoid.network.GameStateMessage;
import com.evervoid.network.TurnMessage;
import com.evervoid.network.lobby.LobbyPlayer;
import com.evervoid.network.lobby.LobbyState;
import com.evervoid.state.EVGameState;
import com.evervoid.state.action.Action;
import com.evervoid.state.action.Turn;
import com.evervoid.state.action.ship.ShootShip;
import com.evervoid.state.data.GameData;
import com.evervoid.state.player.Player;
import com.jme3.network.connection.Client;

public class EVGameEngine implements EVServerMessageObserver
{
	private static final Logger aGameEngineLog = Logger.getLogger(EVGameEngine.class.getName());
	private static final String[] sCombatActionTypes = { "ShootShip", "BombPlanet" };
	private final GameData aGameData = new GameData();
	protected EVServerEngine aServer;
	private EVGameState aState;

	EVGameEngine(final EVServerEngine server)
	{
		aGameEngineLog.setLevel(Level.ALL);
		aGameEngineLog.info("Game engine starting with server " + server);
		aServer = server;
		server.registerListener(this);
	}

	private void calculateTurn(final Turn turn)
	{
		aGameEngineLog.info("Game engine received turn: " + turn);
		// First: Combat actions
		final List<Action> combatActions = turn.getActionsOfType(sCombatActionTypes);
		for (final Action act : combatActions) {
			if (act instanceof ShootShip) {
				((ShootShip) act).rollDamage();
			}
		}
		// Second: Movement actions
		// TODO: Resolve conflicts etc
		aState.commitTurn(turn);
		aServer.sendAll(new TurnMessage(turn));
	}

	@Override
	public void messageReceived(final String type, final Client client, final Json content)
	{
		if (type.equals("handshake")) {
			// FIXME: Server should not handle handshake messages at all
			// aServer.send(client, new GameStateMessage(aState));
		}
		else if (type.equals("turn")) {
			calculateTurn(new Turn(content, aState));
		}
		else if (type.equals("startgame")) {
			final LobbyState lobby = new LobbyState(content);
			final List<Player> playerList = new ArrayList<Player>();
			for (final LobbyPlayer player : lobby) {
				playerList.add(new Player(player.getNickname(), player.getRace(), player.getColorName(), aGameData));
			}
			setState(new EVGameState(playerList, aGameData));
			aServer.sendAll(new GameStateMessage(aState));
		}
	}

	protected void setState(final EVGameState state)
	{
		aState = state;
	}
}
