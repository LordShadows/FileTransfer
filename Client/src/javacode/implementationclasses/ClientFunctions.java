package javacode.implementationclasses;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import javacode.classresources.*;
import javacode.controllers.AccountWindowController;
import javacode.controllers.LogInWindowController;
import javacode.controllers.MainWindowController;
import javacode.controllers.SettingWindowController;
import javacode.listcellscontrollers.ContactListCellController;
import javacode.listcellscontrollers.MessageListCellController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class ClientFunctions {
    static public WriteServer WS;
    static public MainWindowController MWC;
    static public SettingWindowController SWC;

    static public void sendMessage(String command, Object[] parameters) {
        WS.sendMessage(new MessageProtocol(command, parameters));
    }

    static public void SendFileToServer(String idContact, File file){
        sendMessage("MessageFileToServer", new Object[]{ idContact, file.getName(), file.length() } );
        try {
            FileInputStream input = new FileInputStream(file);
            long readedLength = 0;
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            while(true) {
                int readedBytesCount = input.read(buffer);
                if (readedBytesCount == -1) {
                    break;
                }
                if (readedBytesCount > 0) {
                    WS.socketWriter.write(buffer, 0, readedBytesCount);

                    readedLength += readedBytesCount;
                    MWC.fileProgressBar.setProgress(readedLength/file.length());
                }
            }
            WS.socketWriter.flush();
            input.close();
            MWC.fileProgressBar.setProgress(0);
            MWC.textFieldOpenFile.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void UpdateUserAccountData(String name, byte[] avatar) {
        Platform.runLater(()-> MWC.labelUserName.setText(name));
        if (avatar != null) {
            BufferedImage fromBytes;
            try {
                fromBytes = ImageIO.read(new ByteInputStream(avatar, avatar.length));
                Platform.runLater(()-> {
                    MWC.imageUserLogo.setImage(SwingFXUtils.toFXImage(fromBytes, null));
                    Rectangle clip = new Rectangle(
                            MWC.imageUserLogo.getFitWidth(), MWC.imageUserLogo.getFitHeight()
                    );
                    clip.setArcWidth(35);
                    clip.setArcHeight(35);
                    MWC.imageUserLogo.setClip(clip);

                    SnapshotParameters parameters = new SnapshotParameters();
                    parameters.setFill(Color.TRANSPARENT);
                    WritableImage image = MWC.imageUserLogo.snapshot(parameters, null);
                    MWC.imageUserLogo.setClip(null);
                    MWC.imageUserLogo.setImage(image);
                });
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }

    static void UpdateUserContactsData(Contact[] contacts) {
        try {
            ObservableList<ListCellContact> contactObservableList = FXCollections.observableArrayList();
            ListCellContact selectedContact = (ListCellContact)MWC.contactsListView.getSelectionModel().getSelectedItem();
            int index = -1;
            if(selectedContact != null) {
                for (Contact contact : contacts) {
                    BufferedImage fromBytes = ImageIO.read(new ByteInputStream(contact.getAvatar(), contact.getAvatar().length));
                    contactObservableList.add(new ListCellContact(contact.getId(), contact.getName(), SwingFXUtils.toFXImage(fromBytes, null), contact.getLastMessage(), contact.getDate()));
                    if (Objects.equals(selectedContact.getID(), contact.getId())) index = contactObservableList.size() - 1;
                }
            } else {
                for (Contact contact : contacts) {
                    BufferedImage fromBytes = ImageIO.read(new ByteInputStream(contact.getAvatar(), contact.getAvatar().length));
                    contactObservableList.add(new ListCellContact(contact.getId(), contact.getName(), SwingFXUtils.toFXImage(fromBytes, null), contact.getLastMessage(), contact.getDate()));
                }
            }
            int finalIndex = index;
            Platform.runLater(()-> {
                MWC.contactsListView.setItems(contactObservableList);
                MWC.contactsListView.setCellFactory(slw -> new ContactListCellController());
                MWC.loadPane.setVisible(false);
                if(finalIndex >= 0) {
                    MWC.contactsListView.getSelectionModel().select(finalIndex);
                } else {
                    MWC.idSelectContact = null;
                    MWC.messagesListView.getItems().clear();
                    MWC.labelSelectContact.setText("Выберите диалог");
                    if(MWC.filePane.getScaleY() == 1) {
                        MWC.buttonSendFile.setDisable(true);
                        MWC.textFieldOpenFile.setDisable(true);
                        MWC.buttonOpenFile.setDisable(true);
                        MWC.buttonShowMessage.setDisable(true);
                    } else {
                        MWC.buttonSendMessage.setDisable(true);
                        MWC.textFieldMessage.setDisable(true);
                        MWC.buttonShowFile.setDisable(true);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void UpdateContactMessages(Message[] messages) {
        ObservableList<ListCellMessage> messageObservableList = FXCollections.observableArrayList();
        for (Message message : messages) {
            messageObservableList.add(new ListCellMessage(message.getFilename(), message.getFilesize(), message.getDate(), message.getUser(), message.getFilepath(), message.getMessagetext()));
        }
        Platform.runLater(()-> {
            MWC.messagesListView.setItems(messageObservableList);
            MWC.messagesListView.setCellFactory(slw -> new MessageListCellController());
            MWC.messagesListView.scrollTo(messageObservableList.size() - 1);
        });
    }

    static void GetInfoAboutContact(ContactData contact) {
        Platform.runLater(()-> {
            try {
                Image ico = new Image("/resources/img/favicon.png");
                Stage accountStage = new Stage();
                FXMLLoader accountLoader = new FXMLLoader(ClientFunctions.class.getResource("/resources/fxml/account_window.fxml"));
                Parent accountRoot = accountLoader.load();
                accountStage.getIcons().add(ico);
                accountStage.initStyle(StageStyle.UNDECORATED);
                Scene accountScene = new Scene(accountRoot, Color.TRANSPARENT);
                accountStage.setScene(accountScene);
                accountStage.setTitle("Информация об аккаунте");
                accountStage.initStyle(StageStyle.TRANSPARENT);
                AccountWindowController accountController = accountLoader.getController();
                accountController.Initialization(accountStage, contact, true);
                accountStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    static void GetInfoAboutMe(ContactData contact) {
        Platform.runLater(()-> {
            try {
                Image ico = new Image("/resources/img/favicon.png");
                Stage accountStage = new Stage();
                FXMLLoader accountLoader = new FXMLLoader(ClientFunctions.class.getResource("/resources/fxml/account_window.fxml"));
                Parent accountRoot = accountLoader.load();
                accountStage.getIcons().add(ico);
                accountStage.initStyle(StageStyle.UNDECORATED);
                Scene accountScene = new Scene(accountRoot, Color.TRANSPARENT);
                accountStage.setScene(accountScene);
                accountStage.setTitle("Редактирование аккаунта");
                accountStage.initStyle(StageStyle.TRANSPARENT);
                AccountWindowController accountController = accountLoader.getController();
                accountController.Initialization(accountStage, contact, false);
                accountStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    static void UpdateContactOnEditPane(Contact[] contacts) {
        try {
            ObservableList<ListCellContact> contactObservableList = FXCollections.observableArrayList();
            for (Contact contact : contacts) {
                BufferedImage fromBytes = ImageIO.read(new ByteInputStream(contact.getAvatar(), contact.getAvatar().length));
                contactObservableList.add(new ListCellContact(contact.getId(), contact.getName(), SwingFXUtils.toFXImage(fromBytes, null), contact.getLastMessage(), contact.getDate()));
            }
            Platform.runLater(()-> {
                SWC.contactsListView.setItems(contactObservableList);
                SWC.contactsListView.setCellFactory(slw -> new ContactListCellController());
                SWC.loadPane.setVisible(false);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
