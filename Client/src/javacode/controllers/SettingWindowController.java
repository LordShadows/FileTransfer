package javacode.controllers;

import dialogs.Dialogs;
import javacode.classresources.ListCellContact;
import javacode.implementationclasses.ClientFunctions;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.Objects;

public class SettingWindowController {
    private Stage STAGE;

    private double xOffset;
    private double yOffset;

    private boolean isAllContacts = false;

    private ListCellContact selectedContact;

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
    private TextField textFieldSearch;

    @FXML
    public AnchorPane loadPane;

    @FXML
    private AnchorPane buttonYourContact;

    @FXML
    private Label labelButtoYourContact;

    @FXML
    private AnchorPane buttonAllContact;

    @FXML
    private Label labelButtonAllContact;

    @FXML
    private Button buttonAddContact;

    @FXML
    private Button buttonDeleteContact;


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

        buttonHeaderClose.setOnAction(event -> STAGE.close());

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

        buttonAllContact.setOnMouseClicked(event -> {
            buttonYourContact.getStyleClass().remove("pressed");
            labelButtoYourContact.getStyleClass().remove("pressed");
            buttonAllContact.getStyleClass().add("pressed");
            labelButtonAllContact.getStyleClass().add("pressed");
            loadPane.setVisible(true);
            textFieldSearch.setText("");
            buttonDeleteContact.setVisible(false);
            buttonAddContact.setVisible(true);
            buttonAddContact.setDisable(true);

            selectedContact = null;
            isAllContacts = true;

            ClientFunctions.sendMessage("UpdateAllContactOnEditPane", new Object[] { "" });
        });

        buttonYourContact.setOnMouseClicked(event -> {
            buttonAllContact.getStyleClass().remove("pressed");
            labelButtonAllContact.getStyleClass().remove("pressed");
            buttonYourContact.getStyleClass().add("pressed");
            labelButtoYourContact.getStyleClass().add("pressed");
            loadPane.setVisible(true);
            textFieldSearch.setText("");
            buttonDeleteContact.setVisible(true);
            buttonAddContact.setVisible(false);
            buttonDeleteContact.setDisable(true);

            selectedContact = null;
            isAllContacts = false;

            ClientFunctions.sendMessage("UpdateMyContactOnEditPane", new Object[] { "" });
        });

        contactsListView.setOnMouseClicked(event->{
            if(contactsListView.getSelectionModel().getSelectedItem() != null) {
                if(buttonDeleteContact.isDisable()) {
                    buttonDeleteContact.setDisable(false);
                }
                if(buttonAddContact.isDisable()) {
                    buttonAddContact.setDisable(false);
                }
                ListCellContact contact = (ListCellContact) contactsListView.getSelectionModel().getSelectedItem();
                if (!Objects.equals(contact, selectedContact)) {
                    selectedContact = contact;
                }
            }
        });

        textFieldSearch.setOnKeyReleased(event -> {
            if(isAllContacts) {
                ClientFunctions.sendMessage("UpdateAllContactOnEditPane", new Object[] { textFieldSearch.getText() });
            } else {
                ClientFunctions.sendMessage("UpdateMyContactOnEditPane", new Object[] { textFieldSearch.getText() });
            }
        });

        buttonAddContact.setOnMouseClicked(event -> {
            if(selectedContact != null){
                if(Dialogs.showQuestion("Пользователь будет добавлен в ваш список контактов, где вы можете начать с ним диалог.", "Вы действительно хотите добавить контакт?")) {
                    buttonAddContact.setDisable(true);
                    ClientFunctions.sendMessage("AddContact", new Object[]{selectedContact.getID()});
                    ClientFunctions.sendMessage("UpdateAllContactOnEditPane", new Object[] { textFieldSearch.getText() });
                    Platform.runLater(() -> ClientFunctions.MWC.UpdateUserContactsData());
                }
            }
        });

        buttonDeleteContact.setOnMouseClicked(event -> {
            if(selectedContact != null){
                if(Dialogs.showQuestion("Пользователь будет удален из списка ваших контактов. Также будут удалены все сообщения и файлы.", "Вы действительно хотите удалить контакт?")) {
                    buttonDeleteContact.setDisable(true);
                    ClientFunctions.sendMessage("DeleteContact", new Object[]{selectedContact.getID()});
                    ClientFunctions.sendMessage("UpdateMyContactOnEditPane", new Object[] { textFieldSearch.getText() });
                    Platform.runLater(() -> ClientFunctions.MWC.UpdateUserContactsData());
                }
            }
        });
    }


    public void Initialization(Stage _stage) {
        STAGE = _stage;
        initComponents();
        ClientFunctions.SWC = this;
        buttonAddContact.setVisible(false);
        ClientFunctions.sendMessage("UpdateMyContactOnEditPane", new Object[] { "" });
    }

}