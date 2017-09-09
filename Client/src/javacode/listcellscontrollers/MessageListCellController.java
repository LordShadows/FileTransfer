package javacode.listcellscontrollers;

import javacode.classresources.ListCellMessage;
import javacode.implementationclasses.FileIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import jdk.nashorn.internal.parser.DateParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MessageListCellController extends ListCell<ListCellMessage> {
    public String ID;

    @FXML
    private ImageView icon;

    @FXML
    private GridPane gridPane;

    @FXML
    private AnchorPane mainBackground;

    @FXML
    private Label name;

    @FXML
    private Label size;

    @FXML
    private Label date;

    @FXML
    private Label text;

    @FXML
    private GridPane gridPaneContent;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(ListCellMessage message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (Objects.equals(message.getMessagetext(), null) || Objects.equals(message.getMessagetext(), "")) {
                if (!message.isMine()) {
                    setStyle("-fx-alignment: top-left;");
                    mLLoader = new FXMLLoader(getClass().getResource("/resources/listcells/fxml/left_file_message_listcell.fxml"));
                } else {
                    setStyle("-fx-alignment: top-right;");
                    mLLoader = new FXMLLoader(getClass().getResource("/resources/listcells/fxml/right_file_message_listcell.fxml"));
                }
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                name.setText(message.getName());
                size.setText(message.getSize());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date localDate = null;
                try {
                    localDate = formatter.parse(message.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yy");
                date.setText(format.format(localDate));

                icon.setImage(FileIcon.getIcon(message.getName()));

                setText(null);
                setGraphic(gridPane);
            } else {
                if (!message.isMine()) {
                    setStyle("-fx-alignment: top-left;");
                    mLLoader = new FXMLLoader(getClass().getResource("/resources/listcells/fxml/left_text_message_listcell.fxml"));
                } else {
                    setStyle("-fx-alignment: top-right;");
                    mLLoader = new FXMLLoader(getClass().getResource("/resources/listcells/fxml/right_text_message_listcell.fxml"));
                }
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                text.setText(message.getMessagetext());

                setText(null);
                setGraphic(gridPane);
            }
        }

    }
}