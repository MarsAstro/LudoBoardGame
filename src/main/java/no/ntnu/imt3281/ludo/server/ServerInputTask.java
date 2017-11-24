/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.ntnu.imt3281.ludo.client.Client;

/**
 * Loops through clients and delegates tasks
 * 
 * @author Marius
 *
 */
public class ServerInputTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private byte[] inputData = new byte[256];

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            Server.clientLock.readLock().lock();
            for (ClientInfo client : Server.clients) {
                try {
                    handleClient(client);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
            Server.clientLock.readLock().unlock();
        }
    }

    private void handleClient(ClientInfo client) throws IOException {
        if (client.connection.getInputStream().available() > 0) {

            int length = client.connection.getInputStream().read(inputData);

            String packet = new String(inputData, 0, length, "UTF-8");
            String[] messages = packet.split(";kk;");
            for (String message : messages) {
                handleMessage(client.clientID, message);
            }
        } else if (client.connection.isClosed()) {
            UserCleanupTask.removeUser(client.clientID);
        }
    }

    private void handleMessage(int clientID, String message) {
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
