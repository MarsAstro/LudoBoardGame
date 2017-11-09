/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * @author Marius
 *
 */
public class ServerNetworkTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * Run
     */
    @Override
    public void run() {
        while (!Server.socket.isClosed()) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                Server.socket.receive(receivePacket);
                handlePacket(receivePacket);

            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    private void handlePacket(DatagramPacket datagramPacket) {
        String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = tag;

        switch (tag) {
            case "Login:" :
                ackMessage += handleLoginPacket(datagramPacket.getAddress(),
                        datagramPacket.getPort(), message, tagEndIndex);
                break;
            case "Register:" :
                ackMessage += handleRegisterPacket(datagramPacket.getAddress(),
                        datagramPacket.getPort(), message, tagEndIndex);
                break;
        }

        try {
            sendPacketToClient(ackMessage, datagramPacket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }
    
    private String handleRegisterPacket(InetAddress address, int port, String message, int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1);

        String ackMessage = "";
        try {
            ResultSet resultSet = Server.connection.createStatement().executeQuery(
                    "SELECT Username FROM Accounts WHERE Username='" + username + "'");

            String regexPattern = "^[a-zA-Z][a-zA-Z0-9]{0,15}$";
            
            if (resultSet.next()) {
                ackMessage = "-1";
            } else if (!username.matches(regexPattern)) {
                ackMessage = "-2";
            } else if (!password.matches(regexPattern)) {
                ackMessage = "-3";
            } else {
                PreparedStatement pstmt = Server.connection.prepareStatement(
                        "INSERT INTO Accounts " + "(Username, Password) " + "VALUES (?, ?)");

                pstmt.setString(1, username);
                pstmt.setString(2, password);

                pstmt.executeUpdate();
                ackMessage = "1";
                
                ResultSet newUser = Server.connection.createStatement()
                        .executeQuery("SELECT UserID FROM Accounts WHERE Username='"
                                + username + "'");
                newUser.next();
                Server.connectedClients.add(new ClientInfo(newUser.getInt(1), address, port));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
            }
        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
        }

        return ackMessage;
    }

    /**
     * Handles specifically login related packets
     * 
     * @param message
     *            The received message
     * @param tagEndIndex
     *            Index of last char og tag in message
     * @return Acknowledge message
     */
    private String handleLoginPacket(InetAddress address, int port, String message,
            int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1, message.length());

        try {
            ResultSet resultSet = Server.connection.createStatement()
                    .executeQuery("SELECT UserID, Username, Password FROM Accounts WHERE Username='"
                            + username + "' AND Password='" + password + "'");

            String ackMessage;
            if (resultSet.next()) {
                ackMessage = "1";
                int id = resultSet.getInt(1);
                Server.connectedClients.add(new ClientInfo(id, address, port));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
            } else {
                ackMessage = "-1";
            }
            return ackMessage;

        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
        }

        return "Couldn't query database";
    }
    
    private void sendPacketToClient(String message, DatagramPacket datagramPacket)
            throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(),
                message.getBytes().length, datagramPacket.getAddress(), datagramPacket.getPort());

        Server.socket.send(sendPacket);
    }
}

