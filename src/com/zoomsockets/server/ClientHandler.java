package com.zoomsockets.server;

import com.zoomsockets.common.ControlHeader;
import com.zoomsockets.common.NetworkFrame;
import com.zoomsockets.common.ProtocolStreamer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private UsuarioDAO usuarioDAO;
    private SalaDAO salaDAO;
    private String loggedUserEmail;
    private String currentRoomId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.usuarioDAO = new UsuarioDAO();
        this.salaDAO = new SalaDAO();
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            NetworkFrame frame;
            // Ciclo para escuchar tramas enviadas por este cliente vía TCP
            while ((frame = ProtocolStreamer.readFrame(in)) != null) {
                ControlHeader header = ProtocolStreamer.readHeader(frame);
                if (header == null || header.getAction() == null) continue;

                System.out.println("📥 Acción recibida de [" + socket.getRemoteSocketAddress() + "]: " + header.getAction());

                // Procesar la acción solicitada (exclusivo para control y chat)
                switch (header.getAction()) {
                    case "REGISTER":
                        handleRegister(header);
                        break;
                    case "LOGIN":
                        handleLogin(header);
                        break;
                    case "CREATE_ROOM":
                        handleCreateRoom(header);
                        break;
                    case "JOIN_ROOM":
                        handleJoinRoom(header);
                        break;
                    case "CHAT_MESSAGE":
                        handleChatMessage(header, frame);
                        break;
                    default:
                        System.out.println("⚠️ Acción desconocida: " + header.getAction());
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("🔌 Cliente desconectado: " + socket.getRemoteSocketAddress());
        } finally {
            if (currentRoomId != null) {
                RoomManager.removeUserFromRoom(currentRoomId, this);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRegister(ControlHeader header) throws IOException {
        boolean success = usuarioDAO.registrarUsuario(header.getEmail(), header.getPassword(), header.getName());
        ControlHeader response = new ControlHeader("REGISTER_RESPONSE");
        if (success) {
            response.setMessage("Registro exitoso. ¡Bienvenido!");
        } else {
            response.setMessage("Error: El correo ya se encuentra registrado.");
        }
        ProtocolStreamer.writeHeader(out, response, null);
    }

    private void handleLogin(ControlHeader header) throws IOException {
        boolean success = usuarioDAO.autenticarUsuario(header.getEmail(), header.getPassword());
        ControlHeader response = new ControlHeader("LOGIN_RESPONSE");
        if (success) {
            this.loggedUserEmail = header.getEmail();
            response.setMessage("Login exitoso");
            response.setName(usuarioDAO.obtenerNombrePorEmail(header.getEmail()));
        } else {
            response.setMessage("Error: Credenciales incorrectas.");
        }
        ProtocolStreamer.writeHeader(out, response, null);
    }

    private void handleCreateRoom(ControlHeader header) throws IOException {
        String roomId = header.getRoomId();
        ControlHeader response = new ControlHeader("ROOM_CREATED_RESPONSE");

        boolean created = salaDAO.crearSala(roomId, loggedUserEmail);
        if (created) {
            this.currentRoomId = roomId;
            RoomManager.addUserToRoom(roomId, this);
            response.setMessage("Sala " + roomId + " creada exitosamente.");
            response.setRoomId(roomId);
            System.out.println("🏠 Sala creada con ID: " + roomId + " por el usuario: " + loggedUserEmail);
        } else {
            response.setMessage("Error: La sala ya existe o no se pudo crear.");
        }
        ProtocolStreamer.writeHeader(out, response, null);
    }

    private void handleJoinRoom(ControlHeader header) throws IOException {
        String roomId = header.getRoomId();
        ControlHeader response = new ControlHeader("ROOM_JOINED_RESPONSE");

        if (salaDAO.existeSala(roomId)) {
            this.currentRoomId = roomId;
            RoomManager.addUserToRoom(roomId, this);
            response.setMessage("Te uniste a la sala " + roomId);
            response.setRoomId(roomId);
            System.out.println("👤 Usuario " + loggedUserEmail + " se unió a la sala: " + roomId);
        } else {
            response.setMessage("Error: La sala especificada no existe.");
        }
        ProtocolStreamer.writeHeader(out, response, null);
    }

    private void handleChatMessage(ControlHeader header, NetworkFrame frame) {
        header.setEmail(loggedUserEmail);

        // Obtenemos y asignamos el nombre real del usuario desde la base de datos
        String nombreUsuario = usuarioDAO.obtenerNombrePorEmail(loggedUserEmail);
        header.setName(nombreUsuario != null ? nombreUsuario : loggedUserEmail);

        // ¡IMPORTANTE! Actualizamos la cabecera dentro del frame para que el broadcast lleve el nombre
        frame.setHeader(header);

        // Reenviamos la trama a todos en la sala
        RoomManager.broadcastToRoom(currentRoomId, frame, this);
        System.out.println("💬 Mensaje reenviado en sala " + currentRoomId + " de " + loggedUserEmail);
    }

    public void sendFrame(NetworkFrame frame) {
        try {
            ProtocolStreamer.writeFrame(out, frame);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar frame al cliente: " + e.getMessage());
        }
    }
}