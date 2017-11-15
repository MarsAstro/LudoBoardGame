package no.ntnu.imt3281.ludo.server;

import java.net.DatagramPacket;
import java.net.Socket;
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
    private static ArrayBlockingQueue<String> userTasks;
    private static final Logger LOGGER = Logger.getLogger(UserTask.class.getName());
    private String currentTask;

    @Override
    public void run() {
        userTasks = new ArrayBlockingQueue<>(256);

        while (!Server.serverSocket.isClosed()) {
            try {
                currentTask = userTasks.take();

                int clientEndIndex = currentTask.indexOf(".");
                int clientID = Integer.parseInt(currentTask.substring(0, clientEndIndex));
                int tagEndIndex = currentTask.indexOf(":") + 1;
                String tag = currentTask.substring(clientEndIndex + 1, tagEndIndex);

                switch (tag) {
                    case "Logout:" :
                        handleUserLogoutPacket(clientID, currentTask.substring(tagEndIndex));
                        break;
                    default :
                        break;
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
                + (Server.connections.contains(new ClientInfo(new Socket(), clientID, ""))
                        ? "1" : "-1");
        Platform.runLater(() -> Server.serverGUIController.updateUserList());

        SendToClientTask.send(clientID + "." + ackMessage);
        UserCleanupTask.removeUser(clientID);
    }
}
