package no.ntnu.imt3281.ludo.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;

/**
 * Controller for game board gui
 * 
 * @author Marius
 */
public class GameBoardController implements Initializable {

    int gameID;
    int playerID;

    ArrayList<Label> playerNames;
    ArrayList<ImageView> activeTokens;
    ArrayList<Image> diceImages;

    @FXML // fx:id="player1Active"
    private ImageView player1Active;

    @FXML // fx:id="player1Name"
    private Label player1Name;

    @FXML // fx:id="player2Active"
    private ImageView player2Active;

    @FXML // fx:id="player2Name"
    private Label player2Name;

    @FXML // fx:id="player3Active"
    private ImageView player3Active;

    @FXML // fx:id="player3Name"
    private Label player3Name;

    @FXML // fx:id="player4Active"
    private ImageView player4Active;

    @FXML // fx:id="player4Name"
    private Label player4Name;

    @FXML // fx:id="diceThrown"
    private ImageView diceThrown;

    @FXML // fx:id="throwTheDice"
    private Button throwTheDice;

    @FXML // fx:id="chatArea"
    private TextArea chatArea;

    @FXML // fx:id="textToSay"
    private TextField textToSay;

    @FXML // fx:id="sendTextButton"
    private Button sendTextButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerNames = new ArrayList<>();
        activeTokens = new ArrayList<>();
        diceImages = new ArrayList<>();

        playerNames.add(player1Name);
        playerNames.add(player2Name);
        playerNames.add(player3Name);
        playerNames.add(player4Name);

        activeTokens.add(player1Active);
        activeTokens.add(player2Active);
        activeTokens.add(player3Active);
        activeTokens.add(player4Active);

        diceImages.add(new Image("/images/rolldice.png"));
        for (int i = 1; i <= 6; ++i) {
            diceImages.add(new Image("/images/dice" + i + ".png"));
        }
    }

    @FXML
    void say(ActionEvent event) {
        // TODO
    }

    @FXML
    void throwDice(ActionEvent event) {
        diceThrown.setImage(diceImages.get(0));
        Client.sendMessage("Ludo.Throw:" + gameID);
    }

    void leaveGame() {
        Client.sendMessage("Ludo.Leave:" + gameID);
    }

    /**
     * Updates game board name
     * 
     * @param name
     *            The name that should be visible
     * @param player
     *            The index of player with name
     */
    public void updateName(String name, int player) {
        playerNames.get(player).setText(name);
    }

    /**
     * Updates the active player token when player changes state
     * 
     * @param playerIndex
     *            The player that changed state
     * @param state
     *            Which state the player is at
     */
    public void updateActivePlayer(int playerIndex, int state) {
        throwTheDice.setDisable(true);
        switch (state) {
            case PlayerEvent.PLAYING :
                activeTokens.get(playerIndex).setVisible(true);
                if (playerIndex == playerID) {
                    throwTheDice.setDisable(false);
                }
                break;
            case PlayerEvent.WAITING :
                activeTokens.get(playerIndex).setVisible(false);
                break;
            default :
                break;
        }
    }

    /**
     * Updates the game board to reflect the result of a dice throw
     * 
     * @param playerIndex
     *            Index of the throwing player
     * @param dice
     *            The dice thrown
     */
    public void updateDice(int playerIndex, int dice) {
        diceThrown.setImage(diceImages.get(dice));
        if (playerIndex == playerID) {
            // TODO can move
        }
    }
}