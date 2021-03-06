/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

import no.ntnu.imt3281.ludo.logic.Ludo;

/**
 * Data structure containing information about a ludo game.
 * 
 * @author Marius
 */
public class GameInfo {
    int gameID;
    ArrayList<ClientInfo> clients;
    Ludo ludo;

    /**
     * Only initializes the gameID field, used for language-specific search
     * methods
     * 
     * @param gameID
     *            The ID of the game to look for
     */
    GameInfo(int gameID) {
        this.gameID = gameID;
    }

    /**
     * Initializes all fields and adds the client to the new game
     * 
     * @param gameID
     *            The ID of the new game
     * @param client
     *            The Client initiating the new game
     */
    GameInfo(int gameID, ClientInfo client) {
        ludo = new Ludo();

        ludo.addPlayerListener(e -> {
            for (ClientInfo currentClient : clients) {
                SendToClientTask.send(currentClient.clientID + ".Ludo.Player:" + gameID + "," + e);
            }
        });

        ludo.addDiceListener(e -> {
            for (ClientInfo currentClient : clients) {
                SendToClientTask.send(currentClient.clientID + ".Ludo.Dice:" + gameID + "," + e
                        + "," + ludo.canMove());
            }
        });

        ludo.addPieceListener(e -> {
            for (ClientInfo currentClient : clients) {
                SendToClientTask.send(currentClient.clientID + ".Ludo.Piece:" + gameID + "," + e);
            }
        });

        ludo.addPlayer(client.username);
        this.gameID = gameID;
        clients = new ArrayList<>();
        clients.add(client);
    }

    @Override
    public boolean equals(Object other) {
        boolean isEqual = false;

        if (other instanceof GameInfo) {
            GameInfo gameInfo = (GameInfo) other;
            isEqual = gameID == gameInfo.gameID;
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return gameID * 47 - 14 % 134;
    }

    private boolean isJoinable(ClientInfo client) {
        boolean isAlreadyInGame = false;
        for (ClientInfo clientInGame : clients) {
            if (client.equals(clientInGame)) {
                isAlreadyInGame = true;
                break;
            }
        }

        return !isAlreadyInGame && clients.size() < 4 && "Initiated".equals(ludo.getStatus());
    }

    /**
     * Adding player to game if there is more room and it has not started
     * 
     * @param client
     *            The client to be added
     * @return True if the player was added to the game
     */
    public boolean addPlayer(ClientInfo client) {
        boolean success = false;
        if (isJoinable(client)) {
            clients.add(client);
            ludo.addPlayer(client.username);
            success = true;
        }
        return success;
    }

    /**
     * Removing player from game, if game has not started
     * 
     * @param clientID
     *            The client to be removed
     * @return Return the new name of the player removed (either Inactive or No
     *         player)
     */
    public String removePlayer(int clientID) {
        String returnName = "";
        int index = clients.indexOf(new ClientInfo(clientID));
        if (index >= 0) {
            ClientInfo client = clients.get(index);

            int playerIndex = ludo.getIndexOfPlayer(client.username);

            if (playerIndex != -1) {
                String status = ludo.getStatus();
                String ludoPlayerName = ludo.getPlayerName(playerIndex);

                if ("Initiated".equals(status)) {
                    clients.remove(index);
                    ludo.discardPlayer(ludoPlayerName);
                    returnName = "Discard";
                } else if ("Started".equals(status)) {
                    clients.remove(index);
                    ludo.removePlayer(ludoPlayerName);
                    returnName = ludo.getPlayerName(playerIndex);
                }
            }
        }

        return returnName;
    }

    /**
     * Gets ClientInfo from ID
     * 
     * @param clientID
     *            ID of client to be searched for
     * @return Client info or null if player doesn't exist
     */
    public ClientInfo getClient(int clientID) {
        int index = clients.indexOf(new ClientInfo(clientID));
        return index == -1 ? null : clients.get(index);
    }
}
