/**
 * 
 */
package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

/**
 * @author Charles The Gentle
 *
 */
public class ChatListController {

	ObservableList<Label> chatNames = FXCollections.observableArrayList();

	@FXML // fx:id="chatList"
	private ListView<Label> chatList;

	@FXML // fx:id="joinButton"
	private Button joinButton;

	@FXML // fx:id="createButton"
	private Button createButton;

	@FXML // fx:id="closeButton"
	private Button closeButton;

	@FXML
	void closeChatList(ActionEvent event) {

	}

	@FXML
	void createChat(ActionEvent event) {

	}

	@FXML
	void joinChat(ActionEvent event) {

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
}