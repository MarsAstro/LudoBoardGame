package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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

/**
 * Controls the main user GUI
 * 
 * @author Marius
 *
 */
public class LudoController {
    private Client owner;
    private static final Logger LOGGER = Logger
            .getLogger(LudoController.class.getName());

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

    /**
     * Connects the user to a random game
     * 
     * @param event
     *            The event caused by the buttonpress
     */
    @FXML
    public void joinRandomGame(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("GameBoard.fxml"));
        loader.setResources(
                ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        try {
            AnchorPane gameBoard = loader.load();
            Tab tab = new Tab("Game");
            tab.setContent(gameBoard);
            tabbedPane.getTabs().add(tab);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    @FXML
    void openLoginRegisterGUI(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Login.fxml"));
        loader.setResources(
                ResourceBundle.getBundle("no.ntnu.imt3281.I18N.i18n"));

        try {
            BorderPane root = (BorderPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setScene(scene);
            loginStage.show();

            LoginController controller = loader.getController();
            controller.setOwner(owner);
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }

    }
}
