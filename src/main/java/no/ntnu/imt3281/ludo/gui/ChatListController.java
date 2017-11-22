/**
 * 
 */
package no.ntnu.imt3281.ludo.gui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * @author Charles The Gentle
 *
 */
public class ChatListController implements Initializable {

	private ObservableList<Label> chatNames = FXCollections.observableArrayList();
	private ResourceBundle messages;

	@FXML // fx:id="chatList"
	private ListView<Label> chatList;

	@FXML // fx:id="joinButton"
	private Button joinButton;

	@FXML // fx:id="createButton"
	private Button createButton;

	@FXML // fx:id="closeButton"
	private Button closeButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		messages = resources;
	}

	@FXML
	void closeChatList(ActionEvent event) {
		closeWindow();
	}

	@FXML
	void createChat(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(messages.getString("chatlist.prompttitle"));
		dialog.setHeaderText(messages.getString("chatlist.promptheader"));
		dialog.setContentText(messages.getString("chatlist.promptcontent"));

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			 Client.sendMessage("Chat.Create:" + result.get());
		}
	}

	@FXML
	void joinChat(ActionEvent event) {
		Label selectedChat = chatList.getSelectionModel().getSelectedItem();
		String chatID = selectedChat.getText();
		
		Client.sendMessage("Chat.Join:" + chatID);
	}

	/**
	 * Adds chat name to chat list
	 * 
	 * @param ackMessage
	 *            The name of the chat to be added
	 */
	public void addChatName(String name) {
		Label newChat = new Label(name);
		chatNames.add(newChat);
		chatList.setItems(chatNames);
	}
	
	/**
	 * Closes window
	 */
	public void closeWindow() {
		Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
	}
}