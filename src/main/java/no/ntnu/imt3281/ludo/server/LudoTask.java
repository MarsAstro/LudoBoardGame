package no.ntnu.imt3281.ludo.server;

import java.net.DatagramPacket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;

/**
 * Handles tasks related to ludo actions
 * 
 * @author Marius
 *
 */
public class LudoTask implements Runnable {

    private static ArrayBlockingQueue<String> ludoTasks;
    private static final Logger LOGGER = Logger.getLogger(LudoTask.class.getName());
    String currentTask;

    @Override
    public void run() {
        ludoTasks = new ArrayBlockingQueue<>(256);
        while (!Server.serverSocket.isClosed()) {
            try {
                currentTask = ludoTasks.take();

                int endIDIndex = currentTask.indexOf(".");
                int clientID = Integer.parseInt(currentTask.substring(0, endIDIndex));

                handleMessage(clientID, currentTask.substring(endIDIndex + 1));
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Put a new task in queue
     * 
     * @param message
     *            Message to be put in queue
     */
    public static void blockingPut(String message) {
        try {
            ludoTasks.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void handleMessage(int clientID, String message) {
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);

        switch (tag) {
            case "Throw:" :
                handleLudoThrowPacket(clientID, message.substring(tagEndIndex));
                break;
            case "Move:" :
                handleLudoMovePacket(clientID, message.substring(tagEndIndex));
                break;
            case "JoinRandom:" :
                handleLudoJoinRandomPacket(clientID, message.substring(tagEndIndex));
                break;
            case "Challenge:" :
                // TODO
                break;
            case "Init:" :
                handleLudoInitPacket(message.substring(tagEndIndex));
                break;
            case "Leave:" :
                handleLudoLeavePacket(clientID, message.substring(tagEndIndex));
                break;
            default :
                break;
        }
    }

    private void handleLudoInitPacket(String message) {
        int gameID = Integer.parseInt(message);

        for (GameInfo game : Server.games) {
            if (game.gameID == gameID) {
                for (ClientInfo clientInGame : game.clients) {
                    for (int i = 0; i < game.ludo.nrOfPlayers(); i++) {
                        SendToClientTask.send(clientInGame.clientID + ".Ludo.Name:" + message + ","
                                + i + "," + game.ludo.getPlayerName(i));
                        if (game.ludo.activePlayers() > 1) {
                            SendToClientTask.send(clientInGame.clientID + ".Ludo.Player:" + gameID
                                    + "," + Ludo.RED + "," + PlayerEvent.PLAYING);
                        }
                    }
                }
            }
        }
    }

    private void handleLudoJoinRandomPacket(int clientID, String message) {
        String ackMessage = "Ludo.Join:";

        ClientInfo client = new ClientInfo(new Socket(), clientID, "RandomName");
        int index = Server.connections.indexOf(client);
        client = Server.connections.get(index);

        boolean foundGame = false;
        for (GameInfo game : Server.games) {
            if (game.addPlayer(client)) {
                foundGame = true;
                ackMessage += Integer.toString(game.gameID) + ","
                        + game.ludo.getIndexOfPlayer(client.username);
                break;
            }
        }
        if (!foundGame) {
            ackMessage += Integer.toString(Server.nextGameID) + "," + 0;
            Server.games.add(new GameInfo(Server.nextGameID++, client));
        }

        SendToClientTask.send(clientID + "." + ackMessage);
    }

    private void handleLudoThrowPacket(int clientID, String message) {
        String ackMessage = "";

        // TODO
    }

    private void handleLudoMovePacket(int clientID, String message) {
        String ackMessage = "";
        // TODO
    }

    private void handleLudoLeavePacket(int clientID, String message) {
        int gameID = Integer.parseInt(message);

        for (GameInfo game : Server.games) {
            if (game.gameID == gameID) {
                game.removePlayer(clientID);
                if (game.ludo.activePlayers() <= 0) {
                    Server.games.remove(game);
                }
                break;
            }
        }
    }
}
