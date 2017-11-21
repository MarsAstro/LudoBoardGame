package no.ntnu.imt3281.ludo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

/**
 * Handles chat windows
 * @author Marius
 *
 */
public class ChatWindowController {

    @FXML // fx:id="roomTitle"
    private TitledPane roomTitle;

    @FXML // fx:id="chatBox"
    private ScrollPane chatBox;

    @FXML // fx:id="userBox"
    private ScrollPane userBox;
    
    @FXML // fx:id="userBox"
    private TextField chatArea;

    @FXML
    void sendChatMessage(ActionEvent event) {
        
    }

}
