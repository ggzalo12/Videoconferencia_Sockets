package com.zoomsockets.common;

public class ControlHeader {
    private String action;   // Ej: "LOGIN", "REGISTER", "CREATE_ROOM", "JOIN_ROOM", "CHAT", "FILE"
    private String email;
    private String password;
    private String name;
    private String roomId;
    private String message;
    private String fileName;
    private long fileSize;

    // Constructor vacío (obligatorio para que Gson funcione correctamente)
    public ControlHeader() {}

    // Constructor básico con la acción
    public ControlHeader(String action) {
        this.action = action;
    }

    // Getters y Setters para acceder y modificar los campos
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
}