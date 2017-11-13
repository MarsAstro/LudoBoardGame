/**
 * Selfmade Skeleton for 'Login.fxml' Controller Class
 */

package no.ntnu.imt3281.ludo.gui;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.client.ClientNetworkTask;

/**
 * Handles logging the user in and connecting to server
 * 
 * @author Marius
 *
 */
public class ConnectController implements Initializable {
    private ResourceBundle messages;
    private static final Logger LOGGER = Logger.getLogger(ConnectController.class.getName());

    @FXML // fx:id="IPAddress"
    private TextField IPAddress;

    @FXML // fx:id="username"
    private TextField username;

    @FXML // fx:id="password"
    private PasswordField password;

    @FXML // fx:id="errorMessage"
    private Label errorMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;
    }

    @FXML
    void cancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void login(ActionEvent event) {
        if (allFieldsValid()) {
            try {
                Client.connectToServer(InetAddress.getByName(IPAddress.getText()));
                
                byte[] message = ("Login:" + username.getText() + ";" + password.getText()).getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                Client.sendPacket(datagramPacket);
            } catch (UnknownHostException e) {
                errorMessage.setText("Failed to connect to host");
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
            
        }
    }

    @FXML
    void register(ActionEvent event) {
        if (allFieldsValid()) {
            byte[] message = ("Register:" + username.getText() + ";" + password.getText())
                    .getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
            Client.sendPacket(datagramPacket);
        }
    }

    private boolean allFieldsValid() {
        return !(IPAddress.getText().isEmpty() || username.getText().isEmpty()
                || password.getText().isEmpty());
    }

    /**
     * Handles the servers response to a login attempt
     * 
     * @param ackMessage
     *            The servers response
     */
    public void handleServerLoginResponse(String ackMessage) {
        if (Integer.parseInt(ackMessage) == -1) {
            errorMessage.setText(messages.getString("login.result.failed"));
        } else {
            Platform.runLater(() -> Client.getLudoController().setLoggedInUser(username.getText()));
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) username.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the servers response to a register attempt
     * 
     * @param ackMessage
     *            The servers response
     */
    public void handleServerRegisterResponse(String ackMessage) {
        switch (Integer.parseInt(ackMessage)) {
            case 1 :
                Platform.runLater(
                        () -> Client.getLudoController().setLoggedInUser(username.getText()));
                closeWindow();
                break;
            case -1 :
                errorMessage.setText(messages.getString("login.result.already"));
                break;
            case -2 :
                errorMessage.setText(messages.getString("login.result.usersyntax") + " "
                        + messages.getString("login.result.validsyntax"));
                break;
            case -3 :
                errorMessage.setText(messages.getString("login.result.pwdsyntax") + " "
                        + messages.getString("login.result.validsyntax"));
                break;
            default :
                break;
        }
    }
}
