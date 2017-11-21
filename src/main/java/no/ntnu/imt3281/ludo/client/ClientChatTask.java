package no.ntnu.imt3281.ludo.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.ChatWindowController;


/**
 * Handles tasks releated to chat rooms and messages
 * 
 * @author oyste
 *
 */
public class ClientChatTask implements Runnable {
	private static ArrayBlockingQueue<String> chatTasks = new ArrayBlockingQueue<>(256);
	private static final Logger LOGGER = Logger.getLogger(ClientLudoTask.class.getName());
	String currentTask;
	
	@Override
	public void run() {
		while(!Client.socket.isClosed()) {
			try {
				currentTask = chatTasks.take();
				
				handleReceivedChatPacket(currentTask);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
			
		}
	}
	
	/**
	 * Put a new task in queue
	 * 
	 * @param message
	 *            Message to be put in queue
	 */
	public static void addNewTask(String message) {
		
		try {
			chatTasks.put(message);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	private void handleReceivedChatPacket(String message) {
        int tagEndIndex = message.indexOf(":") + 1;
        String tag = message.substring(0, tagEndIndex);
        String ackMessage = message.substring(tagEndIndex);

        switch (tag) {
            case "Say:" :
            	handleSayChatPacket(ackMessage);
                break;
            case "Join:" :
            	Platform.runLater(() -> Client.ludoController.handleServerJoinChat(ackMessage));
                break;
            case "Name:" :
            	handleNameChatPacket(ackMessage);
            	break;
            default :
                break;
        }
    }

	private void handleNameChatPacket(String ackMessage) {
		String[] messages = ackMessage.split(",");
		int chatID = Integer.parseInt(messages[0]);
		String name = messages[1];
		
		ChatWindowController cwc = Client.ludoController.getChatWindowController(chatID);
		if (cwc != null) {
			Platform.runLater(() -> cwc.updateChatNames(name));
		}
	}

	private void handleSayChatPacket(String message) {
		String[] messages = message.split(",");
		int chatID = Integer.parseInt(messages[0]);
		String sayMessage = messages[1];
		
		ChatWindowController cwc = Client.ludoController.getChatWindowController(chatID);
		if (cwc != null) {
			Platform.runLater(() -> cwc.updateChat(sayMessage));
		}
	}
	
	
}
