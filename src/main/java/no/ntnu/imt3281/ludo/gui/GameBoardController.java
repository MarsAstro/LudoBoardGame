package no.ntnu.imt3281.ludo.gui;

import java.awt.Point;
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
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import no.ntnu.imt3281.ludo.client.Client;
import no.ntnu.imt3281.ludo.logic.PlayerEvent;

/**
 * Controller for game board gui
 * 
 * @author Marius
 */
public class GameBoardController implements Initializable {
    ResourceBundle messages;
    
    int gameID;
    int playerID;
    static final int TILESIZE = 48;
    Rectangle selectedToken;
    int selectedTokenTilePos;

    ArrayList<Point> points;
    ArrayList<Label> playerNames;
    ArrayList<ImageView> activeTokens;
    ArrayList<Image> diceImages;
    ArrayList<Image> pieceImages;
    Rectangle[][] playerTokens;

    @FXML // fx:id="player1Active"
    private AnchorPane anchorPane;

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
        messages = resources;
        
        playerNames = new ArrayList<>();
        activeTokens = new ArrayList<>();
        diceImages = new ArrayList<>();
        pieceImages = new ArrayList<>();
        points = new ArrayList<>();
        playerTokens = new Rectangle[4][4];

        anchorPane.setOnMouseClicked(e -> clickOnBoard(e));

        fillPlayerHUDArrays();
        fillPointsArray();
        initPlayerTokens();
    }

    private void clickOnBoard(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        if (selectedToken != null) {
            int tileIndex = CheckForTile(x, y);
            if (tileIndex != -1 && tileIndex != selectedTokenTilePos) {
                Client.sendMessage("Ludo.Move:" + gameID + "," + playerID + ","
                        + selectedTokenTilePos + "," + tileIndex);
                selectedToken.setEffect(null);
                selectedToken = null;
            }
        }
    }

    private int CheckForTile(double x, double y) {
        int result = -1;
        for (int tileIndex = 0; tileIndex < points.size(); tileIndex++) {
            if (x >= points.get(tileIndex).getX() && x <= points.get(tileIndex).getX() + TILESIZE
                    && y >= points.get(tileIndex).getY()
                    && y <= points.get(tileIndex).getY() + TILESIZE) {
                result = tileIndex;
                break;
            }
        }
        return result;
    }

    private void initPlayerTokens() {
        for (int player = 0; player < 4; player++) {
            for (int piece = 0; piece < 4; piece++) {
                playerTokens[player][piece] = new Rectangle(40, 40);
                playerTokens[player][piece].setFill(new ImagePattern(pieceImages.get(player)));
                playerTokens[player][piece].setX(points.get(player * 4 + piece).getX());
                playerTokens[player][piece].setY(points.get(player * 4 + piece).getY());
                playerTokens[player][piece].setOnMouseClicked(e -> clickOnPiece(e));
                anchorPane.getChildren().add(playerTokens[player][piece]);
            }
        }
    }

    private void clickOnPiece(MouseEvent event) {
        if (event.getSource() instanceof Rectangle && selectedToken == null) {
            Rectangle hitRect = (Rectangle) event.getSource();
            for (int piece = 0; piece < 4; piece++) {
                playerTokens[playerID][piece].setEffect(null);
                if (playerTokens[playerID][piece] == hitRect) {
                    selectedToken = hitRect;
                    // Using event instead of hitrect position, because of
                    // rectangle gives floating point errors
                    selectedTokenTilePos = CheckForTile(event.getX(), event.getY());

                    DropShadow ds = new DropShadow();
                    ds.setOffsetY(5);
                    ds.setOffsetX(10);
                    ds.setColor(Color.HOTPINK);
                    ds.setBlurType(BlurType.GAUSSIAN);

                    Bloom bloom = new Bloom();
                    bloom.setThreshold(0);

                    ds.setInput(bloom);
                    selectedToken.setEffect(ds);
                }
            }
        }
    }

    private void fillPointsArray() {
        // Red starting fields 0-3
        points.add(new Point(554, 74));
        points.add(new Point(554 + TILESIZE, 74 + TILESIZE));
        points.add(new Point(554, 74 + TILESIZE * 2));
        points.add(new Point(554 - TILESIZE, 74 + TILESIZE));

        // Blue starting fields 4-7
        points.add(new Point(554, 506));
        points.add(new Point(554 + TILESIZE, 506 + TILESIZE));
        points.add(new Point(554, 506 + TILESIZE * 2));
        points.add(new Point(554 - TILESIZE, 506 + TILESIZE));

        // Yellow starting fields 8-11
        points.add(new Point(122, 506));
        points.add(new Point(122 + TILESIZE, 506 + TILESIZE));
        points.add(new Point(122, 506 + TILESIZE * 2));
        points.add(new Point(122 - TILESIZE, 506 + TILESIZE));

        // Green starting fields 12-15
        points.add(new Point(122, 74));
        points.add(new Point(122 + TILESIZE, 74 + TILESIZE));
        points.add(new Point(122, 74 + TILESIZE * 2));
        points.add(new Point(122 - TILESIZE, 74 + TILESIZE));

        // 16-20
        for (int i = 0; i < 5; ++i) {
            points.add(new Point(8 * TILESIZE, (1 + i) * TILESIZE));
        }

        // 21-26
        for (int i = 0; i < 6; ++i) {
            points.add(new Point((9 + i) * TILESIZE, 6 * TILESIZE));
        }

        // 27
        points.add(new Point(14 * TILESIZE, 7 * TILESIZE));

        // 28-33
        for (int i = 0; i < 6; ++i) {
            points.add(new Point((14 - i) * TILESIZE, 8 * TILESIZE));
        }

        // 33-39
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(8 * TILESIZE, (9 + i) * TILESIZE));
        }

        // 40
        points.add(new Point(7 * TILESIZE, 14 * TILESIZE));

        // 41-46
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(6 * TILESIZE, (14 - i) * TILESIZE));
        }

        // 47-52
        for (int i = 0; i < 6; ++i) {
            points.add(new Point((5 - i) * TILESIZE, 8 * TILESIZE));
        }

        // 53
        points.add(new Point(0, 7 * TILESIZE));

        // 54-59
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(i * TILESIZE, 6 * TILESIZE));
        }

        // 60-65
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(6 * TILESIZE, (5 - i) * TILESIZE));
        }

        // 66 & 67
        points.add(new Point(7 * TILESIZE, 0));
        points.add(new Point(8 * TILESIZE, 0));

        // 68-73
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(7 * TILESIZE, (1 + i) * TILESIZE));
        }

        // 74-79
        for (int i = 0; i < 6; ++i) {
            points.add(new Point((13 - i) * TILESIZE, 7 * TILESIZE));
        }

        // 80-85
        for (int i = 0; i < 6; ++i) {
            points.add(new Point(7 * TILESIZE, (13 - i) * TILESIZE));
        }

        // 86-91
        for (int i = 0; i < 6; ++i) {
            points.add(new Point((1 + i) * TILESIZE, 7 * TILESIZE));
        }
    }

    private void fillPlayerHUDArrays() {
        playerNames.add(player1Name);
        playerNames.add(player2Name);
        playerNames.add(player3Name);
        playerNames.add(player4Name);

        activeTokens.add(player1Active);
        activeTokens.add(player2Active);
        activeTokens.add(player3Active);
        activeTokens.add(player4Active);

        diceImages.add(new Image(getClass().getResourceAsStream("/images/rolldice.png")));
        for (int i = 1; i <= 6; ++i) {
            diceImages.add(new Image(getClass().getResourceAsStream("/images/dice" + i + ".png")));
        }

        for (int i = 0; i < 4; ++i) {
            pieceImages
                    .add(new Image(getClass().getResourceAsStream("/images/piece" + i + ".png")));
        }
    }

    @FXML
    void say(ActionEvent event) {
        // TODO sjætt
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
        diceThrown.setVisible(false);
        switch (state) {
            case PlayerEvent.PLAYING :
                activeTokens.get(playerIndex).setVisible(true);
                if (playerIndex == playerID) {
                    throwTheDice.setText(messages.getString("ludogameboard.throwdice"));
                    throwTheDice.setDisable(false);
                }
                else
                {
                    throwTheDice.setText(messages.getString("ludogameboard.wait"));
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
     * @param canMove 
     */
    public void updateDice(int playerIndex, int dice, boolean canMove) {
        diceThrown.setImage(diceImages.get(dice));
        diceThrown.setVisible(true);
        if (playerIndex == playerID && canMove) {
            throwTheDice.setText(messages.getString("ludogameboard.move"));
            throwTheDice.setDisable(true);
        }
    }

    /**
     * Updates the game board with the result of a piece move
     * 
     * @param playerID
     *            The playerID to the player owning piece
     * @param piece
     *            The piece index
     * @param from
     *            The tile piece moved from
     * @param to
     *            The tile piece moved to
     */
    public void updatePiece(int playerID, int piece, int from, int to) {        
        playerTokens[playerID][piece].setX(points.get(to).getX() + 8 + (4 * piece));
        playerTokens[playerID][piece].setY(points.get(to).getY() + 8 + (8 * piece));
        
        if (playerID == this.playerID)
        {
            throwTheDice.setDisable(false);
            throwTheDice.setText(messages.getString("ludogameboard.throwdice"));
        }
    }
}