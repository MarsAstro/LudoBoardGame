/**
 * Selfmade Skeleton for 'Login.fxml' Controller Class
 */

package no.ntnu.imt3281.ludo.gui;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Handles logging the user in and connecting to server
 * 
 * @author Marius
 *
 */
public class LoginController {
    private Client owner;
    private static final Logger LOGGER = Logger
            .getLogger(LoginController.class.getName());

    @FXML // fx:id="username"
    private TextField username; // Value injected by FXMLLoader

    @FXML // fx:id="password"
    private TextField password; // Value injected by FXMLLoader

    /**
     * Sets the LudoController's owning client
     * 
     * @param owner
     *            The client to own this controller
     */
    public void setOwner(Client owner) {
        this.owner = owner;
    }

    @FXML
    void cancel(ActionEvent event) {
        Stage stage = (Stage) username.getScene().getWindow();
        stage.close();
    }

    @FXML
    void login(ActionEvent event) {
        if (bothFieldsValid()) {
            byte[] message = ("Login:" + username.getText() + ";" + password.getText()).getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
            owner.sendPacket(datagramPacket);
        }
    }

    @FXML
    void register(ActionEvent event) {
        if (bothFieldsValid()) {
            byte[] message = ("Register:" + username.getText() + ";" + password.getText()).getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
            owner.sendPacket(datagramPacket);
        }
    }

    private boolean bothFieldsValid() {
        return !(username.getText().isEmpty() || password.getText().isEmpty());
    }

}
