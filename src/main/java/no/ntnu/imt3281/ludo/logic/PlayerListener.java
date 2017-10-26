package no.ntnu.imt3281.ludo.logic;

import java.util.EventListener;

public interface PlayerListener extends EventListener {
	public void playerStateChanged(PlayerEvent event);
}
