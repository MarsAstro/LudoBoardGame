package no.ntnu.imt3281.ludo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Controller for the challenge
 * 
 * @author Charles The Gentle
 *
 */
public class ChallengeController {

    ObservableList<Label> challengers = FXCollections.observableArrayList();

    @FXML // fx:id="ChallengedPlayers"
    private ListView<Label> ChallengedPlayers; // Value injected by FXMLLoader

    public void addChallengerNames(String[] names) {
        
    }
}
