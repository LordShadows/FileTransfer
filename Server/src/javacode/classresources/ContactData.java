package javacode.classresources;

public class ContactData {
    private String nickname;
    private String name;
    private String email;
    private byte[] avatar;
    private String password;

    public ContactData(String nickname, String name, String email, byte[] avatar, String password) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public String getPassword() {
        return password;
    }
}
