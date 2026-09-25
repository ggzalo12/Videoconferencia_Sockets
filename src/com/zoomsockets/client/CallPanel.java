package com.zoomsockets.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class CallPanel extends JPanel {
    private JLabel lblRoomInfo;
    private JLabel lblVideoScreen;
    private JTextArea txtChatArea;
    private JTextField txtMessage;
    private JButton btnSend;
    private JButton btnLeave;
    private VideoStreamSender videoStreamSender;

    public CallPanel() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Información de la sala
        lblRoomInfo = new JLabel("Sala activa", JLabel.CENTER);
        lblRoomInfo.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblRoomInfo, BorderLayout.NORTH);

        // Panel Central dividido: Video y Chat
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Pantalla de video
        lblVideoScreen = new JLabel("Esperando video...", JLabel.CENTER);
        lblVideoScreen.setPreferredSize(new Dimension(320, 240));
        lblVideoScreen.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        centerPanel.add(lblVideoScreen);

        // Área de Chat
        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        centerPanel.add(new JScrollPane(txtChatArea));

        add(centerPanel, BorderLayout.CENTER);

        // Panel Inferior: Controles
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        txtMessage = new JTextField();
        btnSend = new JButton("Enviar");
        btnLeave = new JButton("Salir de Sala");

        JPanel controlButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        controlButtons.add(btnSend);
        controlButtons.add(btnLeave);

        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(controlButtons, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        initEvents();
    }

    public void updateRoomInfo() {
        String roomId = ClientSession.getInstance().getCurrentRoomId();
        lblRoomInfo.setText("Estás en la Sala: " + roomId);

        // Iniciar el hilo de la webcam al entrar a la sala
        if (videoStreamSender == null) {
            videoStreamSender = new VideoStreamSender();
            new Thread(videoStreamSender).start();
        }
    }

    public void updateVideoImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon icon = new ImageIcon(image.getScaledInstance(320, 240, Image.SCALE_FAST));
            lblVideoScreen.setIcon(icon);
            lblVideoScreen.setText("");
        });
    }

    public void appendMessage(String sender, String message) {
        txtChatArea.append(sender + ": " + message + "\n");
    }

    private void initEvents() {
        btnSend.addActionListener((ActionEvent e) -> {
            String msg = txtMessage.getText().trim();
            if (!msg.isEmpty()) {
                String roomId = ClientSession.getInstance().getCurrentRoomId();
                appendMessage("Tú", msg);
                ClientService.getInstance().sendChatMessage(roomId, msg);
                txtMessage.setText("");
            }
        });

        btnLeave.addActionListener((ActionEvent e) -> {
            // Detener la transmisión de video al salir de la sala
            if (videoStreamSender != null) {
                videoStreamSender.stopStream();
                videoStreamSender = null;
            }

            ClientSession.getInstance().setCurrentRoomId(null);
            if (ClientApp.getInstance() != null) {
                ClientApp.getInstance().showRoomPanel();
            }
        });
    }
}