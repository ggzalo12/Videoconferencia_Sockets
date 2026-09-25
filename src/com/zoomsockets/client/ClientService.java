package com.zoomsockets.client;

import com.zoomsockets.common.ControlHeader;
import com.zoomsockets.common.NetworkFrame;
import com.zoomsockets.common.ProtocolStreamer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClientService {
    private static ClientService instance = new ClientService();
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean running = false;
    private DatagramSocket udpReceiverSocket;

    private ClientService() {}

    public static ClientService getInstance() {
        return instance;
    }

    public boolean connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            running = true;

            // 1. Iniciar hilo TCP para escuchar respuestas de control, salas y chat
            new Thread(this::listenToServer).start();

            // 2. Iniciar hilo UDP en segundo plano para recibir de forma fluida los fotogramas de video
            startUdpReceiver();

            System.out.println("✅ Conectado exitosamente al servidor en " + ip + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    private void listenToServer() {
        try {
            NetworkFrame frame;
            while (running && (frame = ProtocolStreamer.readFrame(in)) != null) {
                ControlHeader header = ProtocolStreamer.readHeader(frame);
                if (header != null) {
                    handleServerResponse(header, frame);
                }
            }
        } catch (IOException e) {
            System.out.println("🔌 Conexión perdida con el servidor.");
        }
    }

    private void startUdpReceiver() {
        new Thread(() -> {
            try {
                udpReceiverSocket = new DatagramSocket();
                byte[] buffer = new byte[65535];
                System.out.println("🎥 Receptor UDP de video iniciado en el cliente...");

                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpReceiverSocket.receive(packet); // Espera y recibe los bytes del video del compañero

                    try (ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength())) {
                        BufferedImage img = ImageIO.read(bais);
                        if (img != null && ClientApp.getInstance() != null) {
                            ClientApp.getInstance().updateCallVideoImage(img); // Pinta el video en la interfaz
                        }
                    }
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("❌ Error en el receptor UDP: " + e.getMessage());
                }
            }
        }, "UDP-Client-Receiver-Thread").start();
    }

    private void handleServerResponse(ControlHeader response, NetworkFrame frame) {
        System.out.println("📥 Respuesta del servidor [" + response.getAction() + "]: " + response.getMessage());

        if ("LOGIN_RESPONSE".equals(response.getAction()) && response.getName() != null) {
            ClientSession.getInstance().login(response.getEmail(), response.getName());
            if (ClientApp.getInstance() != null) {
                ClientApp.getInstance().showRoomPanel();
            }
        }
        else if ("ROOM_CREATED_RESPONSE".equals(response.getAction()) || "ROOM_JOINED_RESPONSE".equals(response.getAction())) {
            JOptionPane.showMessageDialog(null, response.getMessage());
            if (response.getRoomId() != null) {
                ClientSession.getInstance().setCurrentRoomId(response.getRoomId());
                if (ClientApp.getInstance() != null) {
                    ClientApp.getInstance().showCallPanel();
                }
            }
        }
        else if ("CHAT_MESSAGE".equals(response.getAction())) {
            // Priorizamos el nombre; si no viene, usamos el correo o "Compañero"
            String sender = response.getName() != null ? response.getName() :
                    (response.getEmail() != null ? response.getEmail() : "Compañero");
            String msg = response.getMessage();
            if (ClientApp.getInstance() != null) {
                ClientApp.getInstance().appendChatMessage(sender, msg);
            }
        }
        // Nota: Se removió "VIDEO_FRAME" por TCP ya que el video ahora viaja de forma independiente por UDP.
    }

    public void sendAction(ControlHeader header, byte[] payload) {
        try {
            NetworkFrame frame = new NetworkFrame(header, payload);
            ProtocolStreamer.writeFrame(out, frame);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar acción al servidor: " + e.getMessage());
        }
    }

    public void sendFrame(NetworkFrame frame) {
        try {
            ProtocolStreamer.writeFrame(out, frame);
        } catch (IOException e) {
            System.err.println("❌ Error al enviar frame: " + e.getMessage());
        }
    }

    public void sendChatMessage(String roomId, String message) {
        ControlHeader header = new ControlHeader("CHAT_MESSAGE");
        header.setRoomId(roomId);
        header.setMessage(message);
        sendAction(header, null);
    }
}