package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * Handles tasks releated to a user account
 * 
 * @author oyste
 *
 */
public class ClientUserTask implements Runnable {
    private static ArrayBlockingQueue<String> userTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(ClientLudoTask.class.getName());
    String currentTask;

    @Override
    public void run() {
        while (!Client.socket.isClosed()) {
            try {
                currentTask = userTasks.take();

                handleReceivedUserPacket(currentTask);
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
    public static void addNewTask(String message) {

        try {
            userTasks.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void handleReceivedUserPacket(String message) {

        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);

        switch (tag) {
            case "Login:" :
                Platform.runLater(
                        () -> Client.connectController.handleServerLoginResponse(ackMessage));
                break;
            case "Register:" :
                Platform.runLater(
                        () -> Client.connectController.handleServerRegisterResponse(ackMessage));
                break;
            case "Logout:" :
                handleLogoutResponse(ackMessage);
                break;
            case "List:" :
                Platform.runLater(() -> Client.ludoController.getChallengeListContoller()
                        .addPlayersName(ackMessage));
                break;
            case "Wins:" :
                Platform.runLater(
                        () -> Client.ludoController.addWins(Integer.parseInt(ackMessage)));
                break;
            default :
                break;
        }
    }

    private void handleLogoutResponse(String ackMessage) {
        Platform.runLater(() -> Client.ludoController.handleServerLogoutResponse(ackMessage));
        if (Integer.parseInt(ackMessage) == 1) {
            try {
                Client.socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
}
