package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * I know, man
 * 
 * @author oyste
 *
 */
public class DiceEvent extends EventObject implements DiceListener {
    private static final Logger LOGGER = Logger.getLogger(DiceEvent.class.getName());
    private int player;
    private int dice;

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     */
    public DiceEvent(Ludo ludo) {
        super(ludo);
    }

    /**
     * A constructor
     * 
     * @param ludo
     *            The gameboard to construct this event with
     * @param player
     *            The player index
     * @param dice
     *            The thrown dice
     */
    public DiceEvent(Ludo ludo, int player, int dice) {
        super(ludo);
        this.player = player;
        this.dice = dice;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other == null || this.getClass() != other.getClass())) {
            try {
                DiceEvent event = (DiceEvent) other;
                return player == event.player && dice == event.dice;
            } catch (RuntimeException e) {
                LOGGER.warning(e.getMessage());
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (player + dice) * 17 % 1025;
    }

    @Override
    public void diceThrown(DiceEvent diceEvent) {
        player = diceEvent.player;
        dice = diceEvent.dice;
    }

    @Override
    public String toString() {
        return player + ", " + dice;
    }
}
