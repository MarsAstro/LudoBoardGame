
package no.ntnu.imt3281.ludo.server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Handles the server gui
 * 
 * @author Marius
 *
 */
public class ServerGUIController {

    @FXML // fx:id="userIDList"
    private ScrollPane userIDList; // Value injected by FXMLLoader

    /**
     * Goes through all of the connected clients and updates a list containing
     * them
     * 
     */
    public void updateUserList() {
        VBox vBox = new VBox();
        for (ClientInfo clientInfo : Server.connectedClients) {
            vBox.getChildren().add(new Label("ID: " + clientInfo.clientID + " Address: "
                    + clientInfo.address.toString() + " Port: " + clientInfo.port));
        }
        userIDList.setContent(vBox);
    }

}
