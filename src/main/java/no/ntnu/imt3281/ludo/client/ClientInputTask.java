/**
 * 
 */
package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles raw packets from server and delegates tasks
 * 
 * @author Marius
 *
 */
public class ClientInputTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputTask.class.getName());
    private byte[] inputData = new byte[256];

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Client.socket.isClosed()) {
            try {
                int length = Client.socket.getInputStream().read(inputData);

                if (length != -1) {
                    handleRawInputBytes(length);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, e.getMessage(), e);
            }
        }
    }

    private void handleRawInputBytes(int length) throws UnsupportedEncodingException {
        String packet = new String(inputData, 0, length, "UTF-8");
        String[] messages = packet.split(";kk;");
        for (String message : messages) {
            handleReceivedPacket(message);
        }
    }

    /**
     * Handles received packet
     * 
     * @param inputData
     *            The received packet
     */
    private void handleReceivedPacket(String message) {
        int tagEndIndex = message.indexOf('.') + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);
        switch (tag) {
            case "User." :
                ClientUserTask.addNewTask(ackMessage);
                break;
            case "Ludo." :
                ClientLudoTask.addNewTask(ackMessage);
                break;
            case "Chat." :
                ClientChatTask.addNewTask(ackMessage);
                break;
            default :
                // Packet no proper tag
                break;
        }
    }
}
