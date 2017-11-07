package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.logging.Logger;

import javax.swing.JFrame;

/**
 * 
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Server {
    private static final String url = "jdbc:mysql://mysql.stud.ntnu.no/mksandbe_Ludo";
    private static Connection connection;
    private DatagramSocket socket;
    private static final Logger LOGGER = Logger
            .getLogger(Server.class.getName());

    /**
     * Sets port number and opens the server GUI
     */
    public Server() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, "mksandbe",
                    "1475963");
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.warning(e.getMessage());
        }

        // TODO don't do this pleas
        JFrame frame = new JFrame("Servers gonna serve");
        frame.setSize(200, 971);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            socket = new DatagramSocket(9003);
        } catch (SocketException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Waits until a packet is received, then handles it
     */
    public void waitForPackets() {
        while (!socket.isClosed()) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(data,
                        data.length);

                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0,
                        receivePacket.getLength());
                System.out.println(message);
                handlePacket(receivePacket);

            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    private void sendPacketToClient(String message,
            DatagramPacket datagramPacket) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(),
                message.getBytes().length, datagramPacket.getAddress(),
                datagramPacket.getPort());

        System.out.println("Sender packet");
        socket.send(sendPacket);
    }

    private void handlePacket(DatagramPacket datagramPacket) {

        String message = new String(datagramPacket.getData(), 0,
                datagramPacket.getLength());
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = tag;

        switch (tag) {
            case "Login:" :
                ackMessage += handleLoginPacket(message, tagEndIndex);
                break;
            case "Register:" :
                ackMessage += handleRegisterPacket(message, tagEndIndex);
                break;
        }

        try {
            sendPacketToClient(ackMessage, datagramPacket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    private String handleRegisterPacket(String message, int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1, message.length());

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT Username, Password FROM Accounts WHERE Username='"
                            + username + "'");

            String ackMessage = " -1";
            String regexPattern = "^[a-zA-Z][a-zA-Z0-9]{0,15}$";
            if (!resultSet.next() && username.matches(regexPattern)
                    && password.matches(regexPattern)) {

                PreparedStatement pstmt = connection
                        .prepareStatement("INSERT INTO Accounts "
                                + "(Username, Password) " + "VALUES (?, ?)");

                pstmt.setString(1, username);
                pstmt.setString(2, password);

                pstmt.executeUpdate();
                ackMessage = " 1";

            }

            return ackMessage;

        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
        }

        return null;
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
    private String handleLoginPacket(String message, int tagEndIndex) {
        int splitIndex = message.indexOf(";");
        String username = message.substring(tagEndIndex, splitIndex);
        String password = message.substring(splitIndex + 1, message.length());

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT Username, Password FROM Accounts WHERE Username='"
                            + username + "' AND Password='" + password + "'");

            String ackMessage;
            if (resultSet.next()) {
                ackMessage = " 1";
            } else {
                ackMessage = " -1";
            }
            return ackMessage;

        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
        }

        return "Couldn't query database";
    }

    /**
     * Sets server to wait for packets
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        Server application = new Server();
        application.waitForPackets();
    }

}
