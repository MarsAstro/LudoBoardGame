package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Controller for the challenge list
 * 
 * @author Charles The Gentle
 *
 */
public class ChallengeListController {

    private ObservableList<Label> players = FXCollections.observableArrayList();
    private ObservableList<Label> challengers = FXCollections.observableArrayList();

    @FXML // fx:id="challengeButton"
    private Button challengeButton;

    @FXML // fx:id="closeButton"
    private Button closeButton;

    @FXML // fx:id="playerList"
    private ListView<Label> playerList;

    @FXML // fx:id="challengerList"
    private ListView<Label> challengerList;

    @FXML
    void challenge(ActionEvent event) {
        if (!challengers.isEmpty()) {
            Client.getLudoController().openChallenge();
            String challengeMessage = "";
            for (Label label : challengers) {
                String labelText = label.getText();
                challengeMessage += labelText + ",";
                Client.getLudoController().getChallengeContoller().addChallengerName(labelText);
            }
            Client.sendMessage("Ludo.Challenge:"
                    + challengeMessage.substring(0, challengeMessage.lastIndexOf(",")));
            closeWindow();
        }
    }

    @FXML
    void closeChallengeList(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void challengeListClicked(MouseEvent event) {
        Label selected = challengerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            challengers.remove(selected);
            players.add(selected);
            playerList.setItems(players);
        }
    }

    @FXML
    void playerListClicked(MouseEvent event) {
        Label selected = playerList.getSelectionModel().getSelectedItem();
        if (selected != null && challengers.size() < 3) {
            players.remove(selected);
            challengers.add(selected);
            challengerList.setItems(challengers);
        }
    }

    /**
     * Adds challenger name to challenger list
     * 
     * @param name
     *            The name of the challenger to be added
     */
    public void addChallengerName(String name) {
        Label newChat = new Label(name);
        challengers.add(newChat);
        challengerList.setItems(challengers);
    }

    /**
     * Adds player name to player list
     * 
     * @param name
     *            The name of the player to be added
     */
    public void addPlayersName(String name) {
        Label newChat = new Label(name);
        players.add(newChat);
        playerList.setItems(players);
    }

    /**
     * Closes window
     */
    public void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}