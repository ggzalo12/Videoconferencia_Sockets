package com.zoomsockets.client;

public class ClientSession {
    private static ClientSession instance;
    private String email;
    private String name;
    private String currentRoomId;

    // Constructor privado (Patrón Singleton)
    private ClientSession() {}

    /**
     * Obtiene la única instancia global de la sesión del usuario actual.
     */
    public static synchronized ClientSession getInstance() {
        if (instance == null) {
            instance = new ClientSession();
        }
        return instance;
    }

    /**
     * Inicia la sesión guardando los datos básicos del usuario.
     */
    public void login(String email, String name) {
        this.email = email;
        this.name = name;
    }

    /**
     * Cierra la sesión limpiando los datos almacenados.
     */
    public void logout() {
        this.email = null;
        this.name = null;
        this.currentRoomId = null;
    }

    // Getters y Setters
    public String getEmail() { return email; }
    public String getName() { return name; }

    public String getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(String currentRoomId) { this.currentRoomId = currentRoomId; }
}