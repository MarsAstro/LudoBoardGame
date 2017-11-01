package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

public class LudoController {
    private Client owner;

    @FXML
    private MenuItem random;

    @FXML
    private TabPane tabbedPane;

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
    public void joinRandomGame(ActionEvent e) {
	FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
	loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

	GameBoardController controller = loader.getController();
	// Use controller to set up communication for this game.
	// Note, a new game tab would be created due to some communication from the
	// server
	// This is here purely to illustrate how a layout is loaded and added to a tab
	// pane.

	try {
	    AnchorPane gameBoard = loader.load();
	    Tab tab = new Tab("Game");
	    tab.setContent(gameBoard);
	    tabbedPane.getTabs().add(tab);
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    @FXML
    void openLoginRegisterGUI(ActionEvent event) {
	FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
	loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

	try {
	    BorderPane root = (BorderPane) loader.load();
	    Scene scene = new Scene(root);
	    Stage loginStage = new Stage();

	    loginStage.setScene(scene);
	    loginStage.show();

	    LoginController controller = loader.getController();
	    controller.setOwner(owner);
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

    }
}
