/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

import java.util.ArrayList;

/**
 * @author oyste
 *
 */
public class Ludo {

    /**
     * Index of Red player
     */
    public static final int RED = 0;

    /**
     * Index of Blue player
     */
    public static final int BLUE = 1;

    /**
     * Index of Yellow player
     */
    public static final int YELLOW = 2;

    /**
     * Index of Green player
     */
    public static final int GREEN = 3;

    private static final String WINNER = "Winner: ";
    private ArrayList<String> playerNames;
    private ArrayList<DiceListener> diceListeners;
    private ArrayList<PieceListener> pieceListeners;
    private ArrayList<PlayerListener> playerListeners;
    private int[][] piecePositions;
    private int[][] globalPiecePositions;
    private int activePlayer = 0;
    private int numThrows = 0;
    private int dice = 0;

    /**
     * Initialize a game without players
     */
    public Ludo() {
        playerNames = new ArrayList<>();
        diceListeners = new ArrayList<>();
        pieceListeners = new ArrayList<>();
        playerListeners = new ArrayList<>();
        piecePositions = new int[4][4];
        globalPiecePositions = new int[4][4];
    }

    /**
     * Initialize a game with already known names
     * 
     * @param name1
     *            name of player 1
     * @param name2
     *            name of player 2
     * @param name3
     *            name of player 3
     * @param name4
     *            name of player 4
     * @throws NotEnoughPlayersException
     *             thrown if less than 2 players
     */
    public Ludo(String name1, String name2, String name3, String name4) {
        piecePositions = new int[4][4];
        globalPiecePositions = new int[4][4];
        diceListeners = new ArrayList<>();
        pieceListeners = new ArrayList<>();
        playerListeners = new ArrayList<>();
        playerNames = new ArrayList<>();
        playerNames.add(name1);
        playerNames.add(name2);
        playerNames.add(name3);
        playerNames.add(name4);
        if (nrOfPlayers() < 2) {
            throw new NotEnoughPlayersException();
        }
    }

    /**
     * @return number of players
     */
    public int nrOfPlayers() {
        int numberOfPlayers = 0;
        for (int i = 0; i < playerNames.size(); i++) {
            if (playerNames.get(i) != null) {
                numberOfPlayers++;
            }
        }
        return numberOfPlayers;
    }

    /**
     * Gets player name from index
     * 
     * @param index
     *            index representing a color
     * @return name of player at index
     */
    public String getPlayerName(int index) {
        return playerNames.get(index);
    }

    /**
     * Adds a new player to the game
     * 
     * @param newPlayer
     *            name of the new player
     * @throws NoRoomForMorePlayersException
     *             thrown if game is full
     */
    public void addPlayer(String newPlayer) {
        if (nrOfPlayers() < 4) {
            playerNames.add(newPlayer);
        } else {
            throw new NoRoomForMorePlayersException();
        }
    }

    /**
     * Sets player with name as inactive
     * 
     * @param name
     *            name of player to be set inactive
     */
    public void removePlayer(String name) {
        for (int player = 0; player < playerNames.size(); player++) {
            if (playerNames.get(player) != null
                    && playerNames.get(player).equals(name)) {
                playerNames.set(player, "Inactive: " + playerNames.get(player));

                for (PlayerListener listener : playerListeners) {
                    listener.playerStateChanged(
                            new PlayerEvent(this, player, PlayerEvent.LEFTGAME));
                }

                if (player == activePlayer) {
                    nextPlayer();
                }
            }
        }
    }

    /**
     * @return number of active players
     */
    public int activePlayers() {
        int count = 0;
        for (int i = 0; i < playerNames.size(); i++) {
            if (!playerNames.get(i).contains("Inactive:")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Find position of a specific piece
     * 
     * @param player
     *            player that owns that piece
     * @param piece
     *            index of piece
     * @return the piece position on the board
     */
    public int getPosition(int player, int piece) {
        return piecePositions[player][piece];
    }

    /**
     * @return active player
     */
    public int activePlayer() {
        return activePlayer;
    }

    /**
     * @return A randomly generated die throw
     */
    public int throwDice() {
        return 0;
    }

    /**
     * Takes a die throw and handles the game logic around dice
     * 
     * @param dice
     *            dice
     * @return The die throw
     */
    public int throwDice(int dice) {
        this.dice = dice;

        for (DiceListener listener : diceListeners) {
            listener.diceThrown(new DiceEvent(this, activePlayer, dice));
        }

        if (allHome()) {
            numThrows++;
            if (numThrows >= 3 && dice != 6) {
                nextPlayer();
            }
        } else {
            if (canMove()) {
                numThrows++;
                if (dice == 6 && numThrows > 2) {
                    nextPlayer();
                }
            } else {
                nextPlayer();
            }
        }
        return dice;
    }

    /**
     * Goes through all of the pieces for the active player and determines if
     * anyone of them can move
     * 
     * @return A piece is able to move
     */
    private boolean canMove() {
        boolean movable = false;
        for (int i = 0; i < 4 && !isBlocked(i); i++) {
            if ((piecePositions[activePlayer][i] + dice < 60
                    && piecePositions[activePlayer][i] != 0)
                    || (piecePositions[activePlayer][i] == 0 && dice == 6)) {
                movable = true;
                break;
            }
        }
        return movable;
    }

    /**
     * Checks if a piece is blocked by a tower
     * 
     * @param currentPiece
     *            piece to check for current player
     * @return if piece is blocked
     */
    private boolean isBlocked(int currentPiece) {
        for (int player = 0; player < playerNames.size(); player++) {
            if (player != activePlayer) {
                for (int piece = 0; piece < 4; piece++) {
                    for (int otherPiece = piece + 1; otherPiece < 4; otherPiece++) {
                        if (piecePositions[player][piece] == piecePositions[player][otherPiece]) {
                            for (int i = 1; i <= dice; i++) {
                                if (globalPiecePositions[player][piece] == globalPiecePositions[activePlayer][currentPiece]
                                        + i) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets the next player as active
     */
    private void nextPlayer() {
        numThrows = 0;

        for (PlayerListener listener : playerListeners) {
            listener.playerStateChanged(new PlayerEvent(this, activePlayer, PlayerEvent.WAITING));
        }

        activePlayer = (activePlayer + 1) % playerNames.size();

        for (PlayerListener listener : playerListeners) {
            listener.playerStateChanged(new PlayerEvent(this, activePlayer, PlayerEvent.PLAYING));
        }

        if (playerNames.get(activePlayer) == null
                || playerNames.get(activePlayer).contains("Inactive: ")
                || playerNames.get(activePlayer).contains(WINNER)) {
            nextPlayer();
        }

    }

    /**
     * Moves a piece
     * 
     * @param player
     *            Player index
     * @param from
     *            Tile to move from
     * @param to
     *            Tile to move to
     * @return If move was successful
     */
    public boolean movePiece(int player, int from, int to) {
        boolean success = false;
        for (int i = 0; i < 4; i++) {
            if (piecePositions[player][i] == from) {
                piecePositions[player][i] = to;
                for (PieceListener listener : pieceListeners) {
                    listener.pieceMoved(new PieceEvent(this, player, i, from, to));
                }
                success = true;
                checkWinner();

                if (numThrows >= 3 || dice != 6 || from == 0) {
                    nextPlayer();
                }
                checkForOpponents(player, to);
                updateGlobalPositions();
                break;
            }
        }
        return success;
    }

    /**
     * @return Game status
     */
    public String getStatus() {
        String state = "Created";
        if (nrOfPlayers() != 0) {
            state = "Initiated";
        }
        if (dice != 0) {
            state = "Started";
        }
        for (int i = 0; i < playerNames.size(); i++) {
            boolean finished = true;
            for (int j = 0; j < 4; j++) {
                if (piecePositions[i][j] != 59) {
                    finished = false;
                }
            }
            if (finished) {
                state = "Finished";
                activePlayer = i;
                break;
            }
        }
        return state;
    }

    /**
     * @return winner
     */
    public int getWinner() {
        int winner = -1;

        for (int player = 0; player < playerNames.size(); ++player) {
            if (playerNames.get(player) != null
                    && playerNames.get(player).contains(WINNER)) {
                winner = player;
            }
        }

        return winner;
    }

    /**
     * @return if all pieces of active player is home
     */
    private boolean allHome() {
        for (int i = 0; i < 4; i++) {
            if (piecePositions[activePlayer][i] != 0) {
                return false;
            }
        }
        return true;
    }

    private void checkWinner() {
        boolean finished = true;
        for (int piece = 0; piece < 4; piece++) {
            if (piecePositions[activePlayer][piece] != 59) {
                finished = false;
            }
        }

        if (finished && getWinner() == -1) {
            playerNames.set(activePlayer, WINNER + playerNames.get(activePlayer));

            for (PlayerListener listener : playerListeners) {
                listener.playerStateChanged(new PlayerEvent(this, activePlayer, PlayerEvent.WON));
            }
        }
    }

    /**
     * Converts player position to ludo board grid
     * 
     * @param player
     *            The player
     * @param pos
     *            Player position
     * @return the ludo board grid index
     */
    public int userGridToLudoBoardGrid(int player, int pos) {
        int result = 0;
        if (pos == 0) {
            result = 4 * player;
        } else if (pos < 54) {
            result = (player * 13 + pos) % 52 + 15;
        } else {
            result = 53 + 6 * player + 15;
        }
        return result;
    }

    /**
     * Checks whether someone stands on a piece's destination position. And
     * moves them back if landed on
     * 
     * @param player
     *            Player index
     * @param pos
     *            Local position to check for opponent at
     */
    private void checkForOpponents(int player, int pos) {
        int playerPos = userGridToLudoBoardGrid(player, pos);
        for (int otherPlayer = 0; otherPlayer < playerNames.size(); otherPlayer++) {
            if (otherPlayer != player) {
                for (int piece = 0; piece < 4; piece++) {
                    if (userGridToLudoBoardGrid(otherPlayer,
                            piecePositions[otherPlayer][piece]) == playerPos) {
                        for (PieceListener listener : pieceListeners) {
                            listener.pieceMoved(new PieceEvent(this, otherPlayer, piece,
                                    piecePositions[otherPlayer][piece], 0));
                        }
                        piecePositions[otherPlayer][piece] = 0;
                    }
                }
            }
        }
    }

    /**
     * Updates array of global piece positions
     */
    private void updateGlobalPositions() {
        for (int player = 0; player < playerNames.size(); player++) {
            for (int piece = 0; piece < 4; piece++) {
                if (piecePositions[player][piece] > 0) {
                    globalPiecePositions[player][piece] = userGridToLudoBoardGrid(player,
                            piecePositions[player][piece]);
                }
            }
        }
    }

    /**
     * Adds dice listener
     * 
     * @param diceListener
     *            DiceListener to add
     */
    public void addDiceListener(DiceListener diceListener) {
        diceListeners.add(diceListener);
    }

    /**
     * Adds piece listener
     * 
     * @param pieceListener
     *            PieceListener to add
     */
    public void addPieceListener(PieceListener pieceListener) {
        pieceListeners.add(pieceListener);
    }

    /**
     * Adds player listener
     * 
     * @param playerListener
     *            PlayerListener to add
     */
    public void addPlayerListener(PlayerListener playerListener) {
        playerListeners.add(playerListener);
    }

}