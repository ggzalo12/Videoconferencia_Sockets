package com.zoomsockets.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PORT_TCP = 8080;
    private static final int PORT_UDP = 5001;

    // Colección concurrente para almacenar las direcciones UDP de los clientes activos
    private static final Set<InetSocketAddress> activeUdpClients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Servidor de Videoconferencia (Híbrido TCP + UDP)...");

        // Inicializar la base de datos y verificar tablas
        DatabaseConnection.getConnection();

        // 1. Iniciar el Servidor UDP en un hilo en segundo plano para gestionar el video
        startUdpServer();

        // 2. Crear un pool de hilos para gestionar la concurrencia de los clientes TCP (Control y Chat)
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT_TCP)) {
            System.out.println("✅ Servidor TCP escuchando y listo en el puerto " + PORT_TCP);

            while (true) {
                // Esperar y aceptar una nueva conexión de cliente TCP
                Socket clientSocket = serverSocket.accept();
                System.out.println("👤 Nuevo cliente TCP conectado desde: " + clientSocket.getRemoteSocketAddress());

                // Delegar la conexión a un hilo independiente (ClientHandler)
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("❌ Error crítico en el ServerSocket TCP: " + e.getMessage());
        }
    }

    private static void startUdpServer() {
        new Thread(() -> {
            try (DatagramSocket udpSocket = new DatagramSocket(PORT_UDP)) {
                System.out.println("🎥 Servidor UDP de video escuchando y listo en el puerto " + PORT_UDP);
                byte[] buffer = new byte[65535]; // Buffer para los fotogramas comprimidos

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Recibe el fotograma de video de un cliente

                    InetSocketAddress senderAddress = (InetSocketAddress) packet.getSocketAddress();

                    // Registrar automáticamente la dirección UDP del cliente si es nuevo
                    activeUdpClients.add(senderAddress);

                    // Reenviar el fotograma UDP a todos los *demás* clientes conectados por UDP
                    for (InetSocketAddress clientAddress : activeUdpClients) {
                        if (!clientAddress.equals(senderAddress)) {
                            DatagramPacket forwardPacket = new DatagramPacket(
                                    packet.getData(),
                                    packet.getLength(),
                                    clientAddress.getAddress(),
                                    clientAddress.getPort()
                            );
                            udpSocket.send(forwardPacket);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error en el servidor UDP: " + e.getMessage());
            }
        }, "UDP-Video-Server-Thread").start();
    }
}