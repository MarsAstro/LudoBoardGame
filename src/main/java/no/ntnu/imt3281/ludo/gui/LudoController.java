package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.TabPane.TabClosingPolicy;
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
    private ArrayList<GameBoardController> gameBoards;
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
        gameBoards = new ArrayList<>();
        tabbedPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
    }

    /**
     * Connects the user to a random game
     * 
     * @param event
     *            The event caused by the buttonpress
     */
    @FXML
    public void joinRandomGame(ActionEvent event) {
        Client.sendMessage("Ludo.JoinRandom:");
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
        Client.sendMessage("User.Logout:");
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
        }
    }

    /**
     * Handles received ackMessage when trying to join a game
     * 
     * @param ackMessage
     *            Message indicating if connection was a success
     */
    public void handleServerJoinGame(String ackMessage) {
        int endIndex = ackMessage.indexOf(",");

        int gameID = Integer.parseInt(ackMessage.substring(0, endIndex));
        int playerID = Integer.parseInt(ackMessage.substring(endIndex + 1));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
        loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));

        try {
            AnchorPane gameBoard = loader.load();
            GameBoardController newController = ((GameBoardController) loader.getController());
            Tab tab = new Tab("Game");
            tab.setContent(gameBoard);
            tab.setOnCloseRequest(e -> {
                newController.leaveGame();
                gameBoards.remove(newController);
            });
            tabbedPane.getTabs().add(tab);

            newController.gameID = gameID;
            newController.playerID = playerID;
            gameBoards.add(loader.getController());

            Client.sendMessage("Ludo.Init:" + gameID);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Disconnects from server and closes window
     * 
     * @param event
     *            Event triggering method
     */
    @FXML
    public void closeWindow(ActionEvent event) {
        if (Client.isConnected()) {
            logout(event);
        }
        Stage stage = (Stage) tabbedPane.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    /**
     * Gets game board controller from gameID
     * 
     * @param gameID
     *            The gameID referencing a possible game board controller
     * @return True if gameID reference a game board controller
     */
    public GameBoardController getGameBoardController(int gameID) {
        GameBoardController controller = null;
        for (GameBoardController gbc : gameBoards) {
            if (gameID == gbc.gameID) {
                controller = gbc;
                break;
            }
        }
        return controller;
    }
}
