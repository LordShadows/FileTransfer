package javacode.classresources;


public class ListCellMessage {
    private String name;
    private String size;
    private String date;
    private String messagetext;
    private boolean isMine;
    private String path;

    public ListCellMessage(String name, String size, String date, boolean isMine, String path, String messagetext) {
        this.name = name;
        this.size = size;
        this.date = date;
        this.isMine = isMine;
        this.path = path;
        this.messagetext = messagetext;
    }

    public String getName() { return name; }

    public String getSize() { return size; }

    public String getDate() { return date; }

    public boolean isMine() { return isMine; }

    public String getPath() {
        return path;
    }

    public String getMessagetext() {
        return messagetext;
    }
}
