package com.evervoid.network.message;

import com.evervoid.json.Json;
import com.evervoid.network.EVMessage;
import com.evervoid.network.lobby.LobbyState;

/**
 * Sent by the server, this message contains general server info
 */
public class ServerInfoMessage extends EVMessage
{
    public ServerInfoMessage(final Json json)
    {
        super(json);
    }

    public ServerInfoMessage(final LobbyState lobby, final boolean inGame)
    {
        super(new Json().setAttribute("ingame", inGame).setAttribute("players", lobby.getNumOfPlayers())
                        .setAttribute("name", lobby.getServerName()));
    }
}
