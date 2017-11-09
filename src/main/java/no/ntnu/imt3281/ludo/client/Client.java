package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javafx.application.Application;
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
    public static LudoController ludoController;
    public static ConnectController connectController;

    protected static DatagramSocket socket;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * @see javafx.application.Application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
            loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));
            AnchorPane root = (AnchorPane) loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest(e -> System.exit(0));

            ludoController = loader.getController();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Launches the JavaFX GUI and makes client wait for packets
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        connectToServer(9003);

        ClientNetworkTask networkTask = new ClientNetworkTask();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(networkTask);
        executorService.shutdown();
        
        launch(args);
    }

    /**
     * Attempts to connect the client to the given server
     * 
     * @param port
     *            The port to communicate through
     */
    public static void connectToServer(int port) {
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getLocalHost(), port);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Generic function that sends packet to server
     * 
     * @param datagramPacket
     *            The data which should be sent
     */
    public static void sendPacket(DatagramPacket datagramPacket) {
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
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
}
