package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

public interface PieceListener extends EventListener {
	public void pieceMoved(PieceEvent event);
}
