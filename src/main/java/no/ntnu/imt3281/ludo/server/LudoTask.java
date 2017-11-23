package no.ntnu.imt3281.ludo.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
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
    static ArrayList<Challenge> challenges = new ArrayList<>();
    static ReadWriteLock challengesLock;

    private static ArrayList<ClientInfo> randomQueue;
    private static ReadWriteLock randomQueueLock;
    private String currentTask;
    private int nextChallengeID = 0;

    @Override
    public void run() {
        randomQueue = new ArrayList<>();
        randomQueueLock = new ReentrantReadWriteLock();
        challengesLock = new ReentrantReadWriteLock();

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
                handleLudoChallengePacket(clientID, message.substring(tagEndIndex));
                break;
            case "ChallengeConfirm:" :
                handleLudoChallengeConfirmPacket(clientID, message.substring(tagEndIndex));
                break;
            case "ChallengeValidation:" :
                handleLudoChallengeValidationPacket(clientID, message.substring(tagEndIndex));
                break;
            case "Init:" :
                handleLudoInitPacket(message.substring(tagEndIndex));
                break;
            case "Leave:" :
                handleLudoLeavePacket(clientID, message.substring(tagEndIndex));
                break;
            case "Chat:" :
                handleLudoChatPacket(clientID, message.substring(tagEndIndex));
                break;
            default :
                break;
        }
    }

    private void handleLudoChallengeValidationPacket(int clientID, String message) {

        Challenge curChallenge = null;
        boolean playableGame = Boolean.parseBoolean(message);
        
        challengesLock.readLock().lock();
        for (int challenge = 0; challenge < challenges.size(); challenge++) {
            if (challenges.get(challenge).clientIDs.get(0) == clientID) {
                curChallenge = challenges.get(challenge);
            }
            break;
        }
        challengesLock.readLock().unlock();

        if (playableGame == true && curChallenge != null) {
            Server.clientLock.readLock().lock();
            int index = Server.clients.indexOf(new ClientInfo(clientID));
            ClientInfo newClient = Server.clients.get(index);
            Server.clientLock.readLock().unlock();
            
            Server.gameLock.writeLock().lock();
            Server.games.add(new GameInfo(Server.nextGameID++, newClient));

            GameInfo newGame = Server.games.get(Server.games.size() - 1);
            Server.gameLock.writeLock().unlock();

            Server.clientLock.readLock().lock();
            for (int i = 0; i < curChallenge.clientIDs.size(); i++) {
                int cliID = curChallenge.clientIDs.get(i);
                int newPlayerIndex = Server.clients.indexOf(new ClientInfo(cliID));
                newGame.addPlayer(Server.clients.get(newPlayerIndex));
                SendToClientTask.send(cliID + ".Ludo.Join:" + newGame.gameID + "," + i);
            }
            Server.clientLock.readLock().unlock();
            
            challengesLock.writeLock().lock();
            challenges.remove(curChallenge);
            challengesLock.writeLock().unlock();
            Platform.runLater(() -> Server.serverGUIController.updateGameList());
            initGameForAllClients(newGame);
        } else if (curChallenge != null) {
            challengesLock.writeLock().lock();
            challenges.remove(curChallenge);
            challengesLock.writeLock().unlock();
        }
    }

    private void handleLudoChallengeConfirmPacket(int clientID, String message) {
        boolean confirm = Boolean.parseBoolean(message);

        challengesLock.readLock().lock();
        for (int challenge = 0; challenge < challenges.size(); challenge++) {
            for (int challengedClientID = 1; challengedClientID < challenges
                    .get(challenge).clientIDs.size(); challengedClientID++) {
                int cliID = challenges.get(challenge).clientIDs.get(challengedClientID);
                if (clientID == cliID) {
                    Server.clientLock.readLock().lock();
                    int clientIndex = Server.clients.indexOf(new ClientInfo(cliID));

                    if (clientIndex >= 0) {
                        SendToClientTask
                                .send(challenges.get(challenge).clientIDs.get(0) + ".Ludo.ChallengeConfirm:"
                                        + Server.clients.get(clientIndex).username + ","
                                        + Boolean.toString(confirm));
                    }
                    Server.clientLock.readLock().unlock();
                }
            }
        }
        challengesLock.readLock().unlock();
    }

    private void handleLudoChallengePacket(int clientID, String packetMessage) {
        String[] usernames = packetMessage.split(",");
        
        challengesLock.writeLock().lock();
        challenges.add(new Challenge(nextChallengeID++, clientID));
        challengesLock.writeLock().unlock();
        
        Server.clientLock.readLock().lock();
        for (String user : usernames) {
            for (ClientInfo client : Server.clients) {
                if (user.equals(client.username)) {
                    challenges.get(nextChallengeID - 1).clientIDs.add(client.clientID);
                    SendToClientTask.send(client.clientID + ".Ludo.Challenge:" + client.username);
                }
            }
        }
        Server.clientLock.readLock().unlock();
    }

    private void handleLudoChatPacket(int clientID, String message) {
        int endGameIDIndex = message.indexOf(",");
        int gameID = Integer.parseInt(message.substring(0, endGameIDIndex));

        Server.gameLock.readLock().lock();
        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        Server.chatLock.readLock().lock();
        int clientIndex = Server.clients.indexOf(new ClientInfo(clientID));
        if (gameIndex >= 0 && clientIndex >= 0) {
            String talkingClientName = Server.clients.get(clientIndex).username;
            GameInfo gameInfo = Server.games.get(gameIndex);
            for (ClientInfo client : gameInfo.clients) {
                SendToClientTask.send(client.clientID + ".Ludo.Chat:" + gameInfo.gameID + ","
                        + talkingClientName + ": " + message.substring(endGameIDIndex + 1));
            }
        }
        Server.chatLock.readLock().unlock();
        Server.gameLock.readLock().unlock();
    }

    private void handleLudoInitPacket(String message) {
        int gameID = Integer.parseInt(message);

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);
            initGameForAllClients(game);
        }
    }

    private void initGameForAllClients(GameInfo game) {
        for (ClientInfo clientInGame : game.clients) {
            for (int playerIndex = 0; playerIndex < game.ludo.nrOfPlayers(); playerIndex++) {
                SendToClientTask.send(clientInGame.clientID + ".Ludo.Name:" + game.gameID + ","
                        + playerIndex + "," + game.ludo.getPlayerName(playerIndex));
                if (game.ludo.activePlayers() > 1) {
                    SendToClientTask.send(clientInGame.clientID + ".Ludo.Player:" + game.gameID
                            + "," + Ludo.RED + "," + PlayerEvent.PLAYING);
                }
            }
        }
    }

    /**
     * Removes a client from queues he is in
     * 
     * @param client
     *            client to be removed
     */
    public static void removeFromQueue(ClientInfo client) {
        randomQueueLock.writeLock().lock();
        randomQueue.remove(client);
        randomQueueLock.writeLock().unlock();
    }

    private void handleLudoJoinRandomPacket(int clientID, String message) {
        int index = Server.clients.indexOf(new ClientInfo(clientID));
        ClientInfo newClient = Server.clients.get(index);

        randomQueueLock.writeLock().lock();
        if (!randomQueue.contains(newClient)) {
            randomQueue.add(newClient);
            SendToClientTask.send(newClient.clientID + ".Ludo.RandomSuccess:");

            if (randomQueue.size() == 4) {
                Server.gameLock.writeLock().lock();
                Server.games.add(new GameInfo(Server.nextGameID++, randomQueue.get(0)));
                Server.gameLock.writeLock().unlock();

                GameInfo newGame = Server.games.get(Server.games.size() - 1);
                newGame.addPlayer(randomQueue.get(1));
                newGame.addPlayer(randomQueue.get(2));
                newGame.addPlayer(randomQueue.get(3));

                Platform.runLater(() -> Server.serverGUIController.updateGameList());

                for (int clientIndex = 0; clientIndex < randomQueue.size(); clientIndex++) {
                    SendToClientTask.send(randomQueue.get(clientIndex).clientID
                            + ".Ludo.JoinRandom:" + newGame.gameID + "," + clientIndex);
                }

                initGameForAllClients(newGame);
                randomQueue.clear();
            }
        }
        randomQueueLock.writeLock().unlock();
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
        int piece = Integer.parseInt(messages[4]);

        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);

            int[][] globalPiecePositions = game.ludo.getGlobalPiecePositions();
            int[][] piecePositions = game.ludo.getPiecePositions();

            int newFrom = globalPiecePositions[playerID][piece] == from
                    ? piecePositions[playerID][piece]
                    : -1;
            int newTo = -1;

            if (from < 16 && newFrom != -1) {
                newFrom = 0;
                newTo = 1;
            } else if (to < from) {
                newTo = newFrom + to - from + 52;
            } else if (to < 68) {
                newTo = newFrom + (to - from);
            } else {
                newTo = game.ludo.finalTilesLudoBoardGridToUserGrid(playerID, to);
            }

            if (newFrom != -1 && newTo != -1) {
                game.ludo.setSelectedPiece(piece);
                game.ludo.movePiece(playerID, newFrom, newTo);
            }

            CheckWinner(game);
        }
    }

    private void CheckWinner(GameInfo game) {
        int winner = game.ludo.getWinner();

        if (winner != -1) {
            String winnerName = game.ludo.getPlayerName(winner);
            String strippedName = winnerName.substring(winnerName.indexOf(":") + 2);
            int clientID = -1;

            for (ClientInfo client : game.clients) {
                SendToClientTask.send(client.clientID + ".Ludo.Name:" + game.gameID + "," + winner
                        + "," + winnerName);
                
                if (client.username.equals(strippedName))
                {
                    clientID = client.clientID;
                }
            }

            try {
                PreparedStatement userQuery;
                userQuery = Server.database
                        .prepareStatement("UPDATE Accounts SET Wins = Wins + 1 WHERE Username = ?");
                userQuery.setString(1, strippedName);
                userQuery.executeQuery();
                
                SendToClientTask.send(clientID + ".User.Wins:1");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private void handleLudoLeavePacket(int clientID, String message) {
        int gameID = Integer.parseInt(message);

        Server.gameLock.writeLock().lock();
        int gameIndex = Server.games.indexOf(new GameInfo(gameID));
        if (gameIndex >= 0) {
            GameInfo game = Server.games.get(gameIndex);
            Server.clientLock.readLock().lock();
            int removeClientIndex = Server.clients.indexOf(new ClientInfo(clientID));

            if (removeClientIndex >= 0) {
                String removeClientName = Server.clients.get(removeClientIndex).username;

                int playerIndex = game.ludo.getIndexOfPlayer(removeClientName);

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

            Server.clientLock.readLock().unlock();
            Platform.runLater(() -> Server.serverGUIController.updateGameList());
        }
        Server.gameLock.writeLock().unlock();
    }
}
