/**
 * Selfmade Skeleton for 'Login.fxml' Controller Class
 */

package no.ntnu.imt3281.ludo.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * Handles logging the user in and connecting to server
 * 
 * @author Marius
 *
 */
public class ConnectController implements Initializable {
    private ResourceBundle messages;
    private static final Logger LOGGER = Logger.getLogger(ConnectController.class.getName());

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

    private String ipFileName = "ipdetails.txt";
    private String userFileName = "userdetails.txt";
    private String passFileName = "passdetails.txt";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = resources;

        File ipFile = new File(ipFileName);
        File userFile = new File(userFileName);
        File passFile = new File(passFileName);

        if ((ipFile.exists() && !ipFile.isDirectory())
                && (userFile.exists() && !userFile.isDirectory())
                && (passFile.exists() && !passFile.isDirectory())) {
            rememberMe.setSelected(true);
            try {
                FileInputStream ipReadFile = new FileInputStream(ipFile);
                FileInputStream userReadFile = new FileInputStream(userFile);
                FileInputStream passReadFile = new FileInputStream(passFile);

                byte[] ipBytes = new byte[100];
                byte[] userBytes = new byte[100];
                byte[] passBytes = new byte[100];
                ipReadFile.read(ipBytes);
                userReadFile.read(userBytes);
                passReadFile.read(passBytes);
                String ip = new String(ipBytes);

                ipAddress.setText(ip);
                username.setText(decrypt(userBytes));
                password.setText(decrypt(passBytes));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
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
        try (FileOutputStream ipFile = new FileOutputStream(ipFileName, false);
                FileOutputStream userFile = new FileOutputStream(userFileName, false);
                FileOutputStream passFile = new FileOutputStream(passFileName, false)) {
            if (rememberMe.isSelected()) {
                ipFile.write(ipAddress.getText().getBytes());
                userFile.write(encrypt(username.getText()));
                passFile.write(encrypt(username.getText()));
            } else {
                ipFile.close();
                userFile.close();
                passFile.close();
                new File(ipFileName).delete();
                new File(userFileName).delete();
                new File(passFileName).delete();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        Stage stage = (Stage) username.getScene().getWindow();
        stage.close();
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
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            result = cipher.doFinal(message.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return result;
    }

    private String decrypt(byte[] message) {
        String result = null;

        String key = "Bar12345Bar12345";
        Key aesKey = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            result = new String(cipher.doFinal(message));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        return result;
    }
}
