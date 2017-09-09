package javacode.implementationclasses;

import com.google.gson.Gson;
import javacode.classresources.ContactData;
import javacode.classresources.MessageProtocol;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JavaServer implements Runnable{
    private final ServerSocket ss;
    public static ListView LIST;

    private BlockingQueue<SocketProcessor> q = new LinkedBlockingQueue<>();

    /**
     * Конструктор объекта сервера
     */
    public JavaServer(int port) throws IOException {
        ss = new ServerSocket(port);
        ServerFunctions.addHistory("#SYSTEM >>> Сервер готов к работе. Порт: " + ss.getLocalPort() + ".");
    }

    /**
     * Главный цикл прослушивания/ожидания коннекта.
     */
    public void run() {
        Thread serverThread = Thread.currentThread();
        while (true) {
            Socket s = getNewConn();
            if (serverThread.isInterrupted()) {

                break;
            } else if (s != null){
                try {
                    final SocketProcessor processor = new SocketProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    q.offer(processor);
                }
                catch (IOException ignored) {}
            }
        }
    }

    /**
     * Ожидает новое подключение.
     */
    private Socket getNewConn() {
        Socket s = null;
        try {
            s = ss.accept();
        } catch (IOException e) {
            shutdownServer();
        }
        return s;
    }

    /**
     * метод "глушения" сервера
     */
    private synchronized void shutdownServer() {
        q.forEach(SocketProcessor::close);
        if (!ss.isClosed()) {
            try {
                ss.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Вложенный класс асинхронной обработки одного коннекта.
     */
    public class SocketProcessor implements Runnable{
        private Socket socket;
        public DataInputStream socketReader;
        private DataOutputStream socketWriter;
        private String name = "";
        private String id = "";
        private String ip = "";
        private String mark = "";
        private boolean isLogIn = false;
        Gson gson = new Gson();
        int BUFFERSIZE = 32768;

        /**
         * Сохраняем сокет, пробуем создать читателя и писателя. Если не получается - вылетаем без создания объекта
         */
        SocketProcessor(Socket socketParam) throws IOException {
            socket = socketParam;
            socketWriter = new DataOutputStream(socket.getOutputStream());
            socketReader = new DataInputStream(socket.getInputStream());
            ip = socket.getInetAddress().toString().substring(1) + ":" + socket.getPort();
        }

        String getName()
        {
            return this.name;
        }

        String getIp()
        {
            return this.ip;
        }

        String getMark() {
            return this.mark;
        }

        boolean isLogIn() {
            return this.isLogIn;
        }

        void setMark(String mark) {
            this.mark = mark;
        }

        /**
         * Главный цикл чтения сообщений/рассылки
         */
        @Override
        @SuppressWarnings("StringEquality")
        public void run() {
            String line = null;
            MessageProtocol message;

            try {
                line = socketReader.readUTF();
            } catch (IOException e) {
                close(); // если не получилось - закрываем сокет.
            }
            if(line != null){
                message = gson.fromJson(line, MessageProtocol.class);
                if(Objects.equals(message.getCommand(), "LogIn")) {
                    try {
                        ResultSet rs = ConnectToSQLite.selectQuery("SELECT * FROM users WHERE nickname = '" + message.getParameters()[0].toString() + "' AND password = '" + message.getParameters()[1].toString() + "'");
                        assert rs != null;
                        if (!rs.next()) {
                            sendMessage(new MessageProtocol("Authorization", new Object[]{"No"}));
                            close();
                        } else {
                            name = rs.getString("nickname") + " " + rs.getString("name");
                            id = rs.getString("id");
                            isLogIn = true;
                            sendMessage(new MessageProtocol("Authorization", new Object[]{"Yes"}));
                            ServerFunctions.addHistory("#SERVER >>> Подключен новый пользователь " + name + ".");
                            Platform.runLater(this::list);
                        }
                    } catch (SQLException sqlE) {
                        ServerFunctions.addHistory("#ERROR >>> " + sqlE.toString());
                        sqlE.printStackTrace();
                    }
                } else if (Objects.equals(message.getCommand(), "AddUser")) {
                    ContactData contactData = (gson.fromJson((String)message.getParameters()[0], ContactData.class));
                    String bufId = ServerFunctions.AddUserAccount(contactData);
                    switch (bufId){
                        case "err":
                            sendMessage(new MessageProtocol("Authorization", new Object[]{"No"}));
                            close();
                            break;
                        case "n/u":
                            sendMessage(new MessageProtocol("Authorization", new Object[]{"N/U"}));
                            close();
                            break;
                        default:
                            name = contactData.getNickname() + " " + contactData.getName();
                            id = bufId;
                            isLogIn = true;
                            sendMessage(new MessageProtocol("Authorization", new Object[]{"Yes"}));
                            ServerFunctions.addHistory("#SERVER >>> Подключен новый пользователь " + name + ".");
                            Platform.runLater(this::list);
                            break;
                    }
                }
            } else { close(); }

            while (!socket.isClosed()) {
                line = null;
                try {
                    line = socketReader.readUTF(); // Принимаем сообщение
                } catch (IOException e) {
                    ServerFunctions.addHistory("#SERVER >>> Отключен пользователь " + name + ".");
                    Platform.runLater(this::list);
                    close(); // если не получилось - закрываем сокет.
                }

                if(line == null) continue;
                message = gson.fromJson(line, MessageProtocol.class);

                switch (message.getCommand()) {
                    case "UpdateUserAccountData": {
                        ServerFunctions.UpdateUserAccountData(id, this);
                        break;
                    }
                    case "UpdateUserContactsData": {
                        ServerFunctions.UpdateUserContactsData((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "UpdateContactMessages": {
                        ServerFunctions.UpdateContactMessages((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "MessageFileToServer": {
                        ServerFunctions.MessageFileToServer((String)message.getParameters()[0], (String)message.getParameters()[1], (double)message.getParameters()[2], id, this);
                        break;
                    }
                    case "MessageTextToServer": {
                        ServerFunctions.MessageTextToServer((String)message.getParameters()[0], (String)message.getParameters()[1], id, this);
                        break;
                    }
                    case "GetInfoAboutContact": {
                        ServerFunctions.GetInfoAboutContact((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "GetInfoAboutMe": {
                        ServerFunctions.GetInfoAboutMe(id, this);
                        break;
                    }
                    case "UpdateUserAccount": {
                        ServerFunctions.UpdateUserAccount(gson.fromJson((String)message.getParameters()[0], ContactData.class), id, this);
                        break;
                    }
                    case "UpdateMyContactOnEditPane": {
                        ServerFunctions.UpdateMyContactOnEditPane((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "UpdateAllContactOnEditPane": {
                        ServerFunctions.UpdateAllContactOnEditPane((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "AddContact": {
                        ServerFunctions.AddContact((String)message.getParameters()[0], id, this);
                        break;
                    }
                    case "DeleteContact": {
                        ServerFunctions.DeleteContact((String)message.getParameters()[0], id, this);
                        break;
                    }
                    default:
                        ServerFunctions.addHistory("#ERROR >>> Недопустимая команда (Система обработки входящих сообщений).");
                        setMark("ERROR");
                        Platform.runLater(this::list);
                        break;
                }
            }
        }

        /**
         * Метод посылает в сокет полученную строку
         * @param line строка на отсылку
         */
        synchronized void send(String line) {
            try {
                socketWriter.writeUTF(line);
                socketWriter.flush();
            } catch (IOException e) {
                close();
            }
        }

        synchronized void sendMessage(MessageProtocol message){
            Gson gson = new Gson();
            String messageLine = gson.toJson(message);
            for(int index = 0; true; index += BUFFERSIZE){
                if(index + BUFFERSIZE >= messageLine.length()){
                    send(messageLine.substring(index, messageLine.length()));
                    break;
                } else {
                    send(messageLine.substring(index, index + BUFFERSIZE));
                }
            }
            send("END");
        }

        synchronized void list() {
            LIST.getItems().clear();
            for (SocketProcessor sp: q) {
                LIST.getItems().add(sp.getIp() + " " + sp.getName() + " " + sp.getMark());
            }
        }

        public synchronized void sendMessageTo(MessageProtocol message, String id){
            for (SocketProcessor sp: q) {
                if(Objects.equals(sp.getIp(), id)){
                    sp.sendMessage(message);
                    break;
                }
            }
        }

        /**
         * метод аккуратно закрывает сокет и убирает его со списка активных сокетов
         */
        synchronized void close() {
            q.remove(this);
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        /**
         * Финализатор
         */
        @Override
        @SuppressWarnings("FinalizeDeclaration")
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }
    }
}
