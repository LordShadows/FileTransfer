package javacode.classresources;

public class Contact {
    private String id;
    private String name;
    private byte[] avatar;
    private String lastMessage;
    private String date;

    public Contact(String id, String name, byte[] avatar, String lastMessage, String date) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.lastMessage = lastMessage;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getDate() {
        return date;
    }
}