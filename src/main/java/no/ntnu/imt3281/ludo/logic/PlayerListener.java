package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

/**
 * Listens to players
 * 
 * @author Marius
 * 
 */
public interface PlayerListener extends EventListener {
    /**
     * Abstract function to implement
     * 
     * @param event
     *            The event to handle
     */
    public void playerStateChanged(PlayerEvent event);
}
