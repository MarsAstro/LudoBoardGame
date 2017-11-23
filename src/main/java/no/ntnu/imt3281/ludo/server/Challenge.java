package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Challenge
 * 
 * @author Charles The Gentle
 *
 */
public class Challenge {
    int challengeID;
    ArrayList<Integer> clientIDs;
    private Timer timer;

    /**
     * Initializes challenge with initial client and challenge id
     * 
     * @param challengeID
     *            This challenge's ID
     * @param clientID
     *            The client that challenges other clients
     */
    public Challenge(int challengeID, int clientID) {
        this.challengeID = challengeID;
        clientIDs = new ArrayList<>();
        clientIDs.add(clientID);
        timer = new Timer();
        timer.schedule(new ChallengeTimerTask(this), 1000, 1000);
    }
}
