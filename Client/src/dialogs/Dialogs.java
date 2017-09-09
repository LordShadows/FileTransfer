package dialogs;

import dialogs.controller.DialogWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class Dialogs {
    static private Image ico = new Image("/resources/img/favicon.png");

    static public void showError(String error, String title){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(Dialogs.class.getResource("fxml/dialog_window.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(ico);
        stage.initStyle(StageStyle.UNDECORATED);
        assert root != null;
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Упс! Возникла ошибка");
        stage.initStyle(StageStyle.TRANSPARENT);
        DialogWindowController controller = loader.getController();
        controller.Initialization(stage, title, error, false);
        stage.showAndWait();
    }

    static public boolean showQuestion(String question, String title){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(Dialogs.class.getResource("fxml/dialog_window.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(ico);
        stage.initStyle(StageStyle.UNDECORATED);
        assert root != null;
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Нас интересует ваше мнение");
        stage.initStyle(StageStyle.TRANSPARENT);
        DialogWindowController controller = loader.getController();
        controller.Initialization(stage, title, question, true);
        stage.showAndWait();
        return controller.getAnswer();
    }

    static public void showAlert(String text, String title){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(Dialogs.class.getResource("fxml/dialog_window.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(ico);
        stage.initStyle(StageStyle.UNDECORATED);
        assert root != null;
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Внимание! Внимание!");
        stage.initStyle(StageStyle.TRANSPARENT);
        DialogWindowController controller = loader.getController();
        controller.Initialization(stage, title, text, false);
        stage.showAndWait();
    }
}
