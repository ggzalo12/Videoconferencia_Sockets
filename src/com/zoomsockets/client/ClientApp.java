package com.zoomsockets.client;

import javax.swing.*;
import java.awt.*;

public class ClientApp extends JFrame {
    private static ClientApp instance;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private RoomPanel roomPanel;
    private CallPanel callPanel;

    public ClientApp() {
        instance = this;
        setTitle("Zoom Sockets - UNI");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Instanciar los paneles de la aplicación
        LoginPanel loginPanel = new LoginPanel();
        roomPanel = new RoomPanel();
        callPanel = new CallPanel();

        // Agregar los paneles al contenedor principal con sus nombres de ruta (CardLayout)
        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(roomPanel, "ROOM");
        mainContainer.add(callPanel, "CALL");

        add(mainContainer);

        // Intentar conectar con el servidor automáticamente al abrir la aplicación
        boolean connected = ClientService.getInstance().connect("127.0.0.1", 8080);
        if (!connected) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al servidor. Asegúrate de ejecutar ServerApp primero.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public void appendChatMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            callPanel.appendMessage(sender, message);
        });
    }
    public static ClientApp getInstance() {
        return instance;
    }

    /**
     * Muestra el panel de gestión de salas.
     */
    public void showRoomPanel() {
        SwingUtilities.invokeLater(() -> {
            roomPanel.updateUserInfo();
            cardLayout.show(mainContainer, "ROOM");
        });
    }

    /**
     * Muestra el panel de la sala activa (videollamada / chat).
     */
    public void showCallPanel() {
        SwingUtilities.invokeLater(() -> {
            callPanel.updateRoomInfo();
            cardLayout.show(mainContainer, "CALL");
        });
    }

    public static void main(String[] args) {
        // Ejecutar la interfaz gráfica en el hilo seguro de Swing
        SwingUtilities.invokeLater(() -> {
            new ClientApp().setVisible(true);
        });
    }
    public void updateCallVideoImage(java.awt.image.BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            callPanel.updateVideoImage(img);
        });
    }
}