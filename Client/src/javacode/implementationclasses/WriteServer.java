package javacode.implementationclasses;

import dialogs.Dialogs;
import javacode.classresources.Contact;
import javacode.classresources.ContactData;
import javacode.classresources.Message;
import javacode.classresources.MessageProtocol;
import javacode.controllers.AccountWindowController;
import javacode.controllers.LogInWindowController;
import javafx.application.Platform;
import com.google.gson.Gson;
import java.net.*;
import java.io.*;
import java.util.Objects;

public class WriteServer {

    private Socket socket;
    private DataInputStream socketReader;
    public DataOutputStream socketWriter;
    private Gson gson = new Gson();

    public WriteServer(String host, int port, String login, String password, LogInWindowController LIWC){
        try
        {
            socket = new Socket(host, port);
            socketWriter = new DataOutputStream(socket.getOutputStream());
            socketReader = new DataInputStream(socket.getInputStream());
            MessageProtocol message = new MessageProtocol("LogIn", new Object[] { login, password  });
            sendMessage(message);

            String line = null;
            try {
                line = socketReader.readUTF();
            } catch (IOException e) {
                Platform.runLater(()-> Dialogs.showError("Ошибка возможна, если указан не верный IP-адрес или серврер в данный момент не работает!", "Ошибка подключения к серверу!!!"));
                Platform.runLater(()->{
                    LIWC.buttonConnect.setDisable(false);
                    LIWC.buttonAddUser.setDisable(false);
                });
            }

            message = gson.fromJson(line, MessageProtocol.class);
            if(!Objects.equals(message.getCommand(), "Authorization") || !Objects.equals(message.getParameters()[0].toString(), "Yes")){
                Platform.runLater(()-> Dialogs.showError("Пароль или логин не верны.", "Указан не верный логин или пароль."));
                Platform.runLater(()->{
                    LIWC.buttonConnect.setDisable(false);
                    LIWC.buttonAddUser.setDisable(false);
                });
                close();
                return;
            }

            Platform.runLater(()-> {
                LIWC.isAuthorizate = true;
                LIWC.STAGE.close();
            });

            new Thread(new Receiver()).start();// создаем и запускаем нить асинхронного чтения из сокета
        } catch (IOException e) { // если объект не создан...
            Platform.runLater(()-> Dialogs.showError("Ошибка возможна, если указан не верный IP-адрес или серврер в данный момент не работает!", "Ошибка подключения к серверу!!!"));
            Platform.runLater(()->{
                LIWC.buttonConnect.setDisable(false);
                LIWC.buttonAddUser.setDisable(false);
            });
        }
    }

    public WriteServer(String host, int port, ContactData contactData, LogInWindowController LIWC, AccountWindowController AWC){
        try
        {
            socket = new Socket(host, port);
            socketWriter = new DataOutputStream(socket.getOutputStream());
            socketReader = new DataInputStream(socket.getInputStream());
            MessageProtocol message = new MessageProtocol("AddUser", new Object[] { gson.toJson(contactData) });
            sendMessage(message);

            String line = null;
            try {
                line = socketReader.readUTF();
            } catch (IOException e) {
                Platform.runLater(()-> Dialogs.showError("Ошибка возможна, если указан не верный IP-адрес или серврер в данный момент не работает!", "Ошибка подключения к серверу!!!"));
                Platform.runLater(()->{
                    AWC.buttonAddAccount.setDisable(true);
                    AWC.buttonCancel.setDisable(true);
                });
            }

            message = gson.fromJson(line, MessageProtocol.class);
            if(Objects.equals(message.getCommand(), "Authorization")){
                switch (message.getParameters()[0].toString()){
                    case "Yes":
                        Platform.runLater(()-> {
                            LIWC.isAuthorizate = true;
                            AWC.STAGE.close();
                            LIWC.STAGE.close();
                        });

                        new Thread(new Receiver()).start(); // создаем и запускаем нить асинхронного чтения из сокета
                        break;
                    case "N/U":
                        Platform.runLater(()-> Dialogs.showAlert("Такой никнейм уже занят другим пользователем. Попробуйте еще раз.", "Никнейм уже существует!"));
                        Platform.runLater(()->{
                            AWC.buttonAddAccount.setDisable(false);
                            AWC.buttonCancel.setDisable(false);
                        });
                        close();
                        break;
                    default:
                        Platform.runLater(()-> Dialogs.showError("Возникли какие-то проблемы. Попробуйте еще раз.", "Что-то пошло не так!"));
                        Platform.runLater(()->{
                            AWC.buttonAddAccount.setDisable(false);
                            AWC.buttonCancel.setDisable(false);
                        });
                        close();
                        break;
                }
            }
        } catch (IOException e) { // если объект не создан...
            Platform.runLater(()-> Dialogs.showError("Ошибка возможна, если указан не верный IP-адрес или серврер в данный момент не работает!", "Ошибка подключения к серверу!!!"));
            Platform.runLater(()->{
                LIWC.buttonConnect.setDisable(false);
                LIWC.buttonAddUser.setDisable(false);
            });
        }
    }

    private void writeLine(String userString) {
        try {
            socketWriter.writeUTF(userString); //пишем строку пользователя
            socketWriter.flush(); // отправляем
        } catch (IOException e) {
            Platform.runLater(()->{
                Dialogs.showError("Возможно работа сервера была нарушена! Презапустите приложение и попробуйте снова!", "Ошибка соединения с сервером!!!");
                System.exit(0);
            });
        }
    }

    void sendMessage(MessageProtocol message) {
        writeLine(gson.toJson(message));
    }

    private void close() {
        if (!socket.isClosed()) { // проверяем, что сокет не закрыт...
            try {
                socket.close(); // закрываем...
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private class Receiver implements Runnable{
        /**
         * run() вызовется после запуска нити из конструктора клиента.
         */
        @Override
        public void run() {
            while (!socket.isClosed()) {
                StringBuilder line = new StringBuilder();
                String buffer;
                try {
                    do{
                        buffer = socketReader.readUTF();
                        if(!Objects.equals(buffer, "END")) line.append(buffer);
                    } while (!Objects.equals(buffer, "END"));
                } catch (Exception e) {
                    if ("Socket closed".equals(e.getMessage())) {
                        break;
                    }
                    Platform.runLater(()-> {
                        Dialogs.showError("Возникли непредвиденные неполадки и соединение было потеряно. Приложение будет закрыто.", "Потеряно соединение с сервером!!!");
                        System.exit(0);
                    });
                    return;
                }
                if (!Objects.equals(line.toString(), "")) {
                    MessageProtocol message = gson.fromJson(line.toString(), MessageProtocol.class);

                    switch (message.getCommand()) {
                        case "UpdateUserAccountData": {
                            ClientFunctions.UpdateUserAccountData((String)message.getParameters()[0], gson.fromJson((String)message.getParameters()[1], byte[].class));
                            break;
                        }
                        case "UpdateUserContactsData": {
                            ClientFunctions.UpdateUserContactsData(gson.fromJson((String)message.getParameters()[0], Contact[].class));
                            break;
                        }
                        case "UpdateContactMessages": {
                            ClientFunctions.UpdateContactMessages(gson.fromJson((String)message.getParameters()[0], Message[].class));
                            break;
                        }
                        case "GetInfoAboutContact": {
                            ClientFunctions.GetInfoAboutContact(gson.fromJson((String)message.getParameters()[0], ContactData.class));
                            break;
                        }
                        case "GetInfoAboutMe": {
                            ClientFunctions.GetInfoAboutMe(gson.fromJson((String)message.getParameters()[0], ContactData.class));
                            break;
                        }
                        case "UpdateMyContactOnEditPane": {
                            ClientFunctions.UpdateContactOnEditPane(gson.fromJson((String)message.getParameters()[0], Contact[].class));
                            break;
                        }
                        case "UpdateAllContactOnEditPane": {
                            ClientFunctions.UpdateContactOnEditPane(gson.fromJson((String)message.getParameters()[0], Contact[].class));
                            break;
                        }
                    }
                }
            }
        }
    }
}