package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

/**
 * Listens to piece events.
 * 
 * @author Marius
 *
 */
public interface PieceListener extends EventListener {
    /**
     * Abstract function to implement
     * 
     * @param event
     *            The event to handle
     */
    public void pieceMoved(PieceEvent event);
}
