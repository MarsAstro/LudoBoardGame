package no.ntnu.imt3281.ludo.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Controller for the challenge
 * 
 * @author Charles The Gentle
 *
 */
public class ChallengeController implements Initializable {
    private ResourceBundle messages;
    private ObservableList<Label> challengers = FXCollections.observableArrayList();
    private ArrayList<Boolean> playerConfirmed = new ArrayList<>();
    private boolean playableGame = false;

    @FXML // fx:id="ChallengedPlayers"
    private ListView<Label> challengedPlayers; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;
    }

    /**
     * Adds challenger name to the list
     * 
     * @param name
     */
    public void addChallengerName(String name) {
        challengers.add(new Label(name + ": " + messages.getString("challenge.waiting")));
        challengedPlayers.setItems(challengers);
        playerConfirmed.add(false);
    }

    /**
     * Updates the confirmation
     * 
     * @param name
     *            Name of the client to be updated
     * @param confirm
     *            If the challenged client accepted or not
     */
    public void setConfirmation(String name, boolean confirm) {
        for (int i = 0; i < challengers.size(); i++) {
            Label curLabel = challengers.get(i);
            String labelName = curLabel.getText().substring(0, curLabel.getText().indexOf(":"));
            if (name.equals(labelName)) {
                String status;
                if (confirm == true) {
                    status = ": " + messages.getString("challenge.accepted");
                    playableGame = true;
                } else {
                    status = ": " + messages.getString("challenge.declined");
                }
                curLabel.setText(labelName + status);
                playerConfirmed.set(i, true);
            }
        }
        
        if (allConfirmed() == true) {
            Client.sendMessage("Ludo.ChallengeValidation:" + playableGame);
            closeWindow();
        }
    }

    private boolean allConfirmed() {
        boolean allConfirmed = true;
        for (Boolean bool : playerConfirmed) {
            if (bool == false) {
                allConfirmed = false;
                break;
            }
        }
        return allConfirmed;
    }
    
    /**
     * Closes window
     */
    public void closeWindow() {
        Stage stage = (Stage) challengedPlayers.getScene().getWindow();
        stage.close();
    }
}
