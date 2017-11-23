package no.ntnu.imt3281.ludo.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChallengeTimeoutTask implements Runnable {
    private static ArrayBlockingQueue<Challenge> challengeTasks = new ArrayBlockingQueue<>(256);
    private static final Logger LOGGER = Logger.getLogger(ChallengeTimeoutTask.class.getName());
    private Challenge currentChallenge; 
    
    @Override
    public void run() {
        while (!Server.serverSocket.isClosed()) {
            try {
                currentChallenge = challengeTasks.take();
                
                LudoTask.challengesLock.writeLock().lock();
                for (int i = 0; i < currentChallenge.clientIDs.size(); i++) {
                    SendToClientTask.send(currentChallenge.clientIDs.get(i) + ".Ludo.ChallengeTimedOut:");
                }
                LudoTask.challenges.remove(currentChallenge);
                LudoTask.challengesLock.writeLock().unlock();

            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Put a new task in queue
     * 
     * @param challengeRef
     *            Challenge to be put in queue
     */
    public static void blockingPut(Challenge challengeRef) {
        try {
            challengeTasks.put(challengeRef);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
