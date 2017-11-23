package no.ntnu.imt3281.ludo.client;

import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import no.ntnu.imt3281.ludo.gui.GameBoardController;

/**
 * Handles all tasks related to ludo games ongoing and starting
 * 
 * @author oyste
 *
 */
public class ClientLudoTask implements Runnable {
	private static final ResourceBundle messages = ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n");
	private static ArrayBlockingQueue<String> ludoTasks = new ArrayBlockingQueue<>(256);
	private static final Logger LOGGER = Logger.getLogger(ClientLudoTask.class.getName());
	String currentTask;

	@Override
	public void run() {
		while (!Client.socket.isClosed()) {
			try {
				currentTask = ludoTasks.take();

				handleReceivedLudoPacket(currentTask);
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
			ludoTasks.put(message);
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void handleReceivedLudoPacket(String message) {

		int tagEndIndex = message.indexOf(":") + 1;
		String tag = message.substring(0, tagEndIndex);
		String ackMessage = message.substring(tagEndIndex);

		switch (tag) {
		case "Dice:":
			handleReceivedLudoDice(ackMessage);
			break;
		case "Piece:":
			handleReceivedLudoPiece(ackMessage);
			break;
		case "Player:":
			handleReceivedLudoPlayer(ackMessage);
			break;
		case "Name:":
			handleReceivedLudoNamePacket(ackMessage);
			break;
		case "Challenge:":
		    handleReceivedLudoChallengePacket(ackMessage);
			break;
		case "ChallengeConfirm:":
            handleReceivedLudoChallengeConfirmPacket(ackMessage);
            break;
		case "JoinRandom:":
			Platform.runLater(() -> Client.ludoController.handleServerJoinRandomGame(ackMessage));
			break;
		case "Join:":
			Platform.runLater(() -> Client.ludoController.handleServerJoinGame(ackMessage));
			break;
		case "RandomSuccess:":
			Platform.runLater(() -> Client.ludoController.JoinRandomSuccess());
			break;
		case "Chat:":
			HandleRecivedLudoChatPackage(ackMessage);
			break;
		default:
			break;
		}
	}

	private void handleReceivedLudoChallengeConfirmPacket(String ackMessage) {
        
    }

    private void handleReceivedLudoChallengePacket(String ackMessage) {
        Platform.runLater(() -> Client.ludoController.handleServerChallengeGame(ackMessage));
    }

    private void HandleRecivedLudoChatPackage(String ackMessage) {
		int endGameIDIndex = ackMessage.indexOf(",");
		int gameID = Integer.parseInt(ackMessage.substring(0, endGameIDIndex));
		GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
		if (gbc != null) {
			Platform.runLater(() -> gbc.addMessage(ackMessage.substring(endGameIDIndex + 1)));
		}
	}

	private void handleReceivedLudoDice(String ackMessage) {
		String[] messageInfos = ackMessage.split(",");
		int gameID = Integer.parseInt(messageInfos[0]);
		int playerIndex = Integer.parseInt(messageInfos[1]);
		int dice = Integer.parseInt(messageInfos[2]);
		boolean canMove = Boolean.parseBoolean(messageInfos[3]);

		GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
		Platform.runLater(() -> gbc.updateDice(playerIndex, dice, canMove));
	}

	private void handleReceivedLudoPlayer(String ackMessage) {
		String[] messageInfos = ackMessage.split(",");
		int gameID = Integer.parseInt(messageInfos[0]);
		int playerIndex = Integer.parseInt(messageInfos[1]);
		int playerState = Integer.parseInt(messageInfos[2]);

		GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
		if (gbc != null) {
			Platform.runLater(() -> gbc.updateActivePlayer(playerIndex, playerState));
		}
	}

	private void handleReceivedLudoNamePacket(String message) {
		String[] messageInfos = message.split(",");

		int gameID = Integer.parseInt(messageInfos[0]);
		int playerIndex = Integer.parseInt(messageInfos[1]);
		String name = messageInfos[2];

		if (name.startsWith("Discard")) {
			name = messages.getString("ludogameboard.noplayer");
		} else if (name.startsWith("Inactive")) {
			name = name.replaceFirst("Inactive", messages.getString("ludogameboard.inactive"));
		}

		// Run later expects a string that is "effectively final", so it only
		// accepts a variable that has been assigned to once
		String newName = name;

		GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
		if (gbc != null) {
			Platform.runLater(() -> gbc.updateName(newName, playerIndex));
		}
	}

	private void handleReceivedLudoPiece(String ackMessage) {
		String[] messageInfos = ackMessage.split(",");
		int gameID = Integer.parseInt(messageInfos[0]);
		int playerID = Integer.parseInt(messageInfos[1]);
		int piece = Integer.parseInt(messageInfos[2]);
		int from = Integer.parseInt(messageInfos[3]);
		int to = Integer.parseInt(messageInfos[4]);

		GameBoardController gbc = Client.ludoController.getGameBoardController(gameID);
		Platform.runLater(() -> gbc.updatePiece(playerID, piece, from, to));
	}
}
