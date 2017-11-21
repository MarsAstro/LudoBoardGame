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

				int endIndex = currentTask.indexOf(":");
				String tag = currentTask.substring(0, endIndex);

				handleMessage(tag, currentTask.substring(endIndex));

			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}

		}
	}

	/*
	 * Messages: Chat.Join:ChatID,ClientID
	 * 
	 * Chat.List:
	 * 
	 * Chat.Say:ChatID,ClientID,Message
	 */

	private void handleMessage(String tag, String message) {
		switch (tag) {
		case "Join":
			handleJoinChatPacket(message);
			break;
		case "List":
			handleListChatPacket(message);
			break;
		case "Say":
			handleSayChatPacket(message);
			break;
		default:
			break;
		}

	}

	private void handleSayChatPacket(String message) {
		String[] messages = message.split(",");
		int chatID = Integer.parseInt(messages[0]);
		int clientID = Integer.parseInt(messages[1]);
		String sayMessage = messages[2];

		int clientConnectionID = Server.connections.indexOf(new ClientInfo(clientID));

		if (clientConnectionID >= 0) {
			int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

			if (chatIndex >= 0) {
				ChatInfo chat = Server.chats.get(chatIndex);
				int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

				if (clientIndex == -1) {
					chat.say(sayMessage);
					for (ClientInfo client : chat.clients) {
						SendToClientTask.send(client.clientID + ".Chat.Say:" + chatID + "," + sayMessage);
					}
				}
			}

		}
	}

	private void handleListChatPacket(String message) {
		// TODO
		// Open Tab with active chats
	}

	private void handleJoinChatPacket(String message) {
		String[] messages = message.split(",");
		int chatID = Integer.parseInt(messages[0]);
		int clientID = Integer.parseInt(messages[1]);

		int clientConnectionID = Server.connections.indexOf(new ClientInfo(clientID));

		if (clientConnectionID >= 0) {
			int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

			if (chatIndex >= 0) {
				ChatInfo chat = Server.chats.get(chatIndex);
				int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

				if (clientIndex == -1) {
					ClientInfo addedClient = Server.connections.get(clientConnectionID);
					chat.addClient(addedClient);
					for (ClientInfo client : chat.clients) {
						SendToClientTask.send(client.clientID + ".Chat.Say:" + chatID + "," + addedClient.username
								+ " has joined the lobby!");
					}
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
