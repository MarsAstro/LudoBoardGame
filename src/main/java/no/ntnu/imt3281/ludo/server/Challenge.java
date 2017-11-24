package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Challenge
 * 
 * @author Charles The Gentle
 *
 */
public class Challenge {
    int challengeID;
    ArrayList<ClientInfo> clients;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final int TIMEOUT = 10;

    /**
     * Initializes challenge with initial client and challenge id
     * 
     * @param challengeID
     *            This challenge's ID
     * @param client
     *            The client that challenges other clients
     */
    public Challenge(int challengeID, ClientInfo client) {
        this.challengeID = challengeID;
        clients = new ArrayList<>();
        clients.add(client);
        scheduler.scheduleAtFixedRate(() -> {
            ChallengeTimeoutTask.blockingPut(this);
            scheduler.shutdown();
        }, TIMEOUT, 10, TimeUnit.SECONDS);
    }
}
