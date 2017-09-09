package javacode.implementationclasses;

import java.sql.*;

public class ConnectToSQLite {
    public static Connection conn;
    public static void testConn() {
        try {
            Class.forName("org.sqlite.JDBC");
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:database.db");
                Statement statmt = conn.createStatement();
                statmt.executeQuery("SELECT * FROM users");
                ServerFunctions.addHistory("#DATABASE >>> База данных подключена и готова к работе.");
            } catch (SQLException e) {
                ServerFunctions.addHistory("#DATABASE >>> Тест провален. Не удалось получить доступ к базе данных. Ощибка: " + e.toString());
            }
        } catch (ClassNotFoundException ignore) { ServerFunctions.addHistory("#ERROR >>> " + ignore.getMessage()); }
    }

    static ResultSet selectQuery(String query) {
        try {
            Statement statmt = conn.createStatement();
            return statmt.executeQuery(query);
        } catch (SQLException e) {
            ServerFunctions.addHistory("#ERROR >>> Ошибка работы с базой данных: " + e.toString());
            return null;
        }
    }

    static void update(String query) {
        try (Statement statmt = conn.createStatement()) {
            statmt.execute(query);
        } catch (SQLException e) {
            ServerFunctions.addHistory("#ERROR >>> Ошибка работы с базой данных: " + e.toString());
        }
    }

    static String updateWithId(String query) throws SQLException {
        try (Statement statmt = conn.createStatement()) {
            statmt.execute(query);
            ResultSet rs = statmt.executeQuery("SELECT last_insert_rowid()");
            if(rs.next()) return rs.getString(1);
        }
        return null;
    }
}
