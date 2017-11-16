/**
 * 
 */
package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.GameBoardController;

/**
 * @author Marius
 *
 */
public class ClientInputTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientInputTask.class.getName());
    private byte[] inputData = new byte[100];

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Client.socket.isClosed()) {
            try {
                int length = Client.socket.getInputStream().read(inputData);
                
                String packet = new String(inputData, 0, length, "UTF-8");
                String[] messages = packet.split(";");
                for (String message : messages) {
                    handleReceivedPacket(message);
                }

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Handles received packet
     * 
     * @param inputData
     *            The received packet
     */
    private void handleReceivedPacket(String message) {
        int tagEndIndex = message.indexOf(".") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);
        switch (tag) {
            case "User." :
                handleReceivedUserPacket(ackMessage);
                break;
            case "Ludo." :
                handleReceivedLudoPacket(ackMessage);
                break;
            case "Chat." :
                // TODO
                break;
            default :
                // Packet no proper tag
                break;
        }
    }

    private void handleReceivedLudoPacket(String message) {

        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);

        switch (tag) {
            case "Dice:" :
                handleReceivedLudoDice(ackMessage);
                break;
            case "Piece:" :
                break;
            case "Player:" :
                handleReceivedLudoPlayer(ackMessage);
                break;
            case "Name:" :
                handleReceivedLudoNamePacket(ackMessage);
                break;
            case "Finish:" :
                break;
            case "Challenge:" :
                break;
            case "Join:" :
                Platform.runLater(() -> Client.ludoController.handleServerJoinGame(ackMessage));
                break;
            case "Leave:" :
                break;
            default :
                break;
        }
    }

    private void handleReceivedLudoDice(String ackMessage) {
        String[] messageInfos = ackMessage.split(",");
        int gameID = Integer.parseInt(messageInfos[0]);
        int playerIndex = Integer.parseInt(messageInfos[1]);
        int dice = Integer.parseInt(messageInfos[2]);
        
        GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
        Platform.runLater(() -> gbc.updateDice(playerIndex, dice));
    }

    private void handleReceivedLudoPlayer(String ackMessage) {
        String[] messageInfos = ackMessage.split(",");
        int gameID = Integer.parseInt(messageInfos[0]);
        int playerIndex = Integer.parseInt(messageInfos[1]);
        int playerState = Integer.parseInt(messageInfos[2]);
        
        GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
        Platform.runLater(() -> gbc.updateActivePlayer(playerIndex, playerState));
    }

    private void handleReceivedLudoNamePacket(String message) {
        int endGameIDIndex = message.indexOf(",");
        int gameID = Integer.parseInt(message.substring(0, endGameIDIndex));
        
        message = message.substring(endGameIDIndex + 1);
        
        int indIndex = message.indexOf(",");
        int index = Integer.parseInt(message.substring(0, indIndex));
        String name = message.substring(indIndex + 1);
        
        GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
        Platform.runLater(() -> gbc.updateName(name, index));
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
            default :
                break;
        }
    }

    private void handleLogoutResponse(String ackMessage) {
        Platform.runLater(
                () -> Client.ludoController.handleServerLogoutResponse(ackMessage));
        if (Integer.parseInt(ackMessage) == 1) {
            try {
                Client.socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private void handleReceivedChatPacket(String message) {

        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);

        switch (tag) {
            case "Update:" :
                break;
            case "Join:" :
                break;
            default :
                break;
        }
    }
}
