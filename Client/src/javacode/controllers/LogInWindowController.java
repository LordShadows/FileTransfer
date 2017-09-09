package javacode.controllers;

import dialogs.Dialogs;
import javacode.implementationclasses.ClientFunctions;
import javacode.implementationclasses.ParamConfiguration;
import javacode.implementationclasses.WriteServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.*;

public class LogInWindowController {
    public Stage STAGE;

    private double xOffset;
    private double yOffset;

    public boolean isAuthorizate = false;

    @FXML
    private TextField textFieldName;

    @FXML
    private PasswordField textFieldPassword;

    @FXML
    public Button buttonConnect;

    @FXML
    private Button buttonCancel;

    @FXML
    public Button buttonAddUser;

    @FXML
    private TextField textFieldIPAddress;

    @FXML
    private CheckBox checkBoxSavePassword;

    @FXML
    private AnchorPane anchorPaneMainHeader;

    private void initComponents() {
        anchorPaneMainHeader.setOnMousePressed(event -> {
            xOffset = STAGE.getX() - event.getScreenX();
            yOffset = STAGE.getY() - event.getScreenY();
        });

        anchorPaneMainHeader.setOnMouseDragged(event -> {
            STAGE.setX(event.getScreenX() + xOffset);
            STAGE.setY(event.getScreenY() + yOffset);
        });

        buttonCancel.setOnMouseClicked(event -> System.exit(0));

        textFieldIPAddress.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                initSocket();
            }
        });

        textFieldName.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                initSocket();
            }
        });

        textFieldPassword.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                initSocket();
            }
        });

        buttonConnect.setOnMouseClicked(event -> initSocket());

        buttonAddUser.setOnMouseClicked(event -> Platform.runLater(()-> {
            try {
                Image ico = new Image("/resources/img/favicon.png");
                Stage accountStage = new Stage();
                FXMLLoader accountLoader = new FXMLLoader(ClientFunctions.class.getResource("/resources/fxml/account_window.fxml"));
                Parent accountRoot = accountLoader.load();
                accountStage.getIcons().add(ico);
                accountStage.initStyle(StageStyle.UNDECORATED);
                Scene accountScene = new Scene(accountRoot, Color.TRANSPARENT);
                accountStage.setScene(accountScene);
                accountStage.setTitle("Добавление аккаунта");
                accountStage.initStyle(StageStyle.TRANSPARENT);
                AccountWindowController accountController = accountLoader.getController();
                accountController.Initialization(accountStage, textFieldIPAddress.getText(), this);
                accountStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        STAGE.setOnCloseRequest(event -> {
            if(!isAuthorizate) System.exit(0);
        });
    }

    private void initSocket(){
        if(!regex(textFieldIPAddress.getText(), "^(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[0-9]{2}|[0-9])(\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[0-9]{2}|[0-9])){3}$")){
            Dialogs.showAlert("IP адрес представляет собой A.B.C.D, где каждый элемент является числом от 0 до 255. (192.0.0.1)", "Не правильно введен IP адрес.");
            return;
        }
        if(!regex(textFieldName.getText(), "^[A-Za-z0-9@#&_]+$")){
            Dialogs.showAlert("Имя представляет собой сочетание букв латинского алфавита, цифр и спец. знаков (@#&_).", "Не правильно введено имя пользователя.");
            return;
        }
        if(!regex(textFieldPassword.getText(), "^[A-Za-z0-9_*]+$")){
            Dialogs.showAlert("Пароль представляет собой сочетание букв латинского алфавита, цифр и знак подчеркивания.", "Не правильно введен пароль пользователя.");
            return;
        }

        buttonConnect.setDisable(true);
        buttonAddUser.setDisable(true);

        String passwordHash;
        if(textFieldPassword.getText().contains("*")){
            passwordHash = ParamConfiguration.password;
        } else {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            assert md != null;
            md.update(textFieldPassword.getText().getBytes());
            passwordHash = DatatypeConverter.printHexBinary(md.digest());
        }

        if(checkBoxSavePassword.isSelected()) {
            ParamConfiguration.login = textFieldName.getText();
            ParamConfiguration.passwordLength = textFieldPassword.getText().length();
            ParamConfiguration.password = passwordHash;
            ParamConfiguration.save();
        }

        new Thread(() -> ClientFunctions.WS = new WriteServer(textFieldIPAddress.getText(), 4848, textFieldName.getText(), passwordHash, this)).start();
    }

    private boolean regex(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return m.matches();
    }

    public void Initialization(Stage _stage) {
        STAGE = _stage;
        initComponents();
        if(!Objects.equals(ParamConfiguration.login, "")){
            textFieldName.setText(ParamConfiguration.login);
            char[] text = new char[ParamConfiguration.passwordLength];
            for (int i = 0; i < ParamConfiguration.passwordLength; i++) { text[i] = '*'; }
            textFieldPassword.setText(new String(text));
            checkBoxSavePassword.setSelected(true);
        }
    }
}
