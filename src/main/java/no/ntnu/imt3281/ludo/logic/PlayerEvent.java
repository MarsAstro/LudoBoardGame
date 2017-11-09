package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A player event
 * 
 * @author Marius
 *
 */
public class PlayerEvent extends EventObject implements PlayerListener {
    private static final Logger LOGGER = Logger.getLogger(PlayerEvent.class.getName());

    /**
     * Constant for the Playing state
     */
    public static final int PLAYING = 0;

    /**
     * Constant for the Waiting state
     */
    public static final int WAITING = 1;

    /**
     * Constant for the LeftGame state
     */
    public static final int LEFTGAME = 2;

    /**
     * Constant for the WON state
     */
    public static final int WON = 3;

    private int activePlayer;
    private int state;

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     */
    public PlayerEvent(Ludo ludo) {
        super(ludo);
    }

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     * @param activePlayer
     *            The current player
     * @param state
     *            The current state
     */
    public PlayerEvent(Ludo ludo, int activePlayer, int state) {
        super(ludo);
        this.activePlayer = activePlayer;
        this.state = state;
    }

    @Override
    public void playerStateChanged(PlayerEvent event) {
        activePlayer = event.activePlayer;
        state = event.state;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other == null || this.getClass() != other.getClass())) {
            try {
                PlayerEvent event = (PlayerEvent) other;
                return activePlayer == event.activePlayer && state == event.state;
            } catch (RuntimeException e) {
                LOGGER.warning(e.getMessage());
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (activePlayer / state) * 13 % 721;
    }

    @Override
    public String toString() {
        return activePlayer + ", " + state;
    }
}
