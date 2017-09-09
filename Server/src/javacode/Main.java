package javacode;

import javacode.controllers.MainWindowController;
import javacode.implementationclasses.ConnectToSQLite;
import javacode.implementationclasses.JavaServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Image ico = new Image("/resources/img/favicon.png");

        FXMLLoader connectLoader = new FXMLLoader(getClass().getResource("/resources/fxml/main_window.fxml"));
        Parent root = connectLoader.load();
        primaryStage.getIcons().add(ico);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root, Color.TRANSPARENT));
        primaryStage.setTitle("Server - File Transfer");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        MainWindowController connectController = connectLoader.getController();
        connectController.Initialization(primaryStage);
        primaryStage.show();

        JavaServer.LIST = connectController.contactsListView;

        try
        {
            final JavaServer processor = new JavaServer(4848);
            final Thread thread = new Thread(processor);
            thread.setDaemon(true);
            thread.start();
        }
        catch (IOException ignore){}

        ConnectToSQLite.testConn();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
