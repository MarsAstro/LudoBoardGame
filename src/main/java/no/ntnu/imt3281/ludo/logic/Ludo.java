/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

import java.util.Arrays;
import java.util.Vector;

/**
 * @author oyste
 *
 */
public class Ludo {

	public static final int RED = 0;
	public static final int BLUE = 1;
	public static final int YELLOW = 2;
	public static final int GREEN = 3;
	
	private Vector<String> playerNames;
	private int piecePositions[][];
	private int activePlayer = 0;
	private int numThrows = 0;
	private int dice = 0;
	
	/**
	 * Initialize a game without players
	 */
	public Ludo() {
		playerNames = new Vector<String>();
		piecePositions = new int[4][4];
	}
	
	/**
	 * Initialize a game with already known names
	 * @param name1 name of player 1
	 * @param name2 name of player 2
	 * @param name3 name of player 3
	 * @param name4 name of player 4
	 * @throws NotEnoughPlayersException thrown if less than 2 players
	 */
	public Ludo(String name1, String name2, String name3, String name4) throws NotEnoughPlayersException {
		piecePositions = new int[4][4];
		playerNames = new Vector<String>();
		playerNames.addElement(name1);
		playerNames.addElement(name2);
		playerNames.addElement(name3);
		playerNames.addElement(name4);
		if(nrOfPlayers() < 2) {
			throw new NotEnoughPlayersException();
		}
	}

	/**
	 * @return number of players
	 */
	public int nrOfPlayers() {
		int numberOfPlayers = 0;
		for (int i = 0; i < playerNames.size(); i++) {
			if(playerNames.elementAt(i) != null) {
				numberOfPlayers++;
			}
		}
		return numberOfPlayers;
	}

	/**
	 * Gets player name from index
	 * @param index index representing a color
	 * @return name of player at index
	 */
	public String getPlayerName(int index) {
		return playerNames.elementAt(index);
	}

	/**
	 * Adds a new player to the game
	 * @param newPlayer name of the new player
	 * @throws NoRoomForMorePlayersException thrown if game is full
	 */
	public void addPlayer(String newPlayer) throws NoRoomForMorePlayersException {
		if(nrOfPlayers() < 4) {
			playerNames.addElement(newPlayer);
		}
		else {
			throw new NoRoomForMorePlayersException();
		}
	}

	/**
	 * Sets player with name as inactive
	 * @param name name of player to be set inactive
	 */
	public void removePlayer(String name) {
		for (int i = 0; i < playerNames.size() ; i++) {
			if (playerNames.elementAt(i).equals(name)){
				playerNames.set(i, "Inactive: " + playerNames.elementAt(i));
			}
		}
	}

	/**
	 * @return number of active players
	 */
	public int activePlayers() {
		int count = 0;
		for (int i = 0; i < playerNames.size(); i++) {
			if(!playerNames.elementAt(i).contains("Inactive:")) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Find position of a specific piece
	 * @param player player that owns that piece
	 * @param piece index of piece
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
	 * 
	 */
	public int throwDice() {
		return 0;
	}
	
	/**
	 * 
	 * @param dice dice
	 */
	public int throwDice(int dice) {
		this.dice = dice;
		System.out.println("Throws: " + numThrows);
		if (allHome()) {
			numThrows++;
			if (numThrows >= 3 && dice != 6) {
				nextPlayer();
			}
		}
		else {
			if (canMove()) {
				numThrows++;
				if (dice == 6 && numThrows > 2) {
					nextPlayer();
				}
			}
			else {
				System.out.println("whaaa");
				nextPlayer();
			}
		}
		return dice;
	}

	/**
	 * Goes through all of the pieces for the active player and determines if anyone of them can move
	 * @return A piece is able to move
	 */
	private boolean canMove() {
		boolean movable = false;
		for (int i = 0; i < 4; i ++) {
			if ((piecePositions[activePlayer][i] + dice < 60 && piecePositions[activePlayer][i] != 0) 
					|| (piecePositions[activePlayer][i] == 0 && dice == 6)) {
				movable = true;
				break;
			}
		}
		return movable;
	}

	/**
	 * Sets the next player as active
	 */
	private void nextPlayer() {
		numThrows = 0;
		System.out.println(activePlayer);
		activePlayer = (activePlayer + 1) % nrOfPlayers();
		System.out.println(activePlayer);
	}

	/**
	 * moves a piece
	 * @param player
	 * @param from
	 * @param to
	 * @return success
	 */
	public boolean movePiece(int player, int from, int to) {
		boolean success = false;
		for (int i = 0; i < 4; i++) {
			if(piecePositions[player][i] == from) {
				piecePositions[player][i] = to;
				success = true;
				System.out.println(from);
				if (numThrows >= 3 || dice != 6 || from == 0) {
					nextPlayer();
				}
				checkForOpponents(player, to);
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
		return activePlayer;
	}

	/**
	 * @return if all pieces of active player is home
	 */
	private boolean allHome() {
		for (int i = 0; i < 4; i ++) {
			if(piecePositions[activePlayer][i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	private void checkWinner() {
		
	}

	/**
	 * Converts player position to ludo board grid
	 * @param player The player 
	 * @param pos Player position
	 * @return the ludo board grid index
	 */
	public int userGridToLudoBoardGrid(int player, int pos) {
		int result = 0;
		if (pos == 0) {
			result = 4 * player;
		}
		else if (pos < 54) {
			result = (player * 13 + pos) % 52 + 15;
		}
		else {
			result = 53 + 6 * player + 15;
		}
		return result;
	}
	
	/**
	 * Checks whether someone stands on a piece's destination position.
	 * @param player
	 * @param pos
	 */
	private void checkForOpponents(int player, int pos) {
		int playerPos = userGridToLudoBoardGrid(player, pos);
		for (int i = 0; i < playerNames.size(); i++) {
			if (i != player) {
				for (int j = 0; j < 4; j++) {
					if (userGridToLudoBoardGrid(i, piecePositions[i][j]) == playerPos) {
						piecePositions[i][j] = 0;
						System.out.println("heee");
					}
				}
			}
		}
	}
	
}
