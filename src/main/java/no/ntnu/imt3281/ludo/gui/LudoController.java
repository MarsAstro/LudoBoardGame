package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Controls the main user GUI
 * 
 * @author Marius
 *
 */
public class LudoController implements Initializable {
    private ResourceBundle messages;
    private static final Logger LOGGER = Logger.getLogger(LudoController.class.getName());

    @FXML // fx:id="loginButton"
    private MenuItem loginButton;

    @FXML // fx:id="logoutButton"
    private MenuItem logoutButton;

    @FXML // fx:id="random"
    private MenuItem random;

    @FXML // fx:id="loggedInUser"
    private Menu loggedInUser;

    @FXML // fx:id="tabbedPane"
    private TabPane tabbedPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;
    }

    /**
     * Connects the user to a random game
     * 
     * @param event
     *            The event caused by the buttonpress
     */
    @FXML
    public void joinRandomGame(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));

        try {
            AnchorPane gameBoard = loader.load();
            Tab tab = new Tab("Game");
            tab.setContent(gameBoard);
            tabbedPane.getTabs().add(tab);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @FXML
    void openLoginRegisterGUI(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Connect.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));

        try {
            BorderPane root = (BorderPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setScene(scene);
            loginStage.show();

            ConnectController controller = loader.getController();
            Client.setConnectController(controller);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

    }

    @FXML
    void logout(ActionEvent event) {
        byte[] message = "Logout:".getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
        Client.sendPacket(datagramPacket);
    }

    void setLoggedInUser(String username) {
        logoutButton.setDisable(false);
        loginButton.setDisable(true);
        loggedInUser.setText(messages.getString("ludo.menubar.user.logintext") + " " + username);
    }

    /**
     * Handles the servers response to a logout request
     * 
     * @param ackMessage
     *            The message returned by server
     */
    public void handleServerLogoutResponse(String ackMessage) {
        if (Integer.parseInt(ackMessage) == 1) {
            logoutButton.setDisable(true);
            loginButton.setDisable(false);
            loggedInUser.setText(messages.getString("ludo.menubar.user.nouser"));
            Client.disconnectFromServer();
        }
    }
}
