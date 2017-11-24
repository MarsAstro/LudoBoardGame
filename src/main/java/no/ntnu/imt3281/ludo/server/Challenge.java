package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data structure containing information about a challenge. Times out by itself,
 * and notifies ChallengeTimeoutTask.
 * 
 * @author Charles The Gentle
 *
 */
public class Challenge {
    ArrayList<ClientInfo> clients;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int TIMEOUT = 30;

    /**
     * Initializes challenge with initial client and challenge id
     * 
     * @param client
     *            The client that challenges other clients
     */
    public Challenge(ClientInfo client) {
        clients = new ArrayList<>();
        clients.add(client);
        scheduler.scheduleAtFixedRate(() -> {
            ChallengeTimeoutTask.blockingPut(this);
            scheduler.shutdown();
        }, TIMEOUT, 10, TimeUnit.SECONDS);
    }
}
