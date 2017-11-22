package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * 
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Server extends Application {
	static ServerSocket serverSocket;
	static Connection database;
	static ServerGUIController serverGUIController;
	static ArrayList<ClientInfo> clients;
	static ArrayList<GameInfo> games;
	static ArrayList<ChatInfo> chats;
	static int nextGameID;
	static int nextChatID = 0;
	static ReadWriteLock clientLock;
	static ReadWriteLock gameLock;
	static ReadWriteLock chatLock;

	private static String url = "jdbc:mysql://mysql.stud.ntnu.no/mksandbe_Ludo";
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

	/**
	 * Sets port number and opens the server GUI
	 */
	public static void initServer() {
		try {
			serverSocket = new ServerSocket(9003, 256);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}

		ResourceBundle messages = ResourceBundle.getBundle("no.ntnu.imt3281.i18n.i18n");

		chats = new ArrayList<>();
		chats.add(new ChatInfo(nextChatID++, messages.getString("chat.global")));
		clients = new ArrayList<>();
		games = new ArrayList<>();
		nextGameID = 0;
		messages = ResourceBundle.getBundle("no.ntnu.imt3281.ludo.server.credentials");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			database = DriverManager.getConnection(url, messages.getString("dbuser"), messages.getString("dbpass"));
		} catch (SQLException | ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}

	}

	/**
	 * @see javafx.application.Application
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("./ServerGUI.fxml"));
			GridPane root = (GridPane) loader.load();

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(e -> System.exit(0));

			serverGUIController = loader.getController();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Sets server to wait for packets
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		initServer();

		clientLock = new ReentrantReadWriteLock();
		gameLock = new ReentrantReadWriteLock();
		chatLock = new ReentrantReadWriteLock();

		ExecutorService executorService = Executors.newCachedThreadPool();

		UserTask userTask = new UserTask();
		LudoTask ludoTask = new LudoTask();
		ChatTask chatTask = new ChatTask();
		ServerInputTask inputTask = new ServerInputTask();
		SendToClientTask sendTask = new SendToClientTask();
		UserCleanupTask cleanupTask = new UserCleanupTask();
		ClientConnectionTask clientConTask = new ClientConnectionTask();

		executorService.execute(userTask);
		executorService.execute(ludoTask);
		executorService.execute(chatTask);
		executorService.execute(inputTask);
		executorService.execute(sendTask);
		executorService.execute(cleanupTask);
		executorService.execute(clientConTask);
		executorService.shutdown();

		launch(args);
	}
}
