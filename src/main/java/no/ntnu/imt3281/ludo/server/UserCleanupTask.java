package no.ntnu.imt3281.ludo.server;

import java.net.Socket;
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
    private static ArrayBlockingQueue<Integer> removeIDs;
    private static final Logger LOGGER = Logger.getLogger(ChatTask.class.getName());

    @Override
    public void run() {
        removeIDs = new ArrayBlockingQueue<>(256);
        while (!Server.serverSocket.isClosed()) {
            try {
                Integer clientID = removeIDs.take();
                
                Server.lock.writeLock().lock();
                Server.connections.remove(new ClientInfo(new Socket(), clientID, ""));
                Server.lock.writeLock().unlock();
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
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
