package no.ntnu.imt3281.ludo.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * Removes users from server
 * 
 * @author Marius
 *
 */
public class UserCleanupTask implements Runnable {
    private static ArrayBlockingQueue<Integer> removeIDs = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(ChatTask.class.getName());

    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            try {
                Integer clientID = removeIDs.take();

                Server.clientLock.writeLock().lock();
                Server.clients.remove(new ClientInfo(clientID));
                Server.clientLock.writeLock().unlock();

                Server.gameLock.writeLock().lock();
                for (int game = 0; game < Server.games.size(); ++game) {
                    Server.games.get(game).removePlayer(clientID);
                    if (Server.games.get(game).ludo.activePlayers() <= 0) {
                        Server.games.remove(game);
                        game--;
                    }
                }
                Server.gameLock.writeLock().unlock();

                Server.chatLock.writeLock().lock();
                for (int chat = 0; chat < Server.chats.size(); ++chat) {
                    ChatInfo chatInfo = Server.chats.get(chat);

                    int clientIndex = chatInfo.clients.indexOf(new ClientInfo(clientID));
                    String clientToRemoveName = chatInfo.clients.get(clientIndex).username;

                    chatInfo.removeClient(clientID);
                    if (chatInfo.clients.size() == 0 && chat != 0) {
                        Server.chats.remove(chat);
                        chat--;
                    } else {
                        for (ClientInfo client : chatInfo.clients) {
                            SendToClientTask.send(client.clientID + ".Chat.RemoveName:"
                                    + chatInfo.chatID + "," + clientToRemoveName);
                        }
                    }
                }
                Server.chatLock.writeLock().unlock();

                Platform.runLater(() -> {
                    Server.serverGUIController.updateUserList();
                    Server.serverGUIController.updateGameList();
                });
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Stage a user for removal
     * 
     * @param clientID
     *            ID of user to be removed
     */
    public static void removeUser(Integer clientID) {
        try {
            removeIDs.put(clientID);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
