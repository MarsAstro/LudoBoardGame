/**
 * 
 */
package no.ntnu.imt3281.ludo.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.InOrder;

/**
 * @author okolloen
 *
 */
public class LudoTest {

    /**
     * Tests constants and constructors for class Ludo. Tests the constants for
     * players (RED=0, BLUE=1, YELLOW=2 and GREEN=3) and that the empty
     * constructors initiates a game with no players. The constructor with
     * parameters should require at least two players, or else throw an
     * exception.
     * 
     * The method nrOfPlayers should return the number of players registered for
     * the game.
     * 
     * The method getPlayerName should return the name of the given player.
     */
    @Test
    public void initialTest() {
        // Player 1 should be red, player 2 should be blue, player 3 should be yellow 
        // and player 4 should be green.
        assertEquals(0, Ludo.RED, 0);
        assertEquals(1, Ludo.BLUE, 0);
        assertEquals(2, Ludo.YELLOW, 0);
        assertEquals(3, Ludo.GREEN, 0);

        // Create new game, should have no players.
        Ludo ludo = new Ludo();
        assertEquals(0, ludo.nrOfPlayers(), 0);

        // Create new game with three players
        try {
            ludo = new Ludo("Player1", "Player2", "Player3", null);
        } catch (NotEnoughPlayersException e) {
            assertEquals("No exception should be thrown", e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(3, ludo.nrOfPlayers(), 0);
        assertEquals("Player1", ludo.getPlayerName(Ludo.RED));
        assertEquals("Player2", ludo.getPlayerName(Ludo.BLUE));
        assertEquals("Player3", ludo.getPlayerName(Ludo.YELLOW));
        assertEquals(null, ludo.getPlayerName(Ludo.GREEN));

        // Try to create new game with one player
        boolean exceptionThrown = false;
        try {
            ludo = new Ludo("Player1", null, null, null);
        } catch (NotEnoughPlayersException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * Test the methods addPlayer and removePlayer. It should be possible to add
     * up to four players. Trying to add a fifth player should throw an
     * exception. Removing a player should not actually remove the player but
     * mark the player as inactive. I.e. the number of players returned by
     * nrOfPlayers should not be affected.
     * 
     * The method activePlayers should be used to return the number of active
     * players in a game.
     * 
     * The method getPlayerName should return the name of the player prepended
     * with the string "Inactive: " for players that have been removed from the
     * game.
     */
    @Test
    public void addingRemovingPlayers() {
        Ludo ludo = new Ludo();
        ludo.addPlayer("Player A");
        ludo.addPlayer("Player B");
        ludo.addPlayer("Player C");
        ludo.addPlayer("Player D");

        assertEquals(4, ludo.nrOfPlayers(), 0);

        // Try to add a fifth player, should throw an exception
        boolean exceptionThrown = false;
        try {
            ludo.addPlayer("Player E");
        } catch (NoRoomForMorePlayersException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // Removing a player should not remove the name
        // it should only set the player to inactive
        ludo.removePlayer("Player B");
        assertEquals(4, ludo.nrOfPlayers(), 0);
        assertEquals(3, ludo.activePlayers(), 0);
        assertEquals("Player D", ludo.getPlayerName(Ludo.GREEN));
        assertEquals("Inactive: Player B", ludo.getPlayerName(Ludo.BLUE));
    }

    /**
     * Test initial positions for all pieces for all players. All pieces for all
     * players should be placed at position 0 (start field). Player RED will
     * always start, so playersTurn should return RED.
     */
    @Test
    public void testInitialPositions() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        // All pieces should be placed at position 0
        for (int player = 0; player < 4; player++) { // RED is 0, GREEN is 3
            for (int piece = 0; piece < 4; piece++) {
                assertEquals(0, ludo.getPosition(player, piece), 0);
            }
        }

        // It should be player RED's turn. This player can throw the dice and try to move
        // his/her pieces.
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
    }

    /**
     * Player must throw a six to get out of the home position. Since all pieces
     * are placed at the home position at the beginning of the game it means
     * that the player must throw a six to be able to move. With all pieces at
     * the home position a player gets three attempts at getting a six. If
     * player does not get a six, the turn moves to the next player.
     * 
     * If not able to move, the turn should go to the next player.
     * 
     * When a user throws a six, the player gets and extra turn. Note, the
     * player does NOT get an extra turn if that six is used to move out of the
     * home position.
     * 
     * When the user has moved (and did not get a six) turn goes to the next
     * player. Moving a piece returns true if legal move, false if piece could
     * not be moved.
     */
    @Test
    public void needASixToGetStarted() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(1);
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(2);
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(3);
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        ludo.throwDice(4);
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        ludo.throwDice(5);
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        ludo.throwDice(6);
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        assertTrue(ludo.movePiece(Ludo.BLUE, 0, 1)); // Move players piece from start(0) to square 1
        assertEquals(1, ludo.getPosition(Ludo.BLUE, 0));

        // Since player moved out of start, the next player should get his/her turn(s)
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(4); // RED has not left start
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(3); // Should get three attempts
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(2);

        // It should now be the other players turn
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        int dice = ludo.throwDice(); // Let server generate a throw
        assertTrue(ludo.movePiece(Ludo.BLUE, 1, 1 + dice)); // Move the piece
        assertEquals(1 + dice, ludo.getPosition(Ludo.BLUE, 0));

        if (dice == 6) // If BLUE threw a six BLUE should keep playing 
            assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);
        else // If not it should be REDs turn
            assertEquals(Ludo.RED, ludo.activePlayer(), 0);
    }

    /**
     * Checks that player need exact number of eyes on the dice to move to
     * finish. When all players pieces is finished (at position 59) the game is
     * completed and correct winner is set. Also checks game status, "Created",
     * "Initiated", "Started", "Finished".
     */
    @Test
    public void gameStates() {
        Ludo ludo = new Ludo();
        assertEquals("Created", ludo.getStatus()); // A game with no players are created
        ludo.addPlayer("Player1");
        assertEquals("Initiated", ludo.getStatus()); // A game with players are Initiated
        ludo = new Ludo("Player1", "Player2", null, null);
        assertEquals("Initiated", ludo.getStatus());

        // Move first piece from start to finish (player RED)
        ludo.throwDice(6);
        assertEquals("Started", ludo.getStatus()); // A game where the dice has been thrown is started
        assertTrue(ludo.movePiece(Ludo.RED, 0, 1));

        for (int move = 0; move < 11; move++) {
            skipPlayer(ludo); // Skip blue player
            ludo.throwDice(5);
            assertTrue(ludo.movePiece(Ludo.RED, 1 + move * 5, 1 + (move + 1) * 5));
        }
        assertEquals(56, ludo.getPosition(Ludo.RED, 0), 0); // RED has 3 more to go before home
        skipPlayer(ludo);
        assertEquals(Ludo.RED, ludo.activePlayer(), 0); // Should be RED players turn
        ludo.throwDice(5); // RED can not move 5
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
        skipPlayer(ludo);
        ludo.throwDice(4); // RED can not move 4
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
        skipPlayer(ludo); // Skip blue player
        ludo.throwDice(3); // RED CAN move 3
        assertTrue(ludo.movePiece(Ludo.RED, 56, 59)); // This piece is now finished
        assertEquals("Started", ludo.getStatus());
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
        skipPlayer(ludo);

        // Move two more pieces
        for (int piece = 1; piece < 3; piece++) {
            ludo.throwDice(6);
            assertEquals("Started", ludo.getStatus());
            assertTrue(ludo.movePiece(Ludo.RED, 0, 1));

            for (int move = 0; move < 11; move++) {
                skipPlayer(ludo); // Skip blue plaYer
                ludo.throwDice(5);
                assertTrue(ludo.movePiece(Ludo.RED, 1 + move * 5, 1 + (move + 1) * 5));
            }
            assertEquals(56, ludo.getPosition(Ludo.RED, piece), 0); // RED has 3 more to go before home
            skipPlayer(ludo);
            assertEquals(Ludo.RED, ludo.activePlayer(), 0); // Should be RED players turn
            ludo.throwDice(5); // RED can not move 5
            assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
            skipPlayer(ludo);
            ludo.throwDice(4); // RED can not move 4
            assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
            skipPlayer(ludo);
            ludo.throwDice(3); // RED CAN move 3
            assertTrue(ludo.movePiece(Ludo.RED, 56, 59)); // This piece is now finished
            assertEquals("Started", ludo.getStatus());

            assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
            skipPlayer(ludo);
        }

        // Red now has one more piece to move to finish
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 0, 1));
        skipPlayer(ludo);
        // We will now try to throw several sixes in a row
        assertEquals(6, ludo.throwDice(6), 0);
        assertTrue(ludo.movePiece(Ludo.RED, 1, 7));
        assertEquals(6, ludo.throwDice(6), 0);
        assertTrue(ludo.movePiece(Ludo.RED, 7, 13));
        assertEquals(6, ludo.throwDice(6), 0);
        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // Should be BLUE players turn
        skipPlayer(ludo);
        assertEquals(6, ludo.throwDice(6), 0);
        assertTrue(ludo.movePiece(Ludo.RED, 13, 19));
        assertEquals(6, ludo.throwDice(6), 0);
        assertTrue(ludo.movePiece(Ludo.RED, 19, 25));
        assertEquals(5, ludo.throwDice(5), 0);
        assertEquals(Ludo.RED, ludo.activePlayer(), 0); // Two sixes and then a five is ok
        assertTrue(ludo.movePiece(Ludo.RED, 25, 30));
        skipPlayer(ludo);
        for (int move = 0; move < 5; move++) {
            assertEquals(5, ludo.throwDice(5), 0);
            assertTrue(ludo.movePiece(Ludo.RED, 30 + move * 5, 35 + move * 5));
            skipPlayer(ludo);
        }
        // Last RED piece on position 55, need a four to get to finish
        ludo.throwDice(4);
        assertTrue(ludo.movePiece(Ludo.RED, 55, 59));
        assertEquals("Finished", ludo.getStatus()); // A game with a winner is finished
        assertEquals(Ludo.RED, ludo.getWinner(), 0);
    }

    /**
     * To be able to detect when pieces from different players end up on the
     * same playing field and to place pieces on the Ludo board we need to be
     * able to convert between player locations and board locations. Here we
     * test these conversions.
     * 
     * @param ludo
     */
    @Test
    public void checkPlayerLocationToBoardLocationConversion() {
        Ludo ludo = new Ludo();
        // The order of the players are RED, BLUE, YELLOW and GREEN

        // For user position 0 we return the first Ludo board grid, then we know we can add one, two and three
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 0), 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.BLUE, 0), 4);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.YELLOW, 0), 8);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.GREEN, 0), 12);

        // Player grid 1 for RED should be 14 for GREEN, 27 for YELLOW and 40 for BLUE
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 1),
                ludo.userGridToLudoBoardGrid(Ludo.GREEN, 14), 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 1),
                ludo.userGridToLudoBoardGrid(Ludo.YELLOW, 27), 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 1),
                ludo.userGridToLudoBoardGrid(Ludo.BLUE, 40), 0);

        // Last field that the piece can interfere with other players (53) should be 16, 29, 42 and 55
        // This should also be the starting positions for the players
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 1), 16, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.BLUE, 1), 29, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.YELLOW, 1), 42, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.GREEN, 1), 55, 0);

        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 53), 16, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.BLUE, 53), 29, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.YELLOW, 53), 42, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.GREEN, 53), 55, 0);

        // First field of the "home stretch" (player position 54) should be 68, 74, 80 and 86
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.RED, 54), 68, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.BLUE, 54), 74, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.YELLOW, 54), 80, 0);
        assertEquals(ludo.userGridToLudoBoardGrid(Ludo.GREEN, 54), 86, 0);
    }

    /**
     * When a player lands on top of an opponents piece that opponents piece
     * should be put back to start.
     * 
     */
    @Test
    public void landingOnTopSendsPlayerBack() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        ludo.throwDice(6); // Lucky red, threw a six
        ludo.movePiece(Ludo.RED, 0, 1); // Board position 16
        ludo.throwDice(6); // Lucky blue as well, threw a six
        ludo.movePiece(Ludo.BLUE, 0, 1); // Board position 29

        assertEquals(1, ludo.getPosition(Ludo.BLUE, 0)); // BLUEs first piece is in play

        ludo.throwDice(6); // One more six
        ludo.movePiece(Ludo.RED, 1, 7); // Board position 22
        ludo.throwDice(6); // One more six
        ludo.movePiece(Ludo.RED, 7, 13); // Board position 28
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 13, 14); // Red ended up on top of blue

        // NOTE, one variant of the rules says that you are safe standing on this field
        // For now, we do not implement this rule

        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0); // It should be blue players turn
        assertEquals(0, ludo.getPosition(Ludo.BLUE, 0)); // This piece should be sent back to home field

        skipPlayer(ludo); // Skip blue player for now
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 14, 15); // Board position 30
        ludo.throwDice(6);
        ludo.movePiece(Ludo.BLUE, 0, 1); // Board position 29
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 15, 16); // Board position 31
        assertEquals(16, ludo.getPosition(Ludo.RED, 0));

        ludo.throwDice(2);
        ludo.movePiece(Ludo.BLUE, 1, 3); // Board position 31, this should send the red piece back to start

        // RED player got "hit" by BLUE player and should be sent back to start
        assertEquals(0, ludo.getPosition(Ludo.RED, 0));
    }

    /**
     * When a player has two or more pieces on top of each other no other player
     * can move past this "tower"
     */
    @Test
    public void towersBlocksOpponents() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        ludo.throwDice(6); // RED is in play
        assertTrue(ludo.movePiece(Ludo.RED, 0, 1));
        skipPlayer(ludo);
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 1, 7));
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 7, 13));
        ludo.throwDice(3);
        assertTrue(ludo.movePiece(Ludo.RED, 13, 16)); // Board position 31
        skipPlayer(ludo);
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 0, 1)); // RED put another piece in play
        skipPlayer(ludo);
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 1, 7));
        ludo.throwDice(5);
        assertTrue(ludo.movePiece(Ludo.RED, 7, 12)); // Board position 27
        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.BLUE, 0, 1)); // Board position 29
        ludo.throwDice(4);
        assertTrue(ludo.movePiece(Ludo.RED, 12, 16)); // RED now have two pieces on 31

        assertEquals(Ludo.BLUE, ludo.activePlayer(), 0);

        ludo.throwDice(5); // Blue have one piece in play, but can not move past REDs tower

        // Since BLUE can not move, the move goes on to RED
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);

        ludo.throwDice(6);
        assertTrue(ludo.movePiece(Ludo.RED, 0, 1)); // RED still has two pieces on 31

        ludo.throwDice(2); // Can not move on top of a tower

        // BLUE can still not move, the move goes on to RED
        assertEquals(Ludo.RED, ludo.activePlayer(), 0);
        ludo.throwDice(2);
        assertTrue(ludo.movePiece(Ludo.RED, 1, 3)); // RED still has two pieces on 31

        ludo.throwDice(1);
        assertTrue(ludo.movePiece(Ludo.BLUE, 1, 2)); // BLUE CAN move 1 (one) place forward

        ludo.throwDice(2);
        assertTrue(ludo.movePiece(Ludo.RED, 16, 18));
        ludo.throwDice(3);
        assertTrue(ludo.movePiece(Ludo.BLUE, 2, 5)); // BLUE will jump over one of REDs pieces and land on another

        int piece0Location = ludo.getPosition(Ludo.RED, 0);
        int piece1Location = ludo.getPosition(Ludo.RED, 1);
        if (piece0Location < piece1Location) { // Either piece 0 or piece 1 should be back to home
            assertEquals(0, piece0Location, 0);
            assertEquals(16, piece1Location, 0);
        } else {
            assertEquals(16, piece0Location, 0);
            assertEquals(0, piece1Location, 0);
        }
        assertEquals(3, ludo.getPosition(Ludo.RED, 2), 0); // Moved on line 405 and 412
        assertEquals(0, ludo.getPosition(Ludo.RED, 3), 0);

        assertEquals(5, ludo.getPosition(Ludo.BLUE, 0));
    }

    /**
     * Use to throw three ones for a player, i.e. if this player has all the
     * pieces in home position it will effectively skip this player.
     * 
     * @param ludo
     *            the object holding this game
     */
    private void skipPlayer(Ludo ludo) {
        for (int noBlue = 0; noBlue < 3; noBlue++) { // We will be moving the red players pieces only
            ludo.throwDice(1); // So blue only throws ones
        }
    }

    /*
     * =========================================================================
     * Below are tests for event listeners that should be implementet in the
     * Ludo logic class. These will help when the Ludo logic is used both on the
     * server and on the client.
     */

    /**
     * Check that correct listener gets called when a dice is thrown. When a
     * dice is thrown then diceThrown should be called on all DiceListeners,
     * with the player and value of the dice in the eventObject.
     */
    @Test
    public void diceThrownEventTest() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        // Create a mock DiceListener
        DiceListener diceListener = mock(DiceListener.class);
        ludo.addDiceListener(diceListener); // Add the dice listener
        ludo.throwDice(6); // RED is in play
        ludo.movePiece(Ludo.RED, 0, 1);
        ludo.throwDice(3); // BLUE is in play

        // Now check that the event has happened in the correct order
        InOrder order = inOrder(diceListener);

        DiceEvent diceEvent = new DiceEvent(ludo, Ludo.RED, 6); // First RED threw a six
        // NOTE!! Requires that DiceEvent implements the equals method from Object !!
        order.verify(diceListener).diceThrown(diceEvent);

        DiceEvent diceEvent1 = new DiceEvent(ludo, Ludo.BLUE, 3); // Then blue threw a three
        order.verify(diceListener).diceThrown(diceEvent1);
    }

    /**
     * Check that correct movement of pieces event is sent. I.e. when a piece is
     * moved, both the piece that is moved and any pieces that is affected by
     * that move should receive separate pieceMoved messages through the
     * registered PieceListener's.
     * 
     * This is the BIG one, once this passes your logic is all sound and good
     */
    @Test
    public void pieceMovedEventTest() {
        Ludo ludo = new Ludo("Player1", "Player2", null, null);

        // Create a mock PieceListener
        PieceListener pieceListener = mock(PieceListener.class);
        ludo.addPieceListener(pieceListener);
        ludo.throwDice(6); // Lucky red, threw a six
        ludo.movePiece(Ludo.RED, 0, 1); // Board position 16
        ludo.throwDice(6); // Lucky blue as well, threw a six
        ludo.movePiece(Ludo.BLUE, 0, 1); // Board position 29

        assertEquals(1, ludo.getPosition(Ludo.BLUE, 0)); // BLUEs first piece is in play

        ludo.throwDice(6); // One more six
        ludo.movePiece(Ludo.RED, 1, 7); // Board position 22
        ludo.throwDice(6); // One more six
        ludo.movePiece(Ludo.RED, 7, 13); // Board position 28
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 13, 14); // Red ended up on top of blue

        // Now check that the event has happened in the correct order
        InOrder order = inOrder(pieceListener);
        PieceEvent pe;
        pe = new PieceEvent(ludo, Ludo.RED, 0, 0, 1); // Red moved piece 0 out from start
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 1, 7);
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 7, 13);
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 13, 14);
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 1, 0); // Blue got pushed back to start
        order.verify(pieceListener).pieceMoved(pe);
    }

    /**
     * Check that we get notified when a new player becomes active. When a
     * player throws the dice and can not move or a player has moved and should
     * not throw again a new player becomes the active player. Check that the
     * correct messages are thrown.
     */
    @Test
    public void checkActivePlayerEventTest() {
        Ludo ludo = new Ludo("Player1", "Player2", "Player3", null);

        // Create a mock DiceListener
        DiceListener diceListener = mock(DiceListener.class);

        // Create a mock PieceListener
        PieceListener pieceListener = mock(PieceListener.class);

        // Create a mock PlayerListener
        PlayerListener playerListener = mock(PlayerListener.class);

        ludo.addDiceListener(diceListener);
        ludo.addPieceListener(pieceListener);
        ludo.addPlayerListener(playerListener);

        // Play a game of Ludo
        // Red and blue get out, yellow is stuck
        ludo.throwDice(6); // Lucky red, threw a six
        ludo.movePiece(Ludo.RED, 0, 1); // 
        ludo.throwDice(6); // Lucky blue as well, threw a six
        ludo.movePiece(Ludo.BLUE, 0, 1); // 
        ludo.throwDice(1);
        ludo.throwDice(2);
        ludo.throwDice(3); // Yellow threw three times, got nowhere

        // Red and blue moving along, yellow gives up after two throws
        ludo.throwDice(6); // Red threw another six
        ludo.movePiece(Ludo.RED, 1, 7);
        ludo.throwDice(1); // Red threw a one
        ludo.movePiece(Ludo.RED, 7, 8);
        ludo.throwDice(1); // Blue threw a one
        ludo.movePiece(Ludo.BLUE, 1, 2);
        ludo.throwDice(4);
        ludo.throwDice(5);
        ludo.removePlayer("Player3"); // Player gives up, with one more throw to go

        // Red and blue moving on (***MOVE3***)
        for (int i = 0; i < 3; i++) {
            ludo.throwDice(6);
            System.out.println("------------------------------------------");
            ludo.movePiece(Ludo.RED, (17 * i) + 8, (17 * i) + 14);
            System.out.println("------------------------------------------");
            ludo.throwDice(6);
            ludo.movePiece(Ludo.RED, (17 * i) + 14, (17 * i) + 20);
            ludo.throwDice(5);
            ludo.movePiece(Ludo.RED, (17 * i) + 20, (17 * i) + 25);
            ludo.throwDice(1);
            ludo.movePiece(Ludo.BLUE, i + 2, i + 3);
        }
        // RED piece 0 has reached finish point, blue piece 0 has reached 5

        // Red move one more out of start, blue moves to 6 (***MOVE4***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 0, 1);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 5, 6);

        // Move red piece 1 towards finish (red moved to 52, blue to 9) (***MOVE5***) 
        for (int i = 0; i < 3; i++) {
            ludo.throwDice(6);
            ludo.movePiece(Ludo.RED, (i * 17) + 1, (i * 17) + 7);
            ludo.throwDice(6);
            ludo.movePiece(Ludo.RED, (i * 17) + 7, (i * 17) + 13);
            ludo.throwDice(5);
            ludo.movePiece(Ludo.RED, (i * 17) + 13, (i * 17) + 18);
            ludo.throwDice(1);
            ludo.movePiece(Ludo.BLUE, i + 6, i + 7);
        }

        // Move red piece 1 to finish, blue to 10 (***MOVE6***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 52, 58);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 58, 59);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 9, 10);

        // Move red piece number three out of start, blue moves to 11 (***MOVE7***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 0, 1);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 10, 11);

        // Move red piece 2 towards finish (red moved to 52, blue to 14) (***MOVE8***) 
        for (int i = 0; i < 3; i++) {
            ludo.throwDice(6);
            ludo.movePiece(Ludo.RED, (i * 17) + 1, (i * 17) + 7);
            ludo.throwDice(6);
            ludo.movePiece(Ludo.RED, (i * 17) + 7, (i * 17) + 13);
            ludo.throwDice(5);
            ludo.movePiece(Ludo.RED, (i * 17) + 13, (i * 17) + 18);
            ludo.throwDice(1);
            ludo.movePiece(Ludo.BLUE, i + 11, i + 12);
        }

        // Move red piece 2 to finish, blue to 15 (***MOVE9***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 52, 58);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 58, 59);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 14, 15);

        // *********** Red has three pieces finished, blue has one piece at 15

        // Move red final piece out of start, blue moves to 16 (***MOVE10***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 0, 1);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 15, 16);

        // Move red final piece to 18, blue to 17 (***MOVE11***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 1, 7);
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 7, 13);
        ludo.throwDice(5);
        ludo.movePiece(Ludo.RED, 13, 18);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 16, 17);

        // Move red final piece to 30 (this is blue 17), knocks blue back to start
        // then move red to 35, blue gets back out of start (***MOVE12***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 18, 24);
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 24, 30); // this knock blue back to start
        ludo.throwDice(5);
        ludo.movePiece(Ludo.RED, 30, 35);
        ludo.throwDice(6);
        ludo.movePiece(Ludo.BLUE, 0, 1);

        // Move red final piece to 52, blue to 2 (***MOVE13***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 35, 41);
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 41, 47);
        ludo.throwDice(5);
        ludo.movePiece(Ludo.RED, 47, 52);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.BLUE, 1, 2);

        // Move red final piece to finish, red wins (***MOVE14***)
        ludo.throwDice(6);
        ludo.movePiece(Ludo.RED, 52, 58);
        ludo.throwDice(1);
        ludo.movePiece(Ludo.RED, 58, 59); // Red wins!

        // *************************************************************************
        // Below is all events we are supposed to receive during the above gameplay
        // *************************************************************************
        InOrder order = inOrder(diceListener, pieceListener, playerListener);
        DiceEvent de;
        PieceEvent pe;
        PlayerEvent ple;
        // Red and blue get out of start, yellow stuck in starting position
        de = new DiceEvent(ludo, Ludo.RED, 6); // First RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 0, 1); // Red moved piece 0 out from start
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.YELLOW, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.YELLOW, 1); // Yellow threw a one
        order.verify(diceListener).diceThrown(de);
        de = new DiceEvent(ludo, Ludo.YELLOW, 2); // Yellow threw a two
        order.verify(diceListener).diceThrown(de);
        de = new DiceEvent(ludo, Ludo.YELLOW, 3); // Yellow threw a three
        order.verify(diceListener).diceThrown(de);
        ple = new PlayerEvent(ludo, Ludo.YELLOW, PlayerEvent.WAITING); // No more throws for YELLOW
        order.verify(playerListener).playerStateChanged(ple);

        // Red and blue moving along, yellow giving up
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING); // Move on to RED
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 1, 7); // Red moved piece from 1 to 7
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 1); // RED threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 0, 7, 8); // Red moved piece from 7 to 8
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 1, 2); // Red moved piece from 1 to 7
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.YELLOW, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.YELLOW, 4); // YELLOW threw a four
        order.verify(diceListener).diceThrown(de);
        de = new DiceEvent(ludo, Ludo.YELLOW, 5); // YELLOW threw a five
        order.verify(diceListener).diceThrown(de);
        ple = new PlayerEvent(ludo, Ludo.YELLOW, PlayerEvent.LEFTGAME);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // Red and blue moving along (***MOVE3***)
        for (int i = 0; i < 3; i++) { // RED piece 0 moves to finish, BLUE piece 0 moves to 5
        	de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 0, (17 * i) + 8, (17 * i) + 14);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 0, (17 * i) + 14, (17 * i) + 20);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a five
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 0, (17 * i) + 20, (17 * i) + 25);
            order.verify(pieceListener).pieceMoved(pe);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
            de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a one
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.BLUE, 0, i + 2, i + 3);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
        }

        // (***MOVE4***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 1, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a two
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 5, 6);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // (***MOVE5***)
        for (int i = 0; i < 3; i++) {
            de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 1, (i * 17) + 1, (i * 17) + 7);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 1, (i * 17) + 7, (i * 17) + 13);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 1, (i * 17) + 13, (i * 17) + 18);
            order.verify(pieceListener).pieceMoved(pe);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
            de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a two
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.BLUE, 0, i + 6, i + 7);
            order.verify(pieceListener).pieceMoved(pe);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
        }

        // (***MOVE6***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 1, 52, 58);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 1); // RED threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 1, 58, 59);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 9, 10);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // (***MOVE7***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 2, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a two
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 10, 11);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // (***MOVE8***)
        for (int i = 0; i < 3; i++) {
            de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 2, (i * 17) + 1, (i * 17) + 7);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 2, (i * 17) + 7, (i * 17) + 13);
            order.verify(pieceListener).pieceMoved(pe);
            de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a six
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.RED, 2, (i * 17) + 13, (i * 17) + 18);
            order.verify(pieceListener).pieceMoved(pe);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
            de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a two
            order.verify(diceListener).diceThrown(de);
            pe = new PieceEvent(ludo, Ludo.BLUE, 0, i + 11, i + 12);
            order.verify(pieceListener).pieceMoved(pe);
            ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
            order.verify(playerListener).playerStateChanged(ple);
            ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
            order.verify(playerListener).playerStateChanged(ple);
        }

        // (***MOVE9***) 
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 2, 52, 58);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 1); // RED threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 2, 58, 59);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 14, 15);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // *********** Red has three pieces finished, blue has one piece at 15
        // (***MOVE10***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a two
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 15, 16);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // (***MOVE11***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 1, 7);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 7, 13);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a five
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 13, 18);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // BLUE threw a one
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 16, 17);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);

        // (***MOVE12***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 18, 24);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 24, 30);
        order.verify(pieceListener).pieceMoved(pe);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 17, 0); // Blue gets knocked back to start
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 30, 35);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 0, 1);
        order.verify(pieceListener).pieceMoved(pe);

        // (***MOVE12***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 35, 41);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 41, 47);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 5); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 47, 52);
        order.verify(pieceListener).pieceMoved(pe);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WAITING);
        order.verify(playerListener).playerStateChanged(ple);
        ple = new PlayerEvent(ludo, Ludo.BLUE, PlayerEvent.PLAYING);
        order.verify(playerListener).playerStateChanged(ple);
        de = new DiceEvent(ludo, Ludo.BLUE, 1); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.BLUE, 0, 1, 2);
        order.verify(pieceListener).pieceMoved(pe);

        // (***MOVE13***)
        de = new DiceEvent(ludo, Ludo.RED, 6); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 52, 58);
        order.verify(pieceListener).pieceMoved(pe);
        de = new DiceEvent(ludo, Ludo.RED, 1); // RED threw a six
        order.verify(diceListener).diceThrown(de);
        pe = new PieceEvent(ludo, Ludo.RED, 3, 59, 59);
        ple = new PlayerEvent(ludo, Ludo.RED, PlayerEvent.WON);
        order.verify(playerListener).playerStateChanged(ple); // HURRAY, we have a winner
    }
}