
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

    @FXML // fx:id="GameList"
    private ScrollPane gameList;

    /**
     * Goes through all of the connected clients and updates a list containing
     * them
     * 
     */
    public void updateUserList() {
        VBox vBox = new VBox();
        Server.clientLock.readLock().lock();
        for (ClientInfo clientInfo : Server.clients) {
            vBox.getChildren()
                    .add(new Label("ID: " + clientInfo.clientID + " Address: "
                            + clientInfo.connection.getInetAddress().toString() + " Port: "
                            + clientInfo.connection.getPort()));
        }
        Server.clientLock.readLock().unlock();
        userIDList.setContent(vBox);
    }

    /**
     * Updates the list of ongoing games on the server
     */
    public void updateGameList() {
        VBox vBox = new VBox();
        Server.gameLock.readLock().lock();
        for (GameInfo gameInfo : Server.games) {
            StringBuilder gameData = new StringBuilder("ID: " + gameInfo.gameID);

            for (ClientInfo user : gameInfo.clients) {
                gameData.append("\n\tID: " + user.clientID + ", Name: " + user.username);
            }
            Label label = new Label(gameData.toString());
            vBox.getChildren().add(label);
        }
        Server.gameLock.readLock().unlock();
        gameList.setContent(vBox);
    }

}
