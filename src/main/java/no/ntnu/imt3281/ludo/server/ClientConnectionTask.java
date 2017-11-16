package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;

/**
 * Handles incoming connection requests from clients
 * 
 * @author Marius
 *
 */
public class ClientConnectionTask implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientConnectionTask.class.getName());
    private byte[] inputData = new byte[256];
    private String charset = "UTF-8";
    
    /**
     * Runs
     */
    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            try {
                Socket newClientSocket = Server.serverSocket.accept();
                int length = newClientSocket.getInputStream().read(inputData);
                newClientSocket.setSoTimeout(10000);

                String message = new String(inputData, 0, length, charset);
                message = message.substring(0, message.indexOf(";"));

                int tagEndIndex = message.indexOf(":") + 1;
                String tag = message.substring(0, tagEndIndex);
                
                if ("User.Login:".equals(tag)) {
                    handleUserLogin(newClientSocket, message.substring(tagEndIndex));
                } else if ("User.Register:".equals(tag)) {
                    handleUserRegister(newClientSocket, message.substring(tagEndIndex));
                } else {
                    newClientSocket.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private void handleUserRegister(Socket newClientSocket, String content) {
        int splitIndex = content.indexOf(",");
        String username = content.substring(0, splitIndex);
        String password = content.substring(splitIndex + 1);

        String ackMessage = "User.Register:";
        try {
            PreparedStatement userQuery = Server.database
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
                PreparedStatement newUserInsert = Server.database.prepareStatement(
                        "INSERT INTO Accounts (Username, Password) VALUES (?, ?)");
                newUserInsert.setString(1, username);
                newUserInsert.setString(2, password);
                newUserInsert.executeUpdate();

                PreparedStatement newUserQuery = Server.database
                        .prepareStatement("SELECT UserID FROM Accounts WHERE Username = ?");
                newUserQuery.setString(1, username);
                ResultSet newUser = newUserQuery.executeQuery();

                newUser.next();
                Server.lock.writeLock().lock();
                Server.connections
                        .add(new ClientInfo(newClientSocket, newUser.getInt("UserID"), username));
                Server.lock.writeLock().unlock();
                Platform.runLater(() -> Server.serverGUIController.updateUserList());

                newUserInsert.close();
                newUser.close();
                newUserQuery.close();
                ackMessage += "1";
            }

            userQuery.close();
            resultSet.close();

            ackMessage += ";";
            newClientSocket.getOutputStream().write(ackMessage.getBytes(charset));
            newClientSocket.getOutputStream().flush();
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void handleUserLogin(Socket newClientSocket, String content) {
        int splitIndex = content.indexOf(",");
        String username = content.substring(0, splitIndex);
        String password = content.substring(splitIndex + 1);

        try {

            PreparedStatement userQuery = Server.database.prepareStatement(
                    "SELECT UserID, Username, Password FROM Accounts WHERE Username = ? AND Password = ?");
            userQuery.setString(1, username);
            userQuery.setString(2, password);
            ResultSet resultSet = userQuery.executeQuery();

            String ackMessage = "User.Login:";
            boolean alreadyLoggedIn = false;
            for (ClientInfo client : Server.connections) {
                if (client.userEquals(username)) {
                    alreadyLoggedIn = true;
                    break;
                }
            }

            if (alreadyLoggedIn) {
                ackMessage += "-2";
            } else if (resultSet.next()) {
                Server.lock.writeLock().lock();
                Server.connections
                        .add(new ClientInfo(newClientSocket, resultSet.getInt("UserID"), username));
                Server.lock.writeLock().unlock();
                Platform.runLater(() -> Server.serverGUIController.updateUserList());
                ackMessage += "1";
            } else {
                ackMessage += "-1";
            }

            userQuery.close();
            resultSet.close();

            ackMessage += ";";
            newClientSocket.getOutputStream().write(ackMessage.getBytes(charset));
            newClientSocket.getOutputStream().flush();
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

}
