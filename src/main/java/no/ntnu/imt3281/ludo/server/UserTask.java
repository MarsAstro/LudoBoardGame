package no.ntnu.imt3281.ludo.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * Handles tasks related to user actions
 * 
 * @author Marius
 *
 */
public class UserTask implements Runnable {
    private static ArrayBlockingQueue<String> userTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(UserTask.class.getName());
    private String currentTask;

    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            try {
                currentTask = userTasks.take();

                int clientEndIndex = currentTask.indexOf(".");
                int clientID = Integer.parseInt(currentTask.substring(0, clientEndIndex));
                int tagEndIndex = currentTask.indexOf(":") + 1;
                String tag = currentTask.substring(clientEndIndex + 1, tagEndIndex);

                if ("Logout:".equals(tag)) {
                    handleUserLogoutPacket(clientID, currentTask.substring(tagEndIndex));
                }
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
     * @throws InterruptedException
     *             Thrown if interrupted while waiting
     */
    public static void blockingPut(String message) {
        try {
            userTasks.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void handleUserLogoutPacket(Integer clientID, String message) {
        String ackMessage = "User.Logout:"
                + (Server.clients.contains(new ClientInfo(clientID)) ? "1" : "-1");

        SendToClientTask.send(clientID + "." + ackMessage);
    }
}
