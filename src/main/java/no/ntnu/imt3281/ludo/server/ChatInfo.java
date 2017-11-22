package no.ntnu.imt3281.ludo.server;

import java.util.ArrayList;

public class ChatInfo {
	int chatID;
	ArrayList<ClientInfo> clients;

	/**
	 * Only initializes the chatID field, used for language-specific search methods
	 * 
	 * @param chatID
	 *            The ID of the chat to look for
	 */
	ChatInfo(int chatID) {
		this.chatID = chatID;
		clients = new ArrayList<>();
	}

	/**
	 * Initializes all fields and adds the client to the new chat
	 * 
	 * @param chatID
	 *            The ID of the new chat
	 * @param client
	 *            The Client initiating the new game
	 */
	ChatInfo(int chatID, ClientInfo client) {
		this.chatID = chatID;
		clients = new ArrayList<>();
		clients.add(client);
	}

	@Override
	public boolean equals(Object other) {
		boolean isEqual = false;

		if (other instanceof ChatInfo) {
			ChatInfo chatInfo = (ChatInfo) other;
			isEqual = chatID == chatInfo.chatID;
		}

		return isEqual;
	}

	@Override
	public int hashCode() {
		return chatID * 46 - 17 % 112;
	}

	/**
	 * Adding player to chat
	 * 
	 * @param client
	 *            The client to be added
	 */
	public void addClient(ClientInfo client) {
		clients.add(client);
	}

	/**
	 * Sends message to chat
	 * 
	 * @param message
	 *            The message sent to chat
	 */
	public void say(String sayMessage) {
		for (ClientInfo client : clients) {
			SendToClientTask.send(client.clientID + ".Chat.Say:" + chatID + "," + sayMessage);
		}
	}

	/**
	 * Removing client from chat
	 * 
	 * @param clientID
	 *            The client to be removed
	 * @return Return the new name of the client removed
	 */
	public String removeClient(int clientID) {
		String returnName = "";
		int index = clients.indexOf(new ClientInfo(clientID));
		if (index >= 0) {
			ClientInfo clientToRemove = clients.get(index);
			returnName = clientToRemove.username;
			clients.remove(index);
		}

		return returnName;
	}

	/**
	 * Gets ClientInfo from ID
	 * 
	 * @param clientID
	 *            ID of client to be searched for
	 * @return Client info or null if player doesn't exist
	 */
	public ClientInfo getClient(int clientID) {
		int index = clients.indexOf(new ClientInfo(clientID));
		return index == -1 ? null : clients.get(index);
	}
}
