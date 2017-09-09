package javacode.controllers;

import javacode.classresources.ListCellContact;
import javacode.implementationclasses.ClientFunctions;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainWindowController {
    private Stage STAGE;

    private double xOffset;
    private double yOffset;

    public String idSelectContact;
    private File selectFile;

    @FXML
    private AnchorPane anchorPaneMainHeader;

    @FXML
    private Button buttonHeaderMax;

    @FXML
    private Button buttonHeaderClose;

    @FXML
    private Button buttonHeaderMin;

    @FXML
    private AnchorPane anchorPaneHeaderButtons;

    @FXML
    public ListView contactsListView;

    @FXML
    public ListView messagesListView;

    @FXML
    private Button buttonEditContactsList;

    @FXML
    public ImageView imageUserLogo;

    @FXML
    public Label labelUserName;

    @FXML
    private TextField textFieldSearch;

    @FXML
    public Label labelSelectContact;

    @FXML
    public TextField textFieldMessage;

    @FXML
    private AnchorPane messagePane;

    @FXML
    public Button buttonShowFile;

    @FXML
    public AnchorPane filePane;

    @FXML
    public Button buttonShowMessage;

    @FXML
    public AnchorPane loadPane;

    @FXML
    public ProgressBar fileProgressBar;

    @FXML
    public TextField textFieldOpenFile;

    @FXML
    public Button buttonOpenFile;

    @FXML
    public Button buttonSendFile;

    @FXML
    public Button buttonSendMessage;

    @FXML
    private Label labelAboutContact;

    @FXML
    private Label labelEditingContact;

    @FXML
    private Button buttonAbout;

    private void initComponents() {
        anchorPaneHeaderButtons.setOnMouseEntered(event -> {
            buttonHeaderClose.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/close-hover.png") + ");");
            buttonHeaderMin.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/min-hover.png") + ");");
            buttonHeaderMax.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/max-hover.png") + ");");
        });

        anchorPaneHeaderButtons.setOnMouseExited(event -> {
            buttonHeaderClose.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/close-normal.png") + ");");
            buttonHeaderMin.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/min-normal.png") + ");");
            buttonHeaderMax.setStyle("-fx-background-image: url(" + getClass().getResource("/resources/img/headerbuttons/max-normal.png") + ");");
        });

        STAGE.setOnCloseRequest(event -> System.exit(0));

        buttonHeaderClose.setOnAction(event -> System.exit(0));

        buttonHeaderMin.setOnAction(event -> STAGE.setIconified(true));

        buttonHeaderMax.setOnAction(event -> {
            if(STAGE.isMaximized())
                STAGE.setMaximized(false);
            else
                STAGE.setMaximized(true);
        });

        anchorPaneMainHeader.setOnMousePressed(event -> {
            xOffset = STAGE.getX() - event.getScreenX();
            yOffset = STAGE.getY() - event.getScreenY();
        });

        anchorPaneMainHeader.setOnMouseDragged(event -> {
            STAGE.setX(event.getScreenX() + xOffset);
            STAGE.setY(event.getScreenY() + yOffset);
        });

        contactsListView.setOnMouseClicked(event->{
            if(contactsListView.getSelectionModel().getSelectedItem() != null) {
                if(buttonSendFile.isDisable()) {
                    buttonSendFile.setDisable(false);
                    textFieldOpenFile.setDisable(false);
                    buttonOpenFile.setDisable(false);
                    buttonShowMessage.setDisable(false);
                }
                if(buttonSendMessage.isDisable()) {
                    buttonSendFile.setDisable(false);
                    textFieldMessage.setDisable(false);
                    buttonShowFile.setDisable(false);
                }
                ListCellContact contact = (ListCellContact) contactsListView.getSelectionModel().getSelectedItem();
                if (!Objects.equals(contact.getID(), idSelectContact)) {
                    labelSelectContact.setText(contact.getName());
                    idSelectContact = contact.getID();
                    messagesListView.getItems().clear();
                    UpdateContactMessages();
                    textFieldOpenFile.setText("");
                    textFieldMessage.setText("");
                    selectFile = null;
                }
            }
        });

        textFieldMessage.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 90)
                textFieldMessage.setText(oldValue);
            else
                textFieldMessage.setText(newValue);
        });

        buttonShowMessage.setOnMouseClicked(event -> filePane.setScaleY(0));

        buttonShowFile.setOnMouseClicked(event -> filePane.setScaleY(1));

        buttonOpenFile.setOnAction(event -> {
            if(!Objects.equals(idSelectContact, null)) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Выберите файл для загрузки...");
                File file = fileChooser.showOpenDialog(STAGE);
                if (file != null) {
                    textFieldOpenFile.setText(file.getName());
                    selectFile = file;
                }
            }
        });

        buttonSendFile.setOnAction(event -> {
            if(selectFile != null)
            ClientFunctions.SendFileToServer(idSelectContact, selectFile);
        });

        buttonSendMessage.setOnAction(event -> {
            if(!Objects.equals(textFieldMessage.getText(), "")){
                ClientFunctions.sendMessage("MessageTextToServer", new Object[] { idSelectContact, textFieldMessage.getText() });
            }
        });

        textFieldMessage.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(!Objects.equals(textFieldMessage.getText(), "")){
                    ClientFunctions.sendMessage("MessageTextToServer", new Object[] { idSelectContact, textFieldMessage.getText() });
                    textFieldMessage.setText("");
                }
            }
        });

        labelAboutContact.setOnMouseClicked(event -> {
            if(!Objects.equals(idSelectContact, null)){
                ClientFunctions.sendMessage("GetInfoAboutContact", new Object[] { idSelectContact });
            }
        });

        labelEditingContact.setOnMouseClicked(event -> ClientFunctions.sendMessage("GetInfoAboutMe", null));

        buttonEditContactsList.setOnMouseClicked(event -> {
            try {
                Image ico = new Image("/resources/img/favicon.png");
                Stage settingStage = new Stage();
                FXMLLoader settingLoader = new FXMLLoader(ClientFunctions.class.getResource("/resources/fxml/setting_window.fxml"));
                Parent settingRoot = settingLoader.load();
                settingStage.getIcons().add(ico);
                settingStage.initStyle(StageStyle.UNDECORATED);
                Scene accountScene = new Scene(settingRoot, Color.TRANSPARENT);
                settingStage.setScene(accountScene);
                settingStage.setTitle("Список контактов");
                settingStage.initStyle(StageStyle.TRANSPARENT);
                SettingWindowController settingController = settingLoader.getController();
                settingController.Initialization(settingStage);
                settingStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonAbout.setOnMouseClicked(event -> {
            try {
                Image ico = new Image("/resources/img/favicon.png");
                Stage aboutStage = new Stage();
                FXMLLoader aboutLoader = new FXMLLoader(ClientFunctions.class.getResource("/resources/fxml/about_window.fxml"));
                Parent aboutRoot = aboutLoader.load();
                aboutStage.getIcons().add(ico);
                aboutStage.initStyle(StageStyle.UNDECORATED);
                Scene aboutScene = new Scene(aboutRoot, Color.TRANSPARENT);
                aboutStage.setScene(aboutScene);
                aboutStage.setTitle("О программе");
                aboutStage.initStyle(StageStyle.TRANSPARENT);
                AboutWindowController aboutController = aboutLoader.getController();
                aboutController.Initialization(aboutStage);
                aboutStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        textFieldSearch.setOnKeyReleased(event -> UpdateUserContactsData());
    }

    private void UpdateUserAccountData() {
        ClientFunctions.sendMessage("UpdateUserAccountData", null);
    }

    public void UpdateUserContactsData() {
        ClientFunctions.sendMessage("UpdateUserContactsData", new Object[]{ textFieldSearch.getText() });
    }

    private void UpdateContactMessages() {
        ClientFunctions.sendMessage("UpdateContactMessages", new Object[]{ idSelectContact });
    }

    public void Initialization(Stage _stage) {
        STAGE = _stage;
        initComponents();
        ClientFunctions.MWC = this;
        UpdateUserAccountData();
        UpdateUserContactsData();
    }

}