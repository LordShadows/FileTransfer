package javacode.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AboutWindowController {
    private Stage STAGE;

    private double xOffset;
    private double yOffset;

    @FXML
    private Button buttonCancel;

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

        buttonCancel.setOnMouseClicked(event -> STAGE.close());
    }

    public void Initialization(Stage _stage) {
        STAGE = _stage;
        initComponents();
    }
}
