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
	private ResourceBundle messages;
	private ArrayList<GameBoardController> gameBoards;
	private ArrayList<ChatWindowController> chatWindows;
	private Tab mainTab;
	private static final Logger LOGGER = Logger.getLogger(LudoController.class.getName());

	@FXML // fx:id="spinner"
	private ProgressIndicator spinner;
	
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
		chatWindows = new ArrayList<>();
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

	void userLoggedIn(String username) {
		logoutButton.setDisable(false);
		loginButton.setDisable(true);
		loggedInUser.setText(messages.getString("ludo.menubar.user.logintext") + " " + username);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));

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
            logoutButton.setDisable(true);
            loginButton.setDisable(false);
            loggedInUser.setText(messages.getString("ludo.menubar.user.nouser"));
            tabbedPane.getTabs().remove(mainTab);
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
	 * @return id of the game joined
	 */
	public int handleServerJoinGame(String ackMessage) {
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
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		return gameID;
	}
	
	public void handleServerJoinChat(String ackMessage) {
		int chatID = Integer.parseInt(ackMessage);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
		loader.setResources(ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n"));

		try {
			GridPane gameBoard = loader.load();
			ChatWindowController newController = ((ChatWindowController) loader.getController());
			Tab tab = new Tab("Chat" + chatID);
			tab.setContent(gameBoard);
			tab.setOnCloseRequest(e -> {
				// TODO
				/*
				newController.leaveGame();
				gameBoards.remove(newController);
				*/
			});
			tabbedPane.getTabs().add(tab);

			newController.chatID = chatID;
			chatWindows.add(loader.getController());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		
		Client.sendMessage("Chat.Init:" + chatID);
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

	/**
	 * Gets chat window controller from chatID
	 * 
	 * @param chatID
	 *            The chatID referencing a possible chat window controller
	 * @return A chat window controller if chatID reference a chat window controller
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
    public void JoinRandomSuccess() {
        spinner.setVisible(true);
        random.setDisable(true);
    }
}
