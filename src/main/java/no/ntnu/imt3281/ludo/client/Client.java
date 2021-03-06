package no.ntnu.imt3281.ludo.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
 * This is the main class for the client.
 * 
 * @author oyste
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
        File detailsDir = new File("details");
        if (!detailsDir.exists() && !detailsDir.mkdir()) {
            LOGGER.log(Level.WARNING, "Details directory was not created");
        }
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

            ExecutorService executorService = Executors.newCachedThreadPool();

            ClientInputTask networkTask = new ClientInputTask();
            ClientLudoTask ludoTask = new ClientLudoTask();
            ClientUserTask userTask = new ClientUserTask();
            ClientChatTask chatTask = new ClientChatTask();

            executorService.execute(networkTask);
            executorService.execute(ludoTask);
            executorService.execute(userTask);
            executorService.execute(chatTask);

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
            message += ";kk;";
            socket.getOutputStream().write(message.getBytes("UTF-8"));
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
