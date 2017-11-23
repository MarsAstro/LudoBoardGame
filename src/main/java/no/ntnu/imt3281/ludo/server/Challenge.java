package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

public class Challenge {
    int challengeID;
    ArrayList<Integer> clientIDs;
    
    /**
     * Initialize challenge
     */
    public Challenge(int challengeID, int clientID) {
        this.challengeID = challengeID;
        clientIDs = new ArrayList<>();
        clientIDs.add(clientID);
    }
}
