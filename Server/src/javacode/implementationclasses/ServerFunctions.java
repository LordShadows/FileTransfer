package javacode.implementationclasses;

import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import javacode.classresources.Contact;
import javacode.classresources.ContactData;
import javacode.classresources.Message;
import javacode.classresources.MessageProtocol;
import javacode.controllers.MainWindowController;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class ServerFunctions {
    public static MainWindowController MWC;
    private static Gson gson = new Gson();

    static void addHistory(String record) {
        Platform.runLater(()->{
            MWC.historyListView.getItems().add(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss ").format(new Date())  + record);
            MWC.historyListView.scrollTo(MWC.historyListView.getItems().size() - 1);
        });
    }

    static void UpdateUserAccountData(String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о собственном аккаунте.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT * FROM users WHERE id = '" +  id + "'");
            if(rs.next()) {
                byte[] byteImage = extractBytes2("/upload/avatars/" + rs.getString("avatar") + "45.png");
                sp.sendMessage(new MessageProtocol("UpdateUserAccountData", new Object[] {rs.getString("name"), gson.toJson(byteImage)}));
            }
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void GetInfoAboutContact(String idContact, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации об аккаунте пользователя.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT * FROM contacts WHERE id = '" +  idContact + "'");
            assert rs != null;
            if(rs.next()) {
                ResultSet rss = ConnectToSQLite.selectQuery("SELECT * FROM users WHERE id = '" + (Objects.equals(rs.getString("firstuser"), id) ? rs.getString("seconduser") : rs.getString("firstuser")) + "'");
                assert rss != null;
                if(rss.next()) {
                    byte[] byteImage = extractBytes2("/upload/avatars/" + rss.getString("avatar") + "150.png");
                    sp.sendMessage(new MessageProtocol("GetInfoAboutContact", new Object[] { gson.toJson(new ContactData(rss.getString("nickname"), rss.getString("name"), rss.getString("mail"), byteImage, null)) }));
                }
            }
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void GetInfoAboutMe(String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о себе.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rss = ConnectToSQLite.selectQuery("SELECT * FROM users WHERE id = '" + id + "'");
            assert rss != null;
            if(rss.next()) {
                byte[] byteImage = extractBytes2("/upload/avatars/" + rss.getString("avatar") + "150.png");
                sp.sendMessage(new MessageProtocol("GetInfoAboutMe", new Object[] { gson.toJson(new ContactData(rss.getString("nickname"), rss.getString("name"), rss.getString("mail"), byteImage, null)) }));
            }
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void UpdateUserContactsData(String searchLine, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о контактах.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT contactID, name, avatar, " +
                    "(SELECT text FROM (SELECT '' as text, date FROM file_messages WHERE contact = contactID UNION SELECT text, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'text', " +
                    "(SELECT filename FROM (SELECT filename, date FROM file_messages WHERE contact = contactID UNION SELECT '' as filename, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'filename', " +
                    "(SELECT user FROM (SELECT user, date FROM file_messages WHERE contact = contactID UNION SELECT user, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'user', " +
                    "(SELECT date FROM (SELECT date FROM file_messages WHERE contact = contactID UNION SELECT date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'date' " +
                    "FROM ( " +
                    "SELECT * FROM ( SELECT DISTINCT firstuser 'user', id 'contactID' " +
                    "FROM contacts WHERE seconduser = '" + id + "' " +
                    "UNION SELECT DISTINCT seconduser 'user', id 'contactID' " +
                    "FROM contacts WHERE firstuser = '" + id + "' " +
                    ") selectContacts " +
                    "INNER JOIN users ON selectContacts.user = users.id " +
                    ") selectUsers " +
                    (!Objects.equals(searchLine, "") ? "WHERE search LIKE '%" + searchLine.toLowerCase() + "%' " : "") +
                    "ORDER BY name");
            ArrayList<Contact> contacts = new ArrayList<>();
            assert rs != null;
            while (rs.next()) {
                byte[] byteImage = extractBytes2("/upload/avatars/" + rs.getString("Avatar") + "45.png");
                contacts.add(
                        new Contact(rs.getString("contactID"), rs.getString("name"), byteImage, (Objects.equals(rs.getString("user"), id) ? "Я: " : "") + (Objects.equals(rs.getString("text"), "") ? rs.getString("filename") : rs.getString("text")), rs.getString("date"))
                );
            }
            sp.sendMessage(new MessageProtocol("UpdateUserContactsData", new Object[]{ new Gson().toJson(contacts) }));
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void UpdateContactMessages(String idContact, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о сообщениях.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT * FROM " +
                            "(SELECT contact, user, '' as text, filename, filesize, filepath, date FROM file_messages WHERE contact = '" + idContact + "' " +
                            "UNION " +
                            "SELECT contact, user, text, '' as filename, '' as filesize, '' as filepath, date FROM text_messages WHERE contact = '" + idContact + "') " +
                            "ORDER BY datetime(date)");
            ArrayList<Message> messages = new ArrayList<>();
            assert rs != null;
            while(rs.next()) {
                messages.add(
                        new Message(rs.getString("filename"), rs.getString("filesize"), rs.getString("filepath"), Objects.equals(rs.getString("user"), id), rs.getString("text"), rs.getString("date"))
                );
            }
            sp.sendMessage(new MessageProtocol("UpdateContactMessages", new Object[]{ gson.toJson(messages) }));
        } catch (SQLException  e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void MessageFileToServer(String idContact, String name, double length, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Отправка файла.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        String serverFileName = randomFileName(name);
        String serverFilePath = new File("").getAbsolutePath() + "/upload/files/" + serverFileName;
        long lengthFile = (long)length;
        File file = new File(serverFilePath);
        try {
            file.createNewFile();
            byte[] byteArray = new byte[1024];
            FileOutputStream fos = new FileOutputStream(file);
            while (lengthFile > 0) {
                int i = sp.socketReader.read(byteArray);
                fos.write(byteArray, 0, i);
                lengthFile-= i;
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConnectToSQLite.update("INSERT INTO file_messages (contact, user, filename, filesize, filepath, date) " +
                "VALUES ('" + idContact + "', " +
                "'" + id + "', " +
                "'" + name + "', " +
                "'" + stringSize((long)length) + "', " +
                "'" + serverFileName + "', " +
                "datetime('now', 'localtime'))");
        UpdateContactMessages(idContact, id, sp);
    }

    static void MessageTextToServer(String idContact, String text, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Отправка сообщения.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        ConnectToSQLite.update("INSERT INTO text_messages (contact, user, text, date) " +
                "VALUES ('" + idContact + "', " +
                "'" + id + "', " +
                "'" + text + "', " +
                "datetime('now', 'localtime'))");
        UpdateContactMessages(idContact, id, sp);
    }

    static void UpdateUserAccount(ContactData contact, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Редактирование аккаунта.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        String newAvatarName = null;
        if(contact.getAvatar() != null){
            newAvatarName = saveAvatar(contact.getAvatar());
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT * FROM users WHERE id = '" +  id + "'");
            try {
                assert rs != null;
                if(rs.next()) {
                    deleteAvatar( rs.getString("avatar"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String command = (contact.getNickname() != null ? "nickname = '" + contact.getNickname() + "', " : "") +
                (contact.getName() != null ? "name = '" + contact.getName() + "', " : "") +
                (contact.getEmail()!= null ? "mail = '" + contact.getEmail() + "', " : "") +
                (contact.getPassword() != null ? "password = '" + contact.getPassword() + "', " : "") +
                (newAvatarName != null ? "avatar = '" + newAvatarName + "', " : "");
        command = command.substring(0, command.length() - 2);
        ConnectToSQLite.update("UPDATE users SET " +
                 command +
                " WHERE id = '" + id + "'");
        UpdateUserAccountData(id, sp);
    }

    static String AddUserAccount(ContactData contact) {
        addHistory("#N/A >>> Добавление аккаунта.");
        try {
            String newAvatarName = saveAvatar(contact.getAvatar());
            return ConnectToSQLite.updateWithId("INSERT INTO users (nickname, name, mail, password, avatar, search) VALUES (" +
                    "'" + contact.getNickname() + "', " +
                    "'" + contact.getName() + "', " +
                    "'" + contact.getEmail() + "', " +
                    "'" + contact.getPassword() + "', " +
                    "'" + newAvatarName + "', " +
                    "'" + contact.getNickname().toLowerCase() + " " + contact.getName().toLowerCase() + "'" +
                    ")");
        } catch (Exception e) {
            if(e.toString().contains("column nickname is not unique")) return "n/u";
            ServerFunctions.addHistory("#ERROR >>> Ошибка при добавлении пользователя: " + e.toString());
            return "err";
        }
    }

    static void UpdateMyContactOnEditPane(String searchLine, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о своих контактах (для редактирования).");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT contactID, name, avatar, " +
                    "(SELECT text FROM (SELECT '' as text, date FROM file_messages WHERE contact = contactID UNION SELECT text, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'text', " +
                    "(SELECT filename FROM (SELECT filename, date FROM file_messages WHERE contact = contactID UNION SELECT '' as filename, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'filename', " +
                    "(SELECT user FROM (SELECT user, date FROM file_messages WHERE contact = contactID UNION SELECT user, date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'user', " +
                    "(SELECT date FROM (SELECT date FROM file_messages WHERE contact = contactID UNION SELECT date FROM text_messages WHERE contact = contactID) ORDER BY datetime(date) DESC) 'date' " +
                    "FROM ( " +
                    "SELECT * FROM ( SELECT DISTINCT firstuser 'user', id 'contactID' " +
                    "FROM contacts WHERE seconduser = '" + id + "' " +
                    "UNION SELECT DISTINCT seconduser 'user', id 'contactID' " +
                    "FROM contacts WHERE firstuser = '" + id + "' " +
                    ") selectContacts " +
                    "INNER JOIN users ON selectContacts.user = users.id " +
                    ") selectUsers " +
                    (!Objects.equals(searchLine, "") ? "WHERE search LIKE '%" + searchLine.toLowerCase() + "%' " : "") +
                    "ORDER BY name");
            ArrayList<Contact> contacts = new ArrayList<>();
            assert rs != null;
            while (rs.next()) {
                byte[] byteImage = extractBytes2("/upload/avatars/" + rs.getString("Avatar") + "45.png");
                contacts.add(
                        new Contact(rs.getString("contactID"), rs.getString("name"), byteImage, (Objects.equals(rs.getString("user"), id) ? "Я: " : "") + (Objects.equals(rs.getString("text"), "") ? rs.getString("filename") : rs.getString("text")), rs.getString("date"))
                );
            }
            sp.sendMessage(new MessageProtocol("UpdateMyContactOnEditPane", new Object[]{ new Gson().toJson(contacts) }));
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void UpdateAllContactOnEditPane(String searchLine, String id, JavaServer.SocketProcessor sp) {
        addHistory(sp.getIp() + " >>> Получение информации о своих контактах (для редактирования).");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet rs = ConnectToSQLite.selectQuery("SELECT user as contactID, name, avatar, '' as text, '' as filename, '' as user, '' as date " +
                    "FROM (" +
                    "SELECT nickname, id as 'user', name, avatar FROM users " +
                    "EXCEPT " +
                    "SELECT nickname, user, name, avatar FROM ( " +
                    "SELECT * FROM( " +
                    "SELECT DISTINCT firstuser 'user', id 'contactID' " +
                    "FROM contacts WHERE seconduser = '1' " +
                    "UNION SELECT DISTINCT seconduser 'user', id 'contactID' " +
                    "FROM contacts WHERE firstuser = '1' " +
                    ") selectContacts " +
                    "INNER JOIN users ON selectContacts.user = users.id " +
                    ")cont " +
                    ") selectUsers " +
                    (!Objects.equals(searchLine, "") ? "WHERE search LIKE '%" + searchLine.toLowerCase() + "%' " : "") +
                    "ORDER BY name");
            ArrayList<Contact> contacts = new ArrayList<>();
            assert rs != null;
            while (rs.next()) {
                byte[] byteImage = extractBytes2("/upload/avatars/" + rs.getString("Avatar") + "45.png");
                contacts.add(
                        new Contact(rs.getString("contactID"), rs.getString("name"), byteImage, (Objects.equals(rs.getString("user"), id) ? "Я: " : "") + (Objects.equals(rs.getString("text"), "") ? rs.getString("filename") : rs.getString("text")), rs.getString("date"))
                );
            }
            Predicate<Contact> contactPredicate = p-> Objects.equals(p.getId(), id);
            contacts.removeIf(contactPredicate);
            sp.sendMessage(new MessageProtocol("UpdateAllContactOnEditPane", new Object[]{ new Gson().toJson(contacts) }));
        } catch (SQLException | IOException | URISyntaxException e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void AddContact(String user, String id, JavaServer.SocketProcessor sp) {
        addHistory(" >>> Добавление контакта.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ConnectToSQLite.update("INSERT INTO contacts (firstuser, seconduser) VALUES (" +
                    "'" + id + "', " +
                    "'" + user + "'" +
                    ")");
        } catch (Exception e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    static void DeleteContact(String contact, String id, JavaServer.SocketProcessor sp) {
        addHistory(" >>> Удаление контакта.");
        if(!sp.isLogIn()) { setAlarm(sp); return; }
        try {
            ResultSet messages = ConnectToSQLite.selectQuery("SELECT * FROM " +
                    "(SELECT '' as text, filepath FROM file_messages WHERE contact = '" + contact + "' " +
                    "UNION " +
                    "SELECT text, '' as filepath FROM text_messages WHERE contact = '" + contact + "') ");
            assert messages != null;
            while(messages.next()){
                if(Objects.equals(messages.getString("text"), "")){
                    deleteFile(messages.getString("filepath"));
                }
            }
            ConnectToSQLite.update("DELETE FROM file_messages WHERE contact = '" + contact + "'");
            ConnectToSQLite.update("DELETE FROM text_messages WHERE contact = '" + contact + "'");
            ConnectToSQLite.update("DELETE FROM contacts WHERE id = '" + contact + "'");
        } catch (Exception e) {
            addHistory("#ERROR >>> Ошибка функции обработки данных пользователя: " + e.toString());
            sp.setMark("ERROR");
            Platform.runLater(sp::list);
            e.printStackTrace();
        }
    }

    private static void setAlarm(JavaServer.SocketProcessor sp) {
        addHistory("#ALARM >>> Доступ без прав. Пользователь: " + sp.getIp() + ".");
        sp.setMark("ALARM");
        Platform.runLater(sp::list);
    }

    private static String saveAvatar(byte[] byteAvatar){
        if(byteAvatar == null) return "Default";
        String nameAvatar = randomAvatarName();
        try {
            BufferedImage fromBytes150 = ImageIO.read(new ByteInputStream(byteAvatar, byteAvatar.length));
            BufferedImage fromBytes45 = subImage(fromBytes150, 45);
            ImageIO.write(fromBytes150, "png", new File(new File("").getAbsolutePath() + "/upload/avatars/" + nameAvatar + "150.png"));
            ImageIO.write(fromBytes45, "png", new File(new File("").getAbsolutePath() + "/upload/avatars/" + nameAvatar + "45.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nameAvatar;
    }

    private static void deleteAvatar(String name) {
        new File(new File("").getAbsolutePath() + "/upload/avatars/" + name + "150.png").delete();
        new File(new File("").getAbsolutePath() + "/upload/avatars/" + name + "45.png").delete();
    }

    private static void deleteFile(String name) {
        new File(new File("").getAbsolutePath() + "/upload/files/" + name).delete();
    }

    private static String randomAvatarName(){
        String filename;
        do {
            UUID id = UUID.randomUUID();
            filename = id.toString().replaceAll("-", "");
        } while (new File(new File("").getAbsolutePath() + "/upload/avatars/" + filename + "150.png").exists());
        return filename;
    }

    private static String randomFileName(String fileName){
        String filename;
        do {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            filename = id1.toString().replaceAll("-", "") + id2.toString().replaceAll("-", "");
        } while (new File(new File("").getAbsolutePath() + "/upload/files/" + filename + fileName.substring(fileName.lastIndexOf('.'))).exists());
        return filename  + fileName.substring(fileName.lastIndexOf('.'));
    }

    private static byte[] extractBytes2(String ImageName) throws IOException, URISyntaxException {
        File imgPath = new File(new File("").getAbsolutePath() + ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        ByteOutputStream bos = null;
        try {
            bos = new ByteOutputStream();
            ImageIO.write(bufferedImage, "png", bos);
        } finally {
            try {
                assert bos != null;
                bos.close();
            } catch (Exception ignored) {
            }
        }
        return bos.getBytes();
    }

    private static BufferedImage subImage(BufferedImage image, int size){
        BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        Image scaled;
        if(image.getWidth() > image.getHeight()){
            scaled = image.getSubimage((image.getWidth() - image.getHeight()) / 2, 0, image.getHeight(), image.getHeight()).getScaledInstance(size, size, Image.SCALE_SMOOTH);
        }
        else {
            scaled = image.getSubimage(0, (image.getHeight() - image.getWidth()) / 2, image.getWidth(), image.getWidth()).getScaledInstance(size, size, Image.SCALE_SMOOTH);
        }
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return resizedImage;
    }

    private static String stringSize(long size){
        if(size < 1000) {
            return String.valueOf(size) + " Б";
        } else if (size < 1000000) {
            return String.format("%.1f", (float)size/1000) + " КБ";
        } else if (size < 1000000000) {
            return String.format("%.1f", (float)size/1000000) + " МБ";
        } else {
            return String.format("%.1f", (float)size/1000000000) + " ГБ";
        }
    }
}
