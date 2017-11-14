/**
 * 
 */
package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.GameBoardController;

/**
 * @author Marius
 *
 */
public class ClientNetworkTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Client.socket.isClosed()) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(data, data.length);

                Client.socket.receive(receivePacket);

                handleReceivedPacket(receivePacket);

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Handles received packet
     * 
     * @param receivePacket
     *            The received packet
     */
    private void handleReceivedPacket(DatagramPacket receivePacket) {
        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

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

    private void handleReceivedLudoPlayer(String ackMessage) {
        int endIndex = ackMessage.indexOf(",");
        int gameID = Integer.parseInt(ackMessage.substring(0, endIndex));
        
        ackMessage = ackMessage.substring(endIndex + 1);
        
        int indIndex = ackMessage.indexOf(",");
        int playerID = Integer.parseInt(ackMessage.substring(0, indIndex));
        int playerState = Integer.parseInt(ackMessage.substring(indIndex + 1));
        
        GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
        Platform.runLater(() -> gbc.updateActivePlayer(playerID, playerState));
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
                Platform.runLater(
                        () -> Client.ludoController.handleServerLogoutResponse(ackMessage));
                if (Integer.parseInt(ackMessage) == 1) {
                    Client.socket.close();
                }
                break;
            default :
                break;
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
