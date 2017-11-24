package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.ChatListController;
import no.ntnu.imt3281.ludo.gui.ChatWindowController;

/**
 * Handles tasks related to chat rooms and messages
 * 
 * @author oyste
 *
 */
public class ClientChatTask implements Runnable {
    private static ArrayBlockingQueue<String> chatTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(ClientLudoTask.class.getName());
    String currentTask;

    @Override
    public void run() {
        while (!Client.socket.isClosed()) {
            try {
                currentTask = chatTasks.take();

                handleReceivedChatPacket(currentTask);
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
            chatTasks.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void handleReceivedChatPacket(String message) {
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);

        switch (tag) {
            case "Say:" :
                handleSayChatPacket(ackMessage);
                break;
            case "Join:" :
                Platform.runLater(() -> Client.ludoController.handleServerJoinChat(
                        Integer.parseInt(ackMessage.split(",")[0]), ackMessage));
                break;
            case "Name:" :
                handleNameChatPacket(ackMessage);
                break;
            case "RemoveName:" :
                handleLeaveChatPacket(ackMessage);
                break;
            case "ListName:" :
                handleChatListNamePacket(ackMessage);
                break;
            case "Create:" :
                handleCreateChatPacket(ackMessage);
                break;
            default :
                break;
        }
    }

    private void handleCreateChatPacket(String ackMessage) {
        String[] messages = ackMessage.split(",");
        boolean nameExists = Boolean.parseBoolean(messages[0]);

        if (!nameExists) {
            String message = messages[1] + "," + messages[2];
            Platform.runLater(() -> Client.ludoController.handleServerInitChat(message));
        }
    }

    private void handleChatListNamePacket(String ackMessage) {
        ChatListController clc = Client.ludoController.getChatListContoller();
        if (clc != null) {
            Platform.runLater(() -> clc.addChatName(ackMessage));
        }
    }

    private void handleLeaveChatPacket(String ackMessage) {
        String[] messages = ackMessage.split(",");
        int chatID = Integer.parseInt(messages[0]);
        String name = messages[1];

        ChatWindowController cwc = Client.ludoController.getChatWindowController(chatID);
        if (cwc != null) {
            Platform.runLater(() -> cwc.removeChatName(name));
        }
    }

    private void handleNameChatPacket(String ackMessage) {
        String[] messages = ackMessage.split(",");
        int chatID = Integer.parseInt(messages[0]);
        String name = messages[1];

        ChatWindowController cwc = Client.ludoController.getChatWindowController(chatID);
        if (cwc != null) {
            Platform.runLater(() -> cwc.addChatName(name));
        }
    }

    private void handleSayChatPacket(String message) {
        String[] messages = message.split(",");
        int chatID = Integer.parseInt(messages[0]);
        String sayMessage = messages[1];

        ChatWindowController cwc = Client.ludoController.getChatWindowController(chatID);
        if (cwc != null) {
            Platform.runLater(() -> cwc.addChatMessage(sayMessage));
        }
    }

}
