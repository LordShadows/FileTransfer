package javacode.classresources;

import javafx.scene.image.Image;

public class ListCellContact {
    private String id;
    private String name;
    private Image icon;
    private String lastMessage;
    private String date;

    public ListCellContact(String id, String name, Image icon, String lastMessage, String date) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.lastMessage = lastMessage;
        this.date = date;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Image getIcon() {
        return icon;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getDate() {
        return date;
    }
}
