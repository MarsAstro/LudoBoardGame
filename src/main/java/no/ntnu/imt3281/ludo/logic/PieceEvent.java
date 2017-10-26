package no.ntnu.imt3281.ludo.logic;

import java.util.EventObject;

public class PieceEvent extends EventObject implements PieceListener {

	private int player;
	private int piece;
	private int from;
	private int to;
	
	public PieceEvent(Ludo ludo) {
		super(ludo);
	}
	
	public PieceEvent(Ludo ludo, int player, int piece, int from, int to) {
		super(ludo);
		this.player = player;
		this.piece = piece;
		this.from = from;
		this.to = to;
	}

	@Override
	public void pieceMoved(PieceEvent event) {
		player = event.player;
		piece = event.piece;
		from = event.piece;
		to = event.to;
	}
	
	@Override
	public boolean equals(Object other) {
		try {
			PieceEvent event = (PieceEvent)other;
			return player == event.player && piece == event.piece && from == event.from && to == event.to;
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return new String(player + ", " + piece + ", " + from + ", " + to);
	}
}
