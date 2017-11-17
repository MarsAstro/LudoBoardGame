package no.ntnu.imt3281.ludo.server;

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

    private static ArrayBlockingQueue<String> ludoTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(LudoTask.class.getName());
    String currentTask;

    @Override
    public void run() {
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

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);
            for (ClientInfo clientInGame : game.clients) {
                for (int i = 0; i < game.ludo.nrOfPlayers(); i++) {
                    SendToClientTask.send(clientInGame.clientID + ".Ludo.Name:" + message + "," + i
                            + "," + game.ludo.getPlayerName(i));
                    if (game.ludo.activePlayers() > 1) {
                        SendToClientTask.send(clientInGame.clientID + ".Ludo.Player:" + gameID + ","
                                + Ludo.RED + "," + PlayerEvent.PLAYING);
                    }
                }
            }
        }
    }

    private void handleLudoJoinRandomPacket(int clientID, String message) {
        StringBuilder ackMessage = new StringBuilder("Ludo.Join:");

        int index = Server.connections.indexOf(new ClientInfo(clientID));
        ClientInfo client = Server.connections.get(index);

        boolean foundGame = false;
        for (GameInfo game : Server.games) {
            if (game.addPlayer(client)) {
                foundGame = true;
                ackMessage.append(Integer.toString(game.gameID) + ","
                        + game.ludo.getIndexOfPlayer(client.username));
                break;
            }
        }
        if (!foundGame) {
            ackMessage.append(Integer.toString(Server.nextGameID) + "," + 0);
            Server.games.add(new GameInfo(Server.nextGameID++, client));
        }

        SendToClientTask.send(clientID + "." + ackMessage);
    }

    private void handleLudoThrowPacket(int clientID, String message) {
        int gameID = Integer.parseInt(message);

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);
            game.ludo.throwDice();
        }
    }

    private void handleLudoMovePacket(int clientID, String message) {
        String[] messages = message.split(",");
        int gameID = Integer.parseInt(messages[0]);
        int playerID = Integer.parseInt(messages[1]);
        int from = Integer.parseInt(messages[2]);
        int to = Integer.parseInt(messages[3]);

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);

            int newFrom = -1;
            for (int piece = 0; piece < 4; ++piece) {
                if (game.ludo.globalPiecePositions[playerID][piece] == from) {
                    newFrom = game.ludo.piecePositions[playerID][piece];
                } ;
            }

            if (from < 16) {
                game.ludo.movePiece(playerID, 0, 1);
            } else if (to < from && newFrom != -1) {
                int newTo = newFrom + to - from + 52;
                game.ludo.movePiece(playerID, newFrom, newTo);
            } else if (to < 68 && newFrom != -1) {
                int newTo = newFrom + (to - from);
                game.ludo.movePiece(playerID, newFrom, newTo);
            } else if (newFrom != -1) {
                int newTo = game.ludo.finalTilesLudoBoardGridToUserGrid(playerID, to);
                game.ludo.movePiece(playerID, newFrom, newTo);
            }
        }
    }

    private void handleLudoLeavePacket(int clientID, String message) {
        int gameID = Integer.parseInt(message);

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);
            Server.lock.readLock().lock();
            int removeClientIndex = Server.connections.indexOf(new ClientInfo(clientID));

            if (removeClientIndex >= 0) {
                ClientInfo removeClient = Server.connections.get(removeClientIndex);

                int playerIndex = game.ludo.getIndexOfPlayer(removeClient.username);

                if (playerIndex >= 0) {
                    String newName = game.removePlayer(clientID);
                    if (game.ludo.activePlayers() > 0) {
                        for (ClientInfo client : game.clients) {
                            SendToClientTask.send(client.clientID + ".Ludo.Name:" + game.gameID
                                    + "," + playerIndex + "," + newName);
                        }
                    } else {
                        Server.games.remove(game);
                    }
                }
            }

            Server.lock.readLock().unlock();
        }

    }
}
