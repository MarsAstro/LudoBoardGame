package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;

/**
 * Dunno man
 * @author oyste
 *
 */
public class DiceEvent extends EventObject implements DiceListener {

	private int player;
	private int dice;
	
	public DiceEvent(Ludo ludo) {
		super(ludo);
	}
	public DiceEvent(Ludo ludo, int player, int dice) {
		super(ludo);
		this.player = player;
		this.dice = dice;
	}

	@Override
	public boolean equals(Object other) {
		try {
			DiceEvent event = (DiceEvent) other;
			return player == event.player && dice == event.dice;
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void diceThrown(DiceEvent diceEvent) {
		player = diceEvent.player;
		dice = diceEvent.dice;
	}
	
	@Override
	public String toString() {
		return new String(player + ", " + dice);
	}
}
