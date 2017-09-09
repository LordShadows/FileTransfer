package javacode.classresources;

public class Message {
    private String filename;
    private String filesize;
    private String filepath;
    private boolean user;
    private String messagetext;
    private String date;

    public Message(String filename, String filesize, String filepath, boolean user, String messagetext, String date) {
        this.filename = filename;
        this.filesize = filesize;
        this.filepath = filepath;
        this.user = user;
        this.messagetext = messagetext;
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilesize() {
        return filesize;
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean getUser() {
        return user;
    }

    public String getMessagetext() {
        return messagetext;
    }

    public String getDate() {
        return date;
    }
}
