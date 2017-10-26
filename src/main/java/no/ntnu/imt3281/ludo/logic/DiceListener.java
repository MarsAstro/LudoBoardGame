package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

/**
 * ayyyyy
 * @author oyste
 *
 */
public interface DiceListener extends EventListener {

	void diceThrown(DiceEvent diceEvent);

}
