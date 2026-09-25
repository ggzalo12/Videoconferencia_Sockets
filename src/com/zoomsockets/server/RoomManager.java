package com.zoomsockets.server;

import com.zoomsockets.common.NetworkFrame;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    // Mapa que asocia un ID de sala con el conjunto de manejadores de clientes conectados a ella
    private static final ConcurrentHashMap<String, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();

    /**
     * Añade un cliente a una sala específica.
     */
    public static synchronized void addUserToRoom(String roomId, ClientHandler client) {
        rooms.putIfAbsent(roomId, Collections.synchronizedSet(new HashSet<>()));
        rooms.get(roomId).add(client);
        System.out.println("➕ Cliente añadido a la sala activa: " + roomId);
    }

    /**
     * Remueve un cliente de una sala (cuando se desconecta o sale).
     */
    public static synchronized void removeUserFromRoom(String roomId, ClientHandler client) {
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(client);
            if (rooms.get(roomId).isEmpty()) {
                rooms.remove(roomId); // Limpiar sala vacía
            }
        }
    }

    /**
     * Envía una trama (mensaje/datos) a todos los usuarios de una sala excepto al remitente.
     */
    public static void broadcastToRoom(String roomId, NetworkFrame frame, ClientHandler sender) {
        Set<ClientHandler> clients = rooms.get(roomId);
        if (clients != null) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != sender) {
                        client.sendFrame(frame);
                    }
                }
            }
        }
    }
}