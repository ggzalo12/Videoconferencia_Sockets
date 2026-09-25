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
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.QQVGA.getSize());
        }
        try {
            // Inicializamos el socket UDP y la dirección del servidor (localhost o IP de red)
            udpSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar el socket UDP de video: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (webcam == null) return;
        webcam.open();

        while (running) {
            try {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        // Comprimir la imagen a formato JPG
                        ImageIO.write(image, "jpg", baos);
                        byte[] imageBytes = baos.toByteArray();

                        // Enviar el fotograma directamente por UDP de forma rápida y sin bloqueo
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
                Thread.sleep(100); // ~10 cuadros por segundo para mantener fluidez
            } catch (Exception e) {
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