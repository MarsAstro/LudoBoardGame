package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
    private DatagramSocket socket;

    /**
     * @see javafx.application.Application
     */
    @Override
    public void start(Stage primaryStage) {
	try {
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/Ludo.fxml"));
	    AnchorPane root = (AnchorPane) loader.load();

	    Scene scene = new Scene(root);
	    primaryStage.setScene(scene);
	    primaryStage.show();
	    primaryStage.setOnCloseRequest(e -> System.exit(0));

	    LudoController controller = loader.getController();
	    controller.setOwner(this);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Launches the JavaFX GUI and makes client wait for packets
     * 
     * @param args
     */
    public static void main(String[] args) {
	launch(args);
	Client application = new Client();
	application.waitForPackets();
    }

    /**
     * Constructor
     */
    public Client() {
	// TODO maybe?
    }

    /**
     * Attempts to connect the client to the given server
     * 
     * @param address
     *            The IP address to connect to
     * @param port
     *            The port to communicate through
     */
    public void connectToServer(InetAddress address, int port) {
	try {
	    socket = new DatagramSocket();
	    socket.connect(address, port);
	    byte[] message = new String("conplz").getBytes();
	    socket.send(new DatagramPacket(message, message.length));
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Waits until a packet is received, then handles it
     */
    public void waitForPackets() {
	while (true) {
	    try {
		byte[] data = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		socket.receive(receivePacket);
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
    }
}
