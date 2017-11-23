package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Handles chat windows
 * 
 * @author Marius
 *
 */
public class ChatWindowController {
    int chatID;
    private ObservableList<Label> usernames = FXCollections.observableArrayList();
    private ObservableList<Label> messages = FXCollections.observableArrayList();

    @FXML // fx:id="chatBox"
    private ScrollPane chatBox;

    @FXML // fx:id="chatList"
    private ListView<Label> chatList;

    @FXML // fx:id="userBox"
    private ScrollPane userBox;

    @FXML // fx:id="userList"
    private ListView<Label> userList;

    @FXML // fx:id="messageArea"
    private TextField messageArea;

    @FXML
    void sendChatMessage(ActionEvent event) {
        if (!messageArea.getText().isEmpty()) {
            Client.sendMessage("Chat.Say:" + chatID + "," + messageArea.getText());
            messageArea.clear();
        }
    }

    /**
     * Updates the chat window with new message
     * 
     * @param sayMessage
     *            The message that should be updated into chat window
     */
    public void addChatMessage(String sayMessage) {
        Label newMessage = new Label(sayMessage);
        messages.add(newMessage);
        chatList.setItems(messages);
    }

    /**
     * Updates chat names with new name
     * 
     * @param name
     *            The name to be added
     */
    public void addChatName(String name) {
        Label newUser = new Label(name);
        addChatMessage(name + " has joined the chat!");
        usernames.add(newUser);
        userList.setItems(usernames);
    }

    /**
     * Removing client name when leaving chat
     * 
     * @param name
     *            The name of client to remove
     */
    public void removeChatName(String name) {
        for (int i = 0; i < usernames.size(); i++) {
            if (usernames.get(i).getText().equals(name)) {
                usernames.remove(i);
                break;
            }
        }
        addChatMessage(name + " has left the chat!");
        userList.setItems(usernames);
    }

    /**
     * Sends message to server that client leaved game
     */
    public void leaveChat() {
        Client.sendMessage("Chat.Leave:" + chatID);
    }
}
