package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Marius
 *
 */
public class SendToClientTask implements Runnable {
    private static ArrayBlockingQueue<String> sendTasks;
    private static final Logger LOGGER = Logger.getLogger(SendToClientTask.class.getName());
    private String currentTask;

    @Override
    public void run() {
        sendTasks = new ArrayBlockingQueue<>(256);
        while (!Server.serverSocket.isClosed()) {
            try {
                currentTask = sendTasks.take();

                int idEndIndex = currentTask.indexOf(".");
                Integer clientID = Integer.parseInt(currentTask.substring(0, idEndIndex));
                currentTask = currentTask.substring(idEndIndex + 1);

                Server.lock.readLock().lock();
                int connectionIndex = Server.connections
                        .indexOf(new ClientInfo(new Socket(), clientID, ""));
                if (connectionIndex != -1) {
                    Server.connections.get(connectionIndex).connection.getOutputStream()
                            .write(currentTask.getBytes());
                    Server.connections.get(connectionIndex).connection.getOutputStream().flush();
                }
                Server.lock.readLock().unlock();
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
