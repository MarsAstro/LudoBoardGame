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
        int tagEndIndex = message.indexOf(".") + 1;
        String tag = message.substring(0, tagEndIndex);
        switch (tag) {
            case "User." :
                handleUserPacket(datagramPacket, message.substring(tagEndIndex));
                break;
            case "Ludo." :
                handleLudoPacket(datagramPacket, message.substring(tagEndIndex));
                break;
            case "Chat." :
                // TODO
                break;
            default :
                break;
        }
    }

    private void handleUserPacket(DatagramPacket datagramPacket, String message) {
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);

        switch (tag) {
            case "Login:" :
                handleUserLoginPacket(datagramPacket, message, tagEndIndex);
                break;
            case "Register:" :
                handleUserRegisterPacket(datagramPacket, message, tagEndIndex);
                break;
            case "Logout:" :
                handleUserLogoutPacket(datagramPacket, message, tagEndIndex);
                break;
            default :
                break;
        }
    }

    private void handleLudoPacket(DatagramPacket datagramPacket, String message) {
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);

        switch (tag) {
            case "Throw:" :
                handleLudoThrowPacket(datagramPacket, message.substring(tagEndIndex), tagEndIndex);
                break;
            case "Move:" :
                handleLudoMovePacket(datagramPacket, message.substring(tagEndIndex), tagEndIndex);
                break;
            case "JoinRandom:" :
                handleLudoJoinRandomPacket(datagramPacket, message.substring(tagEndIndex),
                        tagEndIndex);
                break;
            case "Challenge:" :
                // TODO
                break;
            case "Init:" :
                handleLudoInitPacket(datagramPacket, message.substring(tagEndIndex));
                break;
            case "Leave:" :
                handleLudoLeavePacket(datagramPacket, message.substring(tagEndIndex), tagEndIndex);
                break;
            default :
                break;
        }
    }

    private void handleLudoInitPacket(DatagramPacket datagramPacket, String message) {
        int gameID = Integer.parseInt(message);

        for (GameInfo game : Server.games) {
            if (game.gameID == gameID) {
                for (ClientInfo clientInGame : game.clients) {
                    for (int i = 0; i < game.ludo.nrOfPlayers(); i++) {
                        sendPacketToClient(
                                "Ludo.Name:" + message + "," + i + "," + game.ludo.getPlayerName(i),
                                clientInGame);
                        if (game.ludo.activePlayers() > 1) {
                            sendPacketToClient("Ludo.Player:" + gameID + "," + Ludo.RED + ","
                                    + PlayerEvent.PLAYING, clientInGame);
                        }
                    }
                }
            }
        }
    }

    private void handleLudoJoinRandomPacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {
        String ackMessage = "Ludo.Join:";

        ClientInfo client = new ClientInfo(-1, datagramPacket.getAddress(),
                datagramPacket.getPort(), "RandomName");
        int index = Server.connectedClients.indexOf(client);
        client = Server.connectedClients.get(index);

        boolean foundGame = false;
        for (GameInfo game : Server.games) {
            if (game.addPlayer(client)) {
                foundGame = true;
                ackMessage += Integer.toString(game.gameID) + ","
                        + game.ludo.getIndexOfPlayer(client.username);
                break;
            }
        }

        if (!foundGame) {
            ackMessage += Integer.toString(Server.nextGameID) + "," + 0;
            Server.games.add(new GameInfo(Server.nextGameID++, client));
        }

        returnPacketToClient(ackMessage, datagramPacket);
    }

    private void handleLudoThrowPacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {
        String ackMessage = "";

        returnPacketToClient(ackMessage, datagramPacket);
    }

    private void handleLudoMovePacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {
        String ackMessage = "";
        // TODO
    }

    private void handleLudoLeavePacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {
        int gameID = Integer.parseInt(message);

        for (GameInfo game : Server.games) {
            if (game.gameID == gameID) {
                ClientInfo client = game.getClient(datagramPacket.getAddress(),
                        datagramPacket.getPort());
                if (client != null) {
                    game.removePlayer(client);
                    if (game.ludo.activePlayers() <= 0) {
                        Server.games.remove(game);
                    }
                }
                break;
            }
        }
    }

    private void handleUserLogoutPacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {

        // ID and username is hardcoded when searching for client because equals
        // operator only considers address and port.
        ClientInfo client = new ClientInfo(-1, datagramPacket.getAddress(),
                datagramPacket.getPort(), "RandomName");

        String ackMessage = "User.Logout:" + (Server.connectedClients.remove(client) ? "1" : "-1");
        Platform.runLater(() -> Server.serverGUIController.updateUserList());

        returnPacketToClient(ackMessage, datagramPacket);
    }

    private void handleUserRegisterPacket(DatagramPacket datagramPacket, String message,
            int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1);

        String ackMessage = "User.Register:";
        try {
            PreparedStatement userQuery = Server.connection
                    .prepareStatement("SELECT Username FROM Accounts WHERE Username = ?");
            userQuery.setString(1, username);
            ResultSet resultSet = userQuery.executeQuery();

            String regexPattern = "^[a-zA-Z][a-zA-Z0-9]{0,15}$";

            if (resultSet.next()) {
                ackMessage += "-1";
            } else if (!username.matches(regexPattern)) {
                ackMessage += "-2";
            } else if (!password.matches(regexPattern)) {
                ackMessage += "-3";
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
                Server.connectedClients.add(new ClientInfo(newUser.getInt("UserID"),
                        datagramPacket.getAddress(), datagramPacket.getPort(), username));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());

                newUserInsert.close();
                newUser.close();
                newUserQuery.close();
                ackMessage += "1";
            }

            userQuery.close();
            resultSet.close();

            returnPacketToClient(ackMessage, datagramPacket);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Handles specifically login related packets
     * 
     * @param message
     *            The received message
     * @param tagEndIndex
     *            Index of last char and tag in message
     * @return Acknowledge message
     */
    private void handleUserLoginPacket(DatagramPacket datagramPacket, String message,
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

            String ackMessage = "User.Login:";
            boolean alreadyLoggedIn = false;
            for (ClientInfo client : Server.connectedClients) {
                if (client.userEquals(username)) {
                    alreadyLoggedIn = true;
                    break;
                }
            }

            if (alreadyLoggedIn) {
                ackMessage += "-2";
            } else if (resultSet.next()) {
                Server.connectedClients.add(new ClientInfo(resultSet.getInt("UserID"),
                        datagramPacket.getAddress(), datagramPacket.getPort(), username));
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
                ackMessage += "1";
            } else {
                ackMessage += "-1";
            }

            userQuery.close();
            resultSet.close();

            returnPacketToClient(ackMessage, datagramPacket);

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void returnPacketToClient(String message, DatagramPacket datagramPacket) {
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(),
                message.getBytes().length, datagramPacket.getAddress(), datagramPacket.getPort());
        try {
            Server.socket.send(sendPacket);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void sendPacketToClient(String message, ClientInfo client) {

        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(),
                message.getBytes().length, client.address, client.port);

        try {
            Server.socket.send(sendPacket);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
