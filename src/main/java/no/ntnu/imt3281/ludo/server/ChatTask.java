package no.ntnu.imt3281.ludo.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles tasks related to chat actions
 * 
 * @author Marius
 *
 */
public class ChatTask implements Runnable {
	private static ArrayBlockingQueue<String> chatTasks = new ArrayBlockingQueue<>(256);
	private static final Logger LOGGER = Logger.getLogger(ChatTask.class.getName());
	String currentTask;

	@Override
	public void run() {
		while (!Server.serverSocket.isClosed()) {
			try {
				currentTask = chatTasks.take();

				int endIDIndex = currentTask.indexOf(".");
				int clientID = Integer.parseInt(currentTask.substring(0, endIDIndex));
				handleMessage(clientID, currentTask.substring(endIDIndex + 1));

			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}

		}
	}

	private void handleMessage(int clientID, String message) {
		int endIndex = currentTask.indexOf(":");
		String tag = currentTask.substring(0, endIndex);

		switch (tag) {
		case "Join:":
			handleJoinChatPacket(clientID, message);
			break;
		case "List:":
			handleListChatPacket(message);
			break;
		case "Say:":
			handleSayChatPacket(clientID, message);
			break;
		case "Init:":
			handleInitChatPacket(clientID, message);
			break;
		default:
			break;
		}
	}

	private void handleInitChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);

			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));
			if (clientIndex >= 0) {
				ClientInfo clientToInit = chat.clients.get(clientIndex);
				for (ClientInfo client : chat.clients) {
					if (!clientToInit.equals(client)) {
						SendToClientTask.send("Chat.Name:" + chatID + "," + client.username);
					}
				}
			}
		}
	}

	private void handleSayChatPacket(int clientID, String message) {
		String[] messages = message.split(",");
		int chatID = Integer.parseInt(messages[0]);
		String sayMessage = messages[1];

		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

			if (clientIndex >= 0) {
				String talkingClientName = Server.connections.get(clientID).username;
				chat.say(sayMessage);
				for (ClientInfo client : chat.clients) {
					SendToClientTask
							.send(client.clientID + ".Chat.Say:" + chatID + "," + talkingClientName + ": " + sayMessage);
				}
			}
		}
	}

	private void handleListChatPacket(String message) {
		// TODO
		// Open Tab with active chats
	}

	private void handleJoinChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

			if (clientIndex == -1) {
				ClientInfo addedClient = Server.connections.get(clientID);
				chat.addClient(addedClient);
				SendToClientTask.send(addedClient.clientID + ".Chat.Join:" + chatID);
				for (ClientInfo client : chat.clients) {
					SendToClientTask.send(client.clientID + ".Chat.Name:" + chatID + "," + addedClient.username);
				}
			}
		}

	}

	/**
	 * Put a new task in queue
	 * 
	 * @param message
	 *            Message to be put in queue
	 */
	public static void blockingPut(String message) {
		try {
			chatTasks.put(message);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
}
