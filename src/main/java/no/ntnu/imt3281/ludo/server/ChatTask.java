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
		int endIndex = message.indexOf(":");
		String tag = message.substring(0, endIndex + 1);

		switch (tag) {
		case "Join:":
			handleJoinChatPacket(clientID, message.substring(endIndex + 1));
			break;
		case "List:":
			handleListChatPacket(message.substring(endIndex + 1));
			break;
		case "Say:":
			handleSayChatPacket(clientID, message.substring(endIndex + 1));
			break;
		case "Leave:":
			handleLeaveChatPacket(clientID, message.substring(endIndex + 1));
			break;
		case "Init:":
			handleInitChatPacket(clientID, message.substring(endIndex + 1));
			break;
		case "InitGlobal:":
			handleInitGlobalChatPacket(clientID, message.substring(endIndex + 1));
			break;
		default:
			break;
		}
	}

	private void handleLeaveChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		
		Server.chatLock.writeLock().lock();
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));
		
		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));
			if (clientIndex >= 0) {
				String clientToRemoveName = chat.clients.get(clientIndex).username;
				
				chat.removeClient(clientIndex);
				
				for (ClientInfo client : chat.clients) {
					SendToClientTask.send(client.clientID + ".Chat.RemoveName:" + chatID + "," + clientToRemoveName);
				}
			}
		}
		Server.chatLock.writeLock().unlock();
	}

	private void handleInitGlobalChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		
		Server.chatLock.writeLock().lock();
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			
			Server.clientLock.readLock().lock();
			int clientConnection = Server.clients.indexOf(new ClientInfo(clientID));

			if (clientConnection >= 0) {
				ClientInfo newClient = Server.clients.get(clientConnection);
				
				for (ClientInfo client : chat.clients) {
					SendToClientTask.send(newClient.clientID + ".Chat.Name:" + chatID + "," + client.username);
				}
				
				chat.addClient(newClient);
				
				for (ClientInfo client : chat.clients) {
					SendToClientTask.send(client.clientID + ".Chat.Name:" + chatID + "," + newClient.username);
				}
			}
			Server.clientLock.readLock().unlock();
		}
		Server.chatLock.writeLock().unlock();
	}

	private void handleInitChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		
		Server.chatLock.readLock().lock();
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);

			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));
			if (clientIndex >= 0) {
				ClientInfo clientToInit = chat.clients.get(clientIndex);
				for (ClientInfo client : chat.clients) {
					if (!clientToInit.equals(client)) {
						SendToClientTask.send(clientToInit.clientID + ".Chat.Name:" + chatID + "," + client.username);
					}
				}
			}
		}
		Server.chatLock.readLock().unlock();
	}

	private void handleSayChatPacket(int clientID, String message) {
		String[] messages = message.split(",");
		int chatID = Integer.parseInt(messages[0]);
		String sayMessage = messages[1];

		Server.chatLock.readLock().lock();
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

			if (clientIndex >= 0) {
				Server.clientLock.readLock().lock();
				int clientConnection = Server.clients.indexOf(new ClientInfo(clientID));

				if (clientConnection >= 0) {
					String talkingClientName = Server.clients.get(clientConnection).username;
					chat.say(talkingClientName + ": " + sayMessage);
				}
				Server.clientLock.readLock().unlock();
			}
		}
		Server.chatLock.readLock().unlock();
	}

	private void handleListChatPacket(String message) {
		// TODO
		// Open Tab with active chats
	}

	private void handleJoinChatPacket(int clientID, String message) {
		int chatID = Integer.parseInt(message);
		
		Server.chatLock.writeLock().lock();
		int chatIndex = Server.chats.indexOf(new ChatInfo(chatID));

		if (chatIndex >= 0) {
			ChatInfo chat = Server.chats.get(chatIndex);
			int clientIndex = chat.clients.indexOf(new ClientInfo(clientID));

			if (clientIndex == -1) {
				Server.clientLock.readLock().lock();
				int clientConnection = Server.clients.indexOf(new ClientInfo(clientID));

				if (clientConnection >= 0) {
					ClientInfo addedClient = Server.clients.get(clientConnection);
					
					chat.addClient(addedClient);
					
					SendToClientTask.send(addedClient.clientID + ".Chat.Join:" + chatID);
					for (ClientInfo client : chat.clients) {
						SendToClientTask.send(client.clientID + ".Chat.Name:" + chatID + "," + addedClient.username);
					}
				}
				Server.clientLock.readLock().unlock();
			}
		}
		Server.chatLock.writeLock().unlock();
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
