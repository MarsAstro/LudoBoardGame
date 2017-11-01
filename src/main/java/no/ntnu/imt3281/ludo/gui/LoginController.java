/**
 * Selfmade Skeleton for 'Login.fxml' Controller Class
 */

package no.ntnu.imt3281.ludo.gui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

public class LoginController {
    private Client owner;

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
	    try {
		owner.connectToServer(InetAddress.getLocalHost(), 9003);
	    } catch (UnknownHostException e) {
		e.printStackTrace();
	    }
	}
    }

    @FXML
    void register(ActionEvent event) {

    }

    private boolean bothFieldsValid() {
	boolean success = true;

	if (username.getText().isEmpty() || password.getText().isEmpty()) {
	    success = false;
	}

	return success;
    }

}
