package javacode.controllers;

import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import dialogs.Dialogs;
import javacode.classresources.ContactData;
import javacode.implementationclasses.ClientFunctions;
import javacode.implementationclasses.WriteServer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountWindowController {
    public Stage STAGE;
    private LogInWindowController LIWC;
    private String host;

    private double xOffset;
    private double yOffset;

    private ContactData CONTACTDATA;
    private BufferedImage NEWAVATAR = null;
    private boolean isChange = false;

    @FXML
    private Label labelPassword1;

    @FXML
    private ImageView imageAvatar;

    @FXML
    private TextField textFieldEmail;

    @FXML
    private PasswordField textFieldPassword2;

    @FXML
    public Button buttonAddAccount;

    @FXML
    public Button buttonCancel;

    @FXML
    private TextField textFieldNickname;

    @FXML
    private TextField textFieldName;

    @FXML
    private Label labelPassword2;

    @FXML
    private PasswordField textFieldPassword1;

    @FXML
    private Button buttonChangeAvatar;

    @FXML
    private Label labelPasswordHeader;

    @FXML
    private Button buttonSaveChanges;

    @FXML
    private Button buttonOK;

    @FXML
    private AnchorPane anchorPaneMainHeader;

    @FXML
    private Label labelMainTitle;

    @FXML
    private Button buttonDeleteAccount;

    private void initComponents() {
        anchorPaneMainHeader.setOnMousePressed(event -> {
            xOffset = STAGE.getX() - event.getScreenX();
            yOffset = STAGE.getY() - event.getScreenY();
        });

        anchorPaneMainHeader.setOnMouseDragged(event -> {
            STAGE.setX(event.getScreenX() + xOffset);
            STAGE.setY(event.getScreenY() + yOffset);
        });

        buttonCancel.setOnMouseClicked(event -> STAGE.close());

        buttonOK.setOnMouseClicked(event -> STAGE.close());

        textFieldNickname.setOnKeyReleased(event -> onKeyReleased(event.getCode()));

        textFieldName.setOnKeyReleased(event -> onKeyReleased(event.getCode()));

        textFieldEmail.setOnKeyReleased(event -> onKeyReleased(event.getCode()));

        textFieldPassword1.setOnKeyReleased(event -> onKeyReleased(event.getCode()));

        textFieldPassword2.setOnKeyReleased(event -> onKeyReleased(event.getCode()));

        buttonSaveChanges.setOnMouseClicked(event -> saveChanges());

        buttonAddAccount.setOnMouseClicked(event -> addContact());

        buttonChangeAvatar.setOnMouseClicked(event -> changeAvatar());

        buttonDeleteAccount.setOnMouseClicked(event -> {
            if(Dialogs.showQuestion("При удалении аккаунта также удалится список аккаунтов и ваши сообщения.", "Вы действительно хотите удалить аккаунт?")) {
                ClientFunctions.sendMessage("DeleteUserAccount", null);
                Dialogs.showAlert("Приложение будет закрыто.", "Аккаунт удален.");
                System.exit(0);
            }
        });
    }

    public void Initialization(Stage _stage, String host, LogInWindowController logInWindowController) {
        this.host = host;
        LIWC = logInWindowController;
        STAGE = _stage;
        initComponents();
        buttonOK.setVisible(false);
        buttonSaveChanges.setVisible(false);
        buttonDeleteAccount.setVisible(false);
    }

    public void Initialization(Stage _stage, ContactData contact, boolean isReadOnly) {
        STAGE = _stage;
        CONTACTDATA = contact;
        initComponents();

        textFieldNickname.setText(contact.getNickname());
        textFieldName.setText(contact.getName());
        textFieldEmail.setText(contact.getEmail());

        if(isReadOnly){
            buttonCancel.setVisible(false);
            buttonSaveChanges.setVisible(false);
            buttonAddAccount.setVisible(false);
            buttonDeleteAccount.setVisible(false);
            labelPasswordHeader.setDisable(true);
            labelPassword1.setDisable(true);
            labelPassword2.setDisable(true);
            labelMainTitle.setDisable(true);
            buttonChangeAvatar.setDisable(true);
            textFieldPassword1.setDisable(true);
            textFieldPassword2.setDisable(true);
            labelMainTitle.setDisable(true);
            textFieldNickname.setEditable(false);
            textFieldName.setEditable(false);
            textFieldEmail.setEditable(false);
        } else {
            buttonOK.setVisible(false);
            buttonAddAccount.setVisible(false);
            isChange = true;
        }

        if (contact.getAvatar() != null) {
            BufferedImage fromBytes;
            try {
                fromBytes = ImageIO.read(new ByteInputStream(contact.getAvatar(), contact.getAvatar().length));
                Platform.runLater(()-> {
                    imageAvatar.setImage(SwingFXUtils.toFXImage(fromBytes, null));
                    Rectangle clip = new Rectangle(
                            imageAvatar.getFitWidth(), imageAvatar.getFitHeight()
                    );
                    clip.setArcWidth(150);
                    clip.setArcHeight(150);
                    imageAvatar.setClip(clip);

                    SnapshotParameters parameters = new SnapshotParameters();
                    parameters.setFill(Color.TRANSPARENT);
                    WritableImage image = imageAvatar.snapshot(parameters, null);
                    imageAvatar.setClip(null);
                    imageAvatar.setImage(image);
                });
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }

    private void saveChanges(){
        if(!isCorrect()) return;
        String passwordHash = null;
        if(!Objects.equals(textFieldPassword1.getText(), ""))  passwordHash = getHash(textFieldPassword1.getText());

        ClientFunctions.sendMessage("UpdateUserAccount", new Object[]{ new Gson().toJson(new ContactData(
                !Objects.equals(textFieldNickname.getText(), CONTACTDATA.getNickname()) ? textFieldNickname.getText() : null,
                !Objects.equals(textFieldName.getText(), CONTACTDATA.getName()) ? textFieldName.getText() : null,
                !Objects.equals(textFieldEmail.getText(), CONTACTDATA.getEmail()) ? textFieldEmail.getText() : null,
                NEWAVATAR != null ? extractBytes2(NEWAVATAR) : null,
                passwordHash)) });
        STAGE.close();
    }

    private void addContact(){
        if(!isCorrect()) return;
        buttonAddAccount.setDisable(true);
        buttonCancel.setDisable(true);
        ContactData contactData = new ContactData(
                textFieldNickname.getText(),
                textFieldName.getText(),
                textFieldEmail.getText(),
                NEWAVATAR != null ? extractBytes2(NEWAVATAR) : null,
                getHash(textFieldPassword1.getText()));
        new Thread(() -> ClientFunctions.WS = new WriteServer(host, 4848, contactData, LIWC, this)).start();
    }

    private String getHash(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            return DatatypeConverter.printHexBinary(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void onKeyReleased(KeyCode keyCode) {
        if(keyCode == KeyCode.ENTER){
            if(isChange) {
                if(isChange()){
                    saveChanges();
                }
            } else {
                addContact();
            }
        } else {
            if(isChange) isChange();
        }
    }

    private void changeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузка вашего аватара...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File avatar = fileChooser.showOpenDialog(STAGE);
        if (avatar != null) {
            try {
                NEWAVATAR = ImageIO.read(avatar);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NEWAVATAR = subImage(NEWAVATAR);
            imageAvatar.setImage(SwingFXUtils.toFXImage(NEWAVATAR, null));
            Rectangle clip = new Rectangle(
                    imageAvatar.getFitWidth(), imageAvatar.getFitHeight()
            );
            clip.setArcWidth(150);
            clip.setArcHeight(150);
            imageAvatar.setClip(clip);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage image = imageAvatar.snapshot(parameters, null);
            imageAvatar.setClip(null);
            imageAvatar.setImage(image);
        }
        if(isChange) isChange();
    }

    private boolean isChange() {
        if(!Objects.equals(textFieldNickname.getText(), CONTACTDATA.getNickname())
                || !Objects.equals(textFieldName.getText(), CONTACTDATA.getName())
                || !Objects.equals(textFieldEmail.getText(), CONTACTDATA.getEmail())
                || !Objects.equals(textFieldPassword1.getText(), "")
                || NEWAVATAR != null){
            buttonSaveChanges.setDisable(false);
            return true;
        }
        buttonSaveChanges.setDisable(true);
        return false;
    }

    private boolean isCorrect(){
        if(!regex(textFieldNickname.getText(), "^[A-Za-z0-9@#&_]{3,}$")){
            Dialogs.showAlert("Имя представляет собой сочетание букв латинского алфавита, цифр и спец. знаков (@#&_). Минимум 3 символа.", "Не правильно введен никнейм пользователя.");
            return false;
        }
        if(!regex(textFieldName.getText(), "^[А-Яа-яA-Za-z- ]+$")){
            Dialogs.showAlert("Имя представляет собой сочетание букв алфавита.", "Не правильно введено имя пользователя.");
            return false;
        }
        if(!regex(textFieldEmail.getText(), "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$")){
            Dialogs.showAlert("Проверте корректность ввода электронной почты.", "Не правильно введена электронная почта.");
            return false;
        }
        if(!Objects.equals(textFieldPassword1.getText(), "") || !Objects.equals(textFieldPassword2.getText(), "") || !isChange) {
            if (!regex(textFieldPassword1.getText(), "^[A-Za-z0-9_]{6,}$")) {
                Dialogs.showAlert("Пароль представляет собой сочетание букв латинского алфавита, цифр и знак подчеркивания. Минимум 6 символов.", "Не правильно введен пароль пользователя.");
                return false;
            }
            if (!Objects.equals(textFieldPassword1.getText(), textFieldPassword2.getText())) {
                Dialogs.showAlert("Пароли должны совпадать.", "Пароли не совпадают.");
                return false;
            }
        }
        return true;
    }

    private boolean regex(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return m.matches();
    }

    private BufferedImage subImage(BufferedImage image){
        BufferedImage resizedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        Image scaled;
        if(image.getWidth() > image.getHeight()){
            scaled = image.getSubimage((image.getWidth() - image.getHeight()) / 2, 0, image.getHeight(), image.getHeight()).getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        }
        else {
            scaled = image.getSubimage(0, (image.getHeight() - image.getWidth()) / 2, image.getWidth(), image.getWidth()).getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        }
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return resizedImage;
    }

    private static byte[] extractBytes2(BufferedImage bufferedImage){
        ByteOutputStream bos = null;
        try {
            bos = new ByteOutputStream();
            ImageIO.write(bufferedImage, "jpg", bos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bos != null;
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bos.getBytes();
    }

}
