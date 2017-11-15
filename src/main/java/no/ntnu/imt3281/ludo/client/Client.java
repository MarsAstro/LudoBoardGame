package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.gui.ConnectController;
import no.ntnu.imt3281.ludo.gui.LudoController;

/**
 * 
 * This is the main class for the client. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Client extends Application {
    static LudoController ludoController;
    static ConnectController connectController;

    static Socket socket;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * @see javafx.application.Application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
            loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));
            AnchorPane root = (AnchorPane) loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage
                    .setOnCloseRequest(e -> Client.ludoController.closeWindow(new ActionEvent()));

            Client.ludoController = loader.getController();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Launches the JavaFX GUI and makes client wait for packets
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Attempts to connect the client to the given server
     * 
     * @param address
     *            Address to connect to server
     */
    public static void connectToServer(InetAddress address) {
        try {
            socket = new Socket(address, 9003);

            ClientInputTask networkTask = new ClientInputTask();
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(networkTask);
            executorService.shutdown();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Generic function that sends packet to server
     * 
     * @param message
     *            The text that should be sent
     */
    public static void sendMessage(String message) {
        try {
            socket.getOutputStream().write(message.getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * @return The client's current ludo controller
     */
    public static LudoController getLudoController() {
        return ludoController;
    }

    /**
     * @return The client's current connect controller
     */
    public static ConnectController getConnectController() {
        return connectController;
    }

    /**
     * Sets the reference to connect controller (called by ludo controller on
     * connect menu creation)
     * 
     * @param connectController
     *            The new connect controller
     */
    public static void setConnectController(ConnectController connectController) {
        Client.connectController = connectController;
    }

    /**
     * Checks if client is connected to a server
     * 
     * @return if socket is connected
     */
    public static boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
