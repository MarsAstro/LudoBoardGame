package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Marius
 *
 */
public class SendToClientTask implements Runnable {
    private static ArrayBlockingQueue<String> sendTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(SendToClientTask.class.getName());
    private String currentTask;

    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            try {
                currentTask = sendTasks.take();

                int idEndIndex = currentTask.indexOf(".");
                Integer clientID = Integer.parseInt(currentTask.substring(0, idEndIndex));
                currentTask = currentTask.substring(idEndIndex + 1) + ";";

                Server.clientLock.readLock().lock();
                int connectionIndex = Server.connections.indexOf(new ClientInfo(clientID));
                if (connectionIndex >= 0) {
                    Server.connections.get(connectionIndex).connection.getOutputStream()
                            .write(currentTask.getBytes("UTF-8"));
                    Server.connections.get(connectionIndex).connection.getOutputStream().flush();
                }
                Server.clientLock.readLock().unlock();
                if (currentTask.contains("Logout:")) {
                    UserCleanupTask.removeUser(clientID);
                }
            } catch (InterruptedException | IOException e) {
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
    public static void send(String message) {
        try {
            sendTasks.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
