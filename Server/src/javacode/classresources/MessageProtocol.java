package javacode.classresources;

public class MessageProtocol {
    private String command;
    private Object[] parameters;

    public MessageProtocol(String command, Object[] parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    public String getCommand() {
        return command;
    }

    public Object[] getParameters() {
        return parameters;
    }
}