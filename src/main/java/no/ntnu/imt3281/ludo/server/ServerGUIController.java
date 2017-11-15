
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
    private ScrollPane userIDList;

    /**
     * Goes through all of the connected clients and updates a list containing
     * them
     * 
     */
    public void updateUserList() {
        VBox vBox = new VBox();
        for (ClientInfo clientInfo : Server.connections) {
            vBox.getChildren()
                    .add(new Label("ID: " + clientInfo.clientID + " Address: "
                            + clientInfo.connection.getInetAddress().toString() + " Port: "
                            + clientInfo.connection.getPort()));
        }
        userIDList.setContent(vBox);
    }

}
