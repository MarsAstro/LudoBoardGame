package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.JFrame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.ClientNetworkTask;

/**
 * 
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Server extends Application {
    
    protected static DatagramSocket socket;
    protected static Connection connection;
    protected static ArrayList<ClientInfo> connectedClients;
    protected static ServerGUIController serverGUIController;

    private static final String url = "jdbc:mysql://mysql.stud.ntnu.no/mksandbe_Ludo";
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    /**
     * Sets port number and opens the server GUI
     */
    public static void initServer() {
        connectedClients = new ArrayList<>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, "mksandbe", "1475963");
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.warning(e.getMessage());
        }

        try {
            socket = new DatagramSocket(9003);
        } catch (SocketException e) {
            System.err.println("Socket is fjack");
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * @see javafx.application.Application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./ServerGUI.fxml"));
            GridPane root = (GridPane) loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(e -> System.exit(0));

            serverGUIController = loader.getController();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Sets server to wait for packets
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        initServer();
        
        ServerNetworkTask networkTask = new ServerNetworkTask();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(networkTask);
        executorService.shutdown();

        launch(args);
    }
}
