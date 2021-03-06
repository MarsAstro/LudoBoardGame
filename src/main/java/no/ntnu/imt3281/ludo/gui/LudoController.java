package no.ntnu.imt3281.ludo.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Controls the main user GUI
 * 
 * @author Marius
 *
 */
public class LudoController implements Initializable {
    private static final String I18NPATH = "no.ntnu.imt3281.i18n.i18n";
    private static final Logger LOGGER = Logger.getLogger(LudoController.class.getName());

    private ResourceBundle messages;
    private ArrayList<GameBoardController> gameBoards;
    private ArrayList<ChatWindowController> chatWindows;
    private ChatListController chatList = null;
    private ChallengeListController challengeList = null;
    private ChallengeController challengeController = null;
    private Alert challengeAlert = null;
    private Tab mainTab;
    private String username;

    @FXML // fx:id="spinner"
    private ProgressIndicator spinner;

    @FXML // fx:id="loginButton"
    private MenuItem loginButton;

    @FXML // fx:id="logoutButton"
    private MenuItem logoutButton;

    @FXML // fx:id="random"
    private MenuItem random;

    @FXML // fx:id="challenge"
    private MenuItem challenge;

    @FXML // fx:id="chat"
    private MenuItem chat;

    @FXML // fx:id="loggedInUser"
    private Menu loggedInUser;

    @FXML // fx:id="loggedInUser"
    private Menu winsNum;

    @FXML // fx:id="loggedInUser"
    private Menu winsText;

    @FXML // fx:id="tabbedPane"
    private TabPane tabbedPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;
        gameBoards = new ArrayList<>();
        chatWindows = new ArrayList<>();
        tabbedPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        openLoginRegisterGUI(new ActionEvent());
    }

    /**
     * Connects the user to a random game
     * 
     * @param event
     *            The event caused by the button press
     */
    @FXML
    public void joinRandomGame(ActionEvent event) {
        Client.sendMessage("Ludo.JoinRandom:");
    }

    @FXML
    void openLoginRegisterGUI(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Connect.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            BorderPane root = (BorderPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setAlwaysOnTop(true);
            loginStage.setScene(scene);
            loginStage.show();

            ConnectController controller = loader.getController();
            Client.setConnectController(controller);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @FXML
    void openChatList(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatList.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            GridPane root = (GridPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setScene(scene);
            loginStage.show();
            loginStage.setOnCloseRequest(e -> chatList = null);
            chatList = loader.getController();
            Client.sendMessage("Chat.List:");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @FXML
    void openChallengeList(ActionEvent event) {
        challenge.setDisable(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChallengeList.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            GridPane root = (GridPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setScene(scene);
            loginStage.show();
            loginStage.setOnCloseRequest(e -> challengeList = null);
            challengeList = loader.getController();
            Client.sendMessage("User.List:");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Opens the challenge window
     */
    public void openChallenge() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Challenge.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            GridPane root = (GridPane) loader.load();
            Scene scene = new Scene(root);
            Stage loginStage = new Stage();

            loginStage.setScene(scene);
            loginStage.show();
            loginStage.setOnCloseRequest(e -> challengeController = null);
            challengeController = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @FXML
    void logout(ActionEvent event) {
        Client.sendMessage("User.Logout:");
    }

    void userLoggedIn(String username) {
        this.username = username;
        winsText.setVisible(true);
        winsNum.setVisible(true);
        random.setDisable(false);
        chat.setDisable(false);
        challenge.setDisable(false);
        logoutButton.setDisable(false);
        loginButton.setDisable(true);
        loggedInUser.setText(messages.getString("ludo.menubar.user.logintext") + " " + username);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            GridPane mainChat = loader.load();
            mainTab = new Tab(messages.getString("chat.global"));
            mainTab.setContent(mainChat);
            mainTab.setClosable(false);
            tabbedPane.getTabs().add(mainTab);

            ChatWindowController newController = (ChatWindowController) loader.getController();
            chatWindows.add(newController);
            Client.sendMessage("Chat.InitGlobal:" + 0);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Handles the servers response to a logout request
     * 
     * @param ackMessage
     *            The message returned by server
     */
    public void handleServerLogoutResponse(String ackMessage) {
        if (Integer.parseInt(ackMessage) == 1) {
            winsText.setVisible(false);
            winsNum.setText("0");
            winsNum.setVisible(false);
            random.setDisable(true);
            chat.setDisable(true);
            challenge.setDisable(true);
            logoutButton.setDisable(true);
            loginButton.setDisable(false);
            loggedInUser.setText(messages.getString("ludo.menubar.user.nouser"));
            tabbedPane.getTabs().removeAll(tabbedPane.getTabs());
            chatWindows.clear();
            gameBoards.clear();
            challengeList = null;
            chatList = null;
            challengeController = null;
            challengeAlert = null;
        }
    }

    /**
     * Handles received ackMessage when trying to join a game
     * 
     * @param ackMessage
     *            Message indicating if connection was a success
     */
    public void handleServerJoinRandomGame(String ackMessage) {
        spinner.setVisible(false);
        random.setDisable(false);
        int gameID = handleServerJoinGame(ackMessage);
        Client.sendMessage("Ludo.Init:" + gameID);
    }

    /**
     * Handles received ackMessage when trying to join a game
     * 
     * @param ackMessage
     *            Message indicating if connection was a success
     */
    public void handleServerJoinChallengeGame(String ackMessage) {
        int gameID = handleServerJoinGame(ackMessage);
        Client.sendMessage("Ludo.Init:" + gameID);
    }

    /**
     * Handles received ackMessage when trying to join a game
     * 
     * @param ackMessage
     *            Message indicating if connection was a success
     * @return id of the game joined
     */
    public int handleServerJoinGame(String ackMessage) {
        int endIndex = ackMessage.indexOf(",");

        int gameID = Integer.parseInt(ackMessage.substring(0, endIndex));
        int playerID = Integer.parseInt(ackMessage.substring(endIndex + 1));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

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
            tabbedPane.getSelectionModel().select(tab);

            newController.gameID = gameID;
            newController.playerID = playerID;
            gameBoards.add(loader.getController());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return gameID;
    }

    /**
     * Handles received ackMessage when trying to join chat
     * 
     * @param chatID
     *            The chat id of the chat to join
     * @param ackMessage
     *            The received acknowledge message from server
     */
    public void handleServerJoinChat(int chatID, String ackMessage) {
        handleServerInitChat(ackMessage);
        Client.sendMessage("Chat.Init:" + chatID);
    }

    /**
     * Initializes the local chat window after acknowledge message from server
     * 
     * @param ackMessage
     *            The received acknowledge message from server
     */
    public void handleServerInitChat(String ackMessage) {
        String[] ackMessages = ackMessage.split(",");
        int chatID = Integer.parseInt(ackMessages[0]);
        String chatName = ackMessages[1];

        if (chatList != null) {
            chatList.closeWindow();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
        loader.setResources(ResourceBundle.getBundle(I18NPATH));

        try {
            GridPane chatWindow = loader.load();
            ChatWindowController newController = ((ChatWindowController) loader.getController());
            Tab tab = new Tab(chatName);
            tab.setContent(chatWindow);
            tab.setOnCloseRequest(e -> {
                newController.leaveChat();
                chatWindows.remove(newController);
            });
            tabbedPane.getTabs().add(tab);

            newController.chatID = chatID;
            newController.addChatName(username);
            chatWindows.add(loader.getController());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * Handles a challenge game request
     * 
     * @param ackMessage
     *            The acknowledgement from server
     */
    public void handleServerChallengeGame(String ackMessage) {
        challenge.setDisable(true);
        ButtonType acceptButton = new ButtonType(messages.getString("challenge.accept"),
                ButtonBar.ButtonData.OK_DONE);
        ButtonType declineButton = new ButtonType(messages.getString("challenge.decline"),
                ButtonBar.ButtonData.CANCEL_CLOSE);

        challengeAlert = new Alert(AlertType.CONFIRMATION, "", acceptButton, declineButton);
        challengeAlert.setTitle(messages.getString("challenge.title"));
        challengeAlert.setHeaderText(ackMessage + messages.getString("challenge.header"));
        challengeAlert.setContentText(messages.getString("challenge.content"));

        Optional<ButtonType> result = challengeAlert.showAndWait();
        if (result.get() == acceptButton) {
            Client.sendMessage("Ludo.ChallengeConfirm:" + true);
        } else {
            Client.sendMessage("Ludo.ChallengeConfirm:" + false);
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
        Platform.exit();
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

    /**
     * Gets chat window controller from chatID
     * 
     * @param chatID
     *            The chatID referencing a possible chat window controller
     * @return A chat window controller if chatID reference a chat window
     *         controller
     */
    public ChatWindowController getChatWindowController(int chatID) {
        ChatWindowController controller = null;
        for (ChatWindowController cwc : chatWindows) {
            if (chatID == cwc.chatID) {
                controller = cwc;
                break;
            }
        }
        return controller;
    }

    /**
     * Display acknowledgment that client is in queue for random game
     */
    public void joinRandomSuccess() {
        spinner.setVisible(true);
        random.setDisable(true);
    }

    /**
     * @return The chat list controller
     */
    public ChatListController getChatListContoller() {
        return chatList;
    }

    /**
     * @return The challenge list controller
     */
    public ChallengeListController getChallengeListContoller() {
        return challengeList;
    }

    /**
     * @return The challenge controller
     */
    public ChallengeController getChallengeContoller() {
        return challengeController;
    }

    /**
     * @return The challengeAlert dialog box
     */
    public Alert getChallengeAlert() {
        return challengeAlert;
    }

    /**
     * Adds a number of wins to the clients wins display
     * 
     * @param numToAdd
     *            The number of wins to add
     */
    public void addWins(int numToAdd) {
        winsNum.setText(Integer.toString((Integer.parseInt(winsNum.getText()) + numToAdd)));
    }

    /**
     * Sets challengeAlert to null
     */
    public void resetChallengeAlert() {
        challengeAlert = null;
        challenge.setDisable(false);
    }

    /**
     * Sets challenge controller to null
     */
    public void resetChallengeController() {
        challengeController = null;
        challenge.setDisable(false);
    }
}
