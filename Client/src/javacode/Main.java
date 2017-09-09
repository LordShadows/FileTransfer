package javacode;

import javacode.controllers.LogInWindowController;
import javacode.controllers.MainWindowController;
import javacode.implementationclasses.ParamConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Image ico = new Image("/resources/img/favicon.png");
        ParamConfiguration.load();

        // Приветствуйте форму авторизации
        Stage loginStage = new Stage();
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/resources/fxml/login_window.fxml"));
        Parent loginRoot = loginLoader.load();
        loginStage.getIcons().add(ico);
        loginStage.initStyle(StageStyle.UNDECORATED);
        Scene loginScene = new Scene(loginRoot, Color.TRANSPARENT);
        loginStage.setScene(loginScene);
        loginStage.setTitle("Подключение к серверу");
        loginStage.initStyle(StageStyle.TRANSPARENT);
        LogInWindowController logInController = loginLoader.getController();
        logInController.Initialization(loginStage);
        loginStage.showAndWait();


        FXMLLoader connectLoader = new FXMLLoader(getClass().getResource("/resources/fxml/main_window.fxml"));
        Parent root = connectLoader.load();
        primaryStage.getIcons().add(ico);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root, Color.TRANSPARENT));
        primaryStage.setTitle("File Transfer");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        MainWindowController connectController = connectLoader.getController();
        connectController.Initialization(primaryStage);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
