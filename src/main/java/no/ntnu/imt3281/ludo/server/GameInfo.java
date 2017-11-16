/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

import no.ntnu.imt3281.ludo.logic.DiceListener;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.PieceListener;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;
import no.ntnu.imt3281.ludo.logic.PlayerListener;

/**
 * @author Marius $Disgusting guy}
 */
public class GameInfo {
    int gameID;
    ArrayList<ClientInfo> clients;
    Ludo ludo;

    PlayerListener playerListener;
    DiceListener diceListener;
    PieceListener pieceListener;

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
                SendToClientTask.send(currentClient.clientID + ".Ludo.Dice:" + gameID + "," + e);
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
     */
    public void removePlayer(int clientID) {
        int index = clients.indexOf(new ClientInfo(clientID));
        if (index >= 0) {
            ClientInfo client = clients.get(index);

            String status = ludo.getStatus();
            if ("Initiated".equals(status)) {
                clients.remove(index);
                ludo.discardPlayer(client.username);
            } else if ("Started".equals(status)) {
                clients.remove(index);
                ludo.removePlayer(client.username);
            }
        }
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
