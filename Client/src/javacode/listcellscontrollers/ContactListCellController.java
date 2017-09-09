package javacode.listcellscontrollers;

import javacode.classresources.ListCellContact;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ContactListCellController extends ListCell<ListCellContact> {
    @FXML
    private ImageView icon;

    @FXML
    private Label name;

    @FXML
    private Label lastDate;

    @FXML
    private Label lastMessage;

    @FXML
    private GridPane gridPane;

    private FXMLLoader mLLoader;

    @Override
    protected void updateItem(ListCellContact contact, boolean empty) {
        super.updateItem(contact, empty);

        if (empty || contact == null) {
            setText(null);
            setGraphic(null);
        } else {
            mLLoader = new FXMLLoader(getClass().getResource("/resources/listcells/fxml/contact_listcell.fxml"));
            mLLoader.setController(this);

            try {
                mLLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            icon.setImage(contact.getIcon());
            name.setText(contact.getName());

            Rectangle clip = new Rectangle(
                    icon.getFitWidth(), icon.getFitHeight()
            );
            clip.setArcWidth(45);
            clip.setArcHeight(45);
            icon.setClip(clip);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage image = icon.snapshot(parameters, null);
            icon.setClip(null);
            icon.setImage(image);

            if(!Objects.equals(contact.getDate(), null) && !Objects.equals(contact.getDate(), "")) {
                lastMessage.setText(contact.getLastMessage());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date localDate = null;
                try {
                    localDate = formatter.parse(contact.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date = new Date();
                if (date.getDay() == localDate.getDay() && date.getMonth() == localDate.getMonth() && date.getYear() == localDate.getYear()) {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    lastDate.setText(format.format(localDate));
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM");
                    lastDate.setText(format.format(localDate));
                }
            }

            setText(null);
            setGraphic(gridPane);

            selectedProperty().addListener( (obsVal, oldVal, newVal) -> {
                if(newVal){
                    name.setStyle("-fx-text-fill: white");
                    lastDate.setStyle("-fx-text-fill: white");
                    lastMessage.setStyle("-fx-text-fill: white");
                } else {
                    name.setStyle("");
                    lastDate.setStyle("");
                    lastMessage.setStyle("");
                }
            });

            widthProperty().addListener((observable, oldValue, newValue) -> {
                gridPane.setPrefWidth(newValue.doubleValue() - 15);
            });
        }

    }
}
