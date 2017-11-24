/**
 * Selfmade Skeleton for 'Login.fxml' Controller Class
 */

package no.ntnu.imt3281.ludo.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.ntnu.imt3281.ludo.client.Client;

/**
 * Handles logging in and registering the user and connecting to server
 * 
 * @author Marius
 *
 */
public class ConnectController implements Initializable {
    private ResourceBundle messages;
    private static final Logger LOGGER = Logger.getLogger(ConnectController.class.getName());
    private String charSet = "UTF-8";

    @FXML // fx:id="IPAddress"
    private TextField ipAddress;

    @FXML // fx:id="username"
    private TextField username;

    @FXML // fx:id="password"
    private PasswordField password;

    @FXML // fx:id="errorMessage"
    private Label errorMessage;

    @FXML // fx:id="rememberMe"
    private CheckBox rememberMe;

    private String[] detailsFileNames = {"details\\ipdetails.txt", "details\\userdetails.txt",
            "details\\passdetails.txt"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;

        File[] files = {new File(detailsFileNames[0]), new File(detailsFileNames[1]),
                new File(detailsFileNames[2])};

        if (files[0].exists() && files[1].exists() && files[2].exists()) {
            rememberMe.setSelected(true);
            try {
                byte[][] bytes = {new byte[16], new byte[16], new byte[16]};

                readDetailsFiles(files, bytes);

                String ip = new String(bytes[0], charSet);

                ipAddress.setText(ip);
                username.setText(decrypt(bytes[1]));
                password.setText(decrypt(bytes[2]));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private void readDetailsFiles(File[] files, byte[][] bytes) throws IOException {
        for (int fileIndex = 0; fileIndex < 3; fileIndex++) {
            FileInputStream readFile = new FileInputStream(files[fileIndex]);
            int length = readFile.read(bytes[fileIndex]);
            if (fileIndex > 0 && length != 16) {
                LOGGER.log(Level.INFO, "Userdetails file wrong size");
            }
            readFile.close();
        }
    }

    @FXML
    void cancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    void login(ActionEvent event) {
        if (allFieldsValid()) {
            try {
                Client.connectToServer(InetAddress.getByName(ipAddress.getText()));

                Client.sendMessage("User.Login:" + username.getText() + "," + password.getText());
            } catch (UnknownHostException e) {
                errorMessage.setText(messages.getString("login.result.nohost"));
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }

        } else {
            errorMessage.setText(messages.getString("login.fill"));
        }
    }

    @FXML
    void register(ActionEvent event) {
        if (allFieldsValid()) {
            try {
                Client.connectToServer(InetAddress.getByName(ipAddress.getText()));

                Client.sendMessage(
                        "User.Register:" + username.getText() + "," + password.getText());
            } catch (UnknownHostException e) {
                errorMessage.setText(messages.getString("login.result.nohost"));
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }

        } else {
            errorMessage.setText(messages.getString("login.fill"));
        }
    }

    private boolean allFieldsValid() {
        return !(ipAddress.getText().isEmpty() || username.getText().isEmpty()
                || password.getText().isEmpty());
    }

    /**
     * Handles the servers response to a login attempt
     * 
     * @param ackMessage
     *            The servers response
     */
    public void handleServerLoginResponse(String ackMessage) {
        switch (Integer.parseInt(ackMessage)) {
            case 1 :
                Platform.runLater(
                        () -> Client.getLudoController().userLoggedIn(username.getText()));
                closeWindow();
                break;
            case -1 :
                errorMessage.setText(messages.getString("login.result.failed"));
                break;
            case -2 :
                errorMessage.setText(messages.getString("login.result.sneaky"));
                break;
            default :
                errorMessage.setText(messages.getString("login.result.unknown"));
                break;
        }
    }

    private void closeWindow() {
        if (allFieldsValid()) {
            try (FileOutputStream ipFile = new FileOutputStream(detailsFileNames[0], false);
                    FileOutputStream userFile = new FileOutputStream(detailsFileNames[1], false);
                    FileOutputStream passFile = new FileOutputStream(detailsFileNames[2], false)) {
                if (rememberMe.isSelected()) {
                    ipFile.write(ipAddress.getText().getBytes(charSet));
                    userFile.write(encrypt(username.getText()));
                    passFile.write(encrypt(password.getText()));
                } else {
                    cleanupFiles(ipFile, userFile, passFile);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }

        Stage stage = (Stage) username.getScene().getWindow();
        stage.close();
    }

    private void cleanupFiles(FileOutputStream ipFile, FileOutputStream userFile,
            FileOutputStream passFile) throws IOException {
        ipFile.close();
        userFile.close();
        passFile.close();
        for (int index = 0; index < 3; index++) {
            boolean deleted = new File(detailsFileNames[index]).delete();
            if (!deleted) {
                LOGGER.log(Level.INFO, "Failed to clean up old user details");
            }
        }
    }

    /**
     * Handles the servers response to a register attempt
     * 
     * @param ackMessage
     *            The servers response
     */
    public void handleServerRegisterResponse(String ackMessage) {
        switch (Integer.parseInt(ackMessage)) {
            case 1 :
                Platform.runLater(
                        () -> Client.getLudoController().userLoggedIn(username.getText()));
                closeWindow();
                break;
            case -1 :
                errorMessage.setText(messages.getString("login.result.already"));
                break;
            case -2 :
                errorMessage.setText(messages.getString("login.result.usersyntax") + " "
                        + messages.getString("login.result.validsyntax"));
                break;
            case -3 :
                errorMessage.setText(messages.getString("login.result.pwdsyntax") + " "
                        + messages.getString("login.result.validsyntax"));
                break;
            default :
                break;
        }
    }

    private byte[] encrypt(String message) {
        byte[] result = null;

        String key = "Bar12345Bar12345";

        Cipher cipher;
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(charSet), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            result = cipher.doFinal(message.getBytes(charSet));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException
                | UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return result;
    }

    private String decrypt(byte[] message) {
        String result = null;

        String key = "Bar12345Bar12345";

        Cipher cipher;
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(charSet), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            result = new String(cipher.doFinal(message), charSet);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException
                | UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return result;
    }
}
