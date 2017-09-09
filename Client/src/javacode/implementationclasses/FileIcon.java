package javacode.implementationclasses;

import javafx.scene.image.Image;

public class FileIcon {
    private static String path = "/resources/img/fileicons/";
    
    public static Image getIcon(String nameFile) {
        Image icon;
        String fileType = nameFile.substring(nameFile.lastIndexOf('.') + 1);
        switch (fileType) {
            case "avi":
                icon = new Image(path + "avi" + ".png");
                break;
            case "css":
                icon = new Image(path + "css" + ".png");
                break;
            case "dll":
                icon = new Image(path + "dll" + ".png");
                break;
            case "doc":
                icon = new Image(path + "doc" + ".png");
                break;
            case "docx":
                icon = new Image(path + "docx" + ".png");
                break;
            case "eps":
                icon = new Image(path + "eps" + ".png");
                break;
            case "html":
                icon = new Image(path + "html" + ".png");
                break;
            case "jpg":
                icon = new Image(path + "jpg" + ".png");
                break;
            case "mp3":
                icon = new Image(path + "mp3" + ".png");
                break;
            case "pdf":
                icon = new Image(path + "pdf" + ".png");
                break;
            case "ppt":
                icon = new Image(path + "ppt" + ".png");
                break;
            case "pptx":
                icon = new Image(path + "ppt" + ".png");
                break;
            case "psd":
                icon = new Image(path + "psd" + ".png");
                break;
            case "txt":
                icon = new Image(path + "txt" + ".png");
                break;
            case "wav":
                icon = new Image(path + "wav" + ".png");
                break;
            case "xls":
                icon = new Image(path + "xls" + ".png");
                break;
            case "zip":
                icon = new Image(path + "zip" + ".png");
                break;
            default:
                icon = new Image(path + "file" + ".png");
        }
        return icon;
    }
}
