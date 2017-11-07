package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

/**
 * A listener for DiceEvents
 * 
 * @author oyste
 *
 */
public interface DiceListener extends EventListener {

    /**
     * Abstract function to implement
     * 
     * @param diceEvent
     *            The event to handle
     */
    void diceThrown(DiceEvent diceEvent);

}
