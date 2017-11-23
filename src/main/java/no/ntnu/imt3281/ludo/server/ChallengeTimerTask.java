package no.ntnu.imt3281.ludo.server;

import java.util.TimerTask;

/**
 * 
 * @author Charles The Gentle
 *
 */
public class ChallengeTimerTask extends TimerTask {

    Challenge challengeRef;
    private int seconds = 0;
    private final int TIMEOUT = 5; 

    /**
     * Initializes timer task
     */
    ChallengeTimerTask(Challenge challengeRef) {
        this.challengeRef = challengeRef;
    }

    @Override
    public void run() {
        seconds++;
        if (seconds >= TIMEOUT) {
            ChallengeTimeoutTask.blockingPut(challengeRef);
        }
    }

}
