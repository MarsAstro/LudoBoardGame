/**
 * 
 */
package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
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
                LOGGER.log(Level.WARNING, e.getMessage(), e);
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
            case "Logout:" :
                ackMessage += handleLogoutPacket(datagramPacket.getAddress(),
                        datagramPacket.getPort(), message, tagEndIndex);
                break;
            default :
                break;
        }

        try {
            sendPacketToClient(ackMessage, datagramPacket);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private String handleLogoutPacket(InetAddress address, int port, String message,
            int tagEndIndex) {

        // ID is hardcoded because equals operator disregards clientID
        ClientInfo client = new ClientInfo(1, address, port);;
        String result = Server.connectedClients.remove(client) ? "1" : "-1";
        Platform.runLater(() -> Server.serverGUIController.updateUserList());
        return result;
    }

    private String handleRegisterPacket(InetAddress address, int port, String message,
            int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1);

        String ackMessage = "";
        try {
            PreparedStatement userQuery = Server.connection
                    .prepareStatement("SELECT Username FROM Accounts WHERE Username = ?");
            userQuery.setString(1, username);
            ResultSet resultSet = userQuery.executeQuery();

            String regexPattern = "^[a-zA-Z][a-zA-Z0-9]{0,15}$";

            if (resultSet.next()) {
                ackMessage = "-1";
            } else if (!username.matches(regexPattern)) {
                ackMessage = "-2";
            } else if (!password.matches(regexPattern)) {
                ackMessage = "-3";
            } else {
                PreparedStatement newUserInsert = Server.connection.prepareStatement(
                        "INSERT INTO Accounts (Username, Password) VALUES (?, ?)");
                newUserInsert.setString(1, username);
                newUserInsert.setString(2, password);
                newUserInsert.executeUpdate();

                PreparedStatement newUserQuery = Server.connection
                        .prepareStatement("SELECT UserID FROM Accounts WHERE Username = ?");
                newUserQuery.setString(1, username);
                ResultSet newUser = newUserQuery.executeQuery();

                newUser.next();
                Server.connectedClients
                        .add(new ClientInfo(newUser.getInt("UserID"), address, port));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());

                newUserInsert.close();
                newUser.close();
                newUserQuery.close();
                ackMessage = "1";
            }

            userQuery.close();
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
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

            PreparedStatement userQuery = Server.connection.prepareStatement(
                    "SELECT UserID, Username, Password FROM Accounts WHERE Username = ? AND Password = ?");
            userQuery.setString(1, username);
            userQuery.setString(2, password);
            ResultSet resultSet = userQuery.executeQuery();

            String ackMessage;

            if (resultSet.next()) {
                Server.connectedClients
                        .add(new ClientInfo(resultSet.getInt("UserID"), address, port));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
                ackMessage = "1";
            } else {
                ackMessage = "-1";
            }

            userQuery.close();
            resultSet.close();
            return ackMessage;

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
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
