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
 * Controller for the challenge pop-up window
 * 
 * @author Charles The Gentle
 *
 */
public class ChallengeController implements Initializable {
    private ResourceBundle messages;
    private ObservableList<Label> challengers = FXCollections.observableArrayList();
    private ArrayList<Boolean> playerConfirmed = new ArrayList<>();
    private ArrayList<Boolean> playerAccepted = new ArrayList<>();

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
     *            The name of the challenger
     */
    public void addChallengerName(String name) {
        challengers.add(new Label(name + ": " + messages.getString("challenge.waiting")));
        challengedPlayers.setItems(challengers);
        playerConfirmed.add(false);
        playerAccepted.add(false);
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
                if (confirm) {
                    status = ": " + messages.getString("challenge.accepted");
                    playerAccepted.set(i, true);
                } else {
                    status = ": " + messages.getString("challenge.declined");
                }
                curLabel.setText(labelName + status);
                playerConfirmed.set(i, true);
            }
        }

        if (allConfirmed()) {
            StringBuilder players = new StringBuilder();
            boolean hasAcceptedPlayers = false;
            for (int i = 0; i < challengers.size(); i++) {
                if (playerAccepted.get(i)) {
                    players.append(challengers.get(i).getText().substring(0,
                            challengers.get(i).getText().indexOf(":")) + ",");
                    hasAcceptedPlayers = true;
                }
            }
            Client.sendMessage("Ludo.ChallengeValidation:" + hasAcceptedPlayers + ","
                    + players.substring(0, players.lastIndexOf(",")));
            closeWindow();
        }
    }

    private boolean allConfirmed() {
        boolean allConfirmed = true;
        for (Boolean confirm : playerConfirmed) {
            if (!confirm) {
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
