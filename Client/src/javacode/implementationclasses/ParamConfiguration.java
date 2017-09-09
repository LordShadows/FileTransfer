package javacode.implementationclasses;

import java.util.prefs.Preferences;

public class ParamConfiguration {
    public static int posXMainWindow;
    public static int posYMainWindow;
    public static boolean isMaximize;
    public static String login;
    public static String password;
    public static int passwordLength;

    //считывание сохраненных параметров
    public static void load() {
        Preferences prefs = Preferences.userRoot();
        posXMainWindow = prefs.getInt("posXMainWindow", 0);
        posYMainWindow = prefs.getInt("posYMainWindow", 0);
        isMaximize = prefs.getBoolean("isMaximize", false);
        login = prefs.get("login", "");
        password = prefs.get("password", "");
        passwordLength = prefs.getInt("passwordLength", 0);
    }

    //сохранение настроек
    public static void save(){
        Preferences prefs = Preferences.userRoot();
        prefs.putInt("posXMainWindow", posXMainWindow);
        prefs.putInt("posYMainWindow", posYMainWindow);
        prefs.putBoolean("isMaximize", isMaximize);
        prefs.put("login", login);
        prefs.put("password", password);
        prefs.putInt("passwordLength", passwordLength);
    }
}
