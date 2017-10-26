package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;

public class PlayerEvent extends EventObject implements PlayerListener {

	public static final int PLAYING = 0;
	public static final int WAITING = 1;
	public static final int LEFTGAME = 2;
	public static final int WON = 3;
	private int activePlayer;
	private int state;
	
	public PlayerEvent(Ludo ludo) {
		super(ludo);
	}
	
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
		try {
			PlayerEvent event = (PlayerEvent)other;
			return activePlayer == event.activePlayer && state == event.state;
		}
		catch (Exception e) {
			// Is different object
		}
		return false;
	}

	@Override
	public String toString() {
		return new String(activePlayer + ", " + state);
	}
}
