/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.net.InetAddress;
import java.util.ArrayList;

import no.ntnu.imt3281.ludo.logic.Ludo;

/**
 * @author Marius $Disgusting guy}
 */
public class GameInfo {
    int gameID;
    ArrayList<ClientInfo> clients;
    Ludo ludo;

    GameInfo(int gameID, ClientInfo client) {
        ludo = new Ludo();
        ludo.addPlayer(client.username);
        this.gameID = gameID;
        clients = new ArrayList<>();
        clients.add(client);
    }

    private boolean isJoinable(ClientInfo client) {
        boolean isAlreadyInGame = false;
        for (ClientInfo clientInGame : clients) {
            if (client.equals(clientInGame)) {
                isAlreadyInGame = true;
                break;
            }
        }
        
        return !isAlreadyInGame && clients.size() < 4 && ludo.getStatus().equals("Initiated");
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
     * @param client
     *            The client to be added
     */
    public void removePlayer(ClientInfo client) {
        String status = ludo.getStatus();
        if (status == "Initiated") {
            clients.remove(client);
            ludo.discardPlayer(client.username);
        } else if (status == "Started") {
            clients.remove(client);
            ludo.removePlayer(client.username);
        }
    }

    /**
     * Gets ClientInfo from address and port. ID and username is hardcoded when
     * searching for client because equals operator only considers address and
     * port.
     * 
     * @param address
     *            Address client is connected from
     * @param port
     *            Port client is connected to
     * @return Client info or null if player doesn't exist
     */
    public ClientInfo getClient(InetAddress address, int port) {
        int index = clients.indexOf(new ClientInfo(-1, address, port, "Random name"));
        return index == -1 ? null : clients.get(index);
    }
}
