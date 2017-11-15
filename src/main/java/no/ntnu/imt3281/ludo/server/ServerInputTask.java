/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.logic.Ludo;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;

/**
 * @author Marius
 *
 */
public class ServerInputTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            Server.lock.readLock().lock();
            for (ClientInfo client : Server.connections) {
                try {
                    if (client.connection.getInputStream().available() > 0) {
                        byte[] inputData = new byte[100];
                        client.connection.getInputStream().read(inputData);

                        handleMessage(client.clientID, inputData);
                    }
                } catch (IOException e) {
                    // TODO handle closed connection
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
            Server.lock.readLock().unlock();
        }
    }

    private void handleMessage(int clientID, byte[] inputData) {
        String message = new String(inputData, 0, inputData.length);
        message = message.substring(0, message.indexOf("\0"));
        int tagEndIndex = message.indexOf(".");
        String tag = message.substring(0, tagEndIndex + 1);
        switch (tag) {
            case "User." :
                UserTask.blockingPut(clientID + message.substring(tagEndIndex));
                break;
            case "Ludo." :
                LudoTask.blockingPut(clientID + message.substring(tagEndIndex));
                break;
            case "Chat." :
                ChatTask.blockingPut(clientID + message.substring(tagEndIndex));
                break;
            default :
                break;
        }
    }
}
