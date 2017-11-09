/**
 * 
 */
package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

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

        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);

        switch (tag) {
            case "Login:" :
                Platform.runLater(() -> Client.connectController
                        .handleServerLoginResponse(message.substring(tagEndIndex)));
                break;
            case "Register:" :
                Platform.runLater(() -> Client.connectController
                        .handleServerRegisterResponse(message.substring(tagEndIndex)));
                break;
            default :
                break;
        }
    }
}
