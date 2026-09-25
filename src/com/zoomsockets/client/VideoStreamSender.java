package com.zoomsockets.client;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.ImageIO;

public class VideoStreamSender implements Runnable {
    private volatile boolean running = true;
    private Webcam webcam;
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private static final int SERVER_UDP_PORT = 5001;

    public VideoStreamSender() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.err.println("❌ No se encontró ninguna cámara web disponible.");
        } else {
            webcam.setViewSize(WebcamResolution.QQVGA.getSize());
        }
        try {
            udpSocket = new DatagramSocket();
            // Usa "localhost" si pruebas en la misma PC del servidor, o la IP "192.168.130.60" si estás en la otra laptop
            serverAddress = InetAddress.getByName("192.168.130.60");
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar el socket UDP de video: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (webcam == null) {
            System.err.println("❌ El hilo de video se detuvo porque la webcam es null.");
            return;
        }

        try {
            webcam.open();
            System.out.println("📹 Cámara iniciada correctamente en el cliente.");
        } catch (Exception e) {
            System.err.println("❌ No se pudo abrir la cámara (quizás otra app la está usando): " + e.getMessage());
            return;
        }

        while (running) {
            try {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(image, "jpg", baos);
                        byte[] imageBytes = baos.toByteArray();

                        if (udpSocket != null && serverAddress != null) {
                            DatagramPacket packet = new DatagramPacket(
                                    imageBytes,
                                    imageBytes.length,
                                    serverAddress,
                                    SERVER_UDP_PORT
                            );
                            udpSocket.send(packet);
                        }
                    }
                }
                Thread.sleep(100); // ~10 cuadros por segundo
            } catch (Exception e) {
                System.err.println("❌ Error al enviar fotograma por UDP: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        webcam.close();
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }

    public void stopStream() {
        running = false;
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}