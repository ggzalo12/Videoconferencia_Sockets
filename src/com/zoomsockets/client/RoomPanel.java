package com.zoomsockets.client;

import com.zoomsockets.common.ControlHeader;
import javax.swing.*;
import java.awt.*;

public class RoomPanel extends JPanel {
    private JTextField txtRoomId;
    private JButton btnCreateRoom;
    private JButton btnJoinRoom;
    private JLabel lblWelcome;

    public RoomPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblWelcome = new JLabel("Panel Principal", JLabel.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblWelcome, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("ID de Sala:"), gbc);
        txtRoomId = new JTextField(15);
        gbc.gridx = 1;
        add(txtRoomId, gbc);

        btnCreateRoom = new JButton("Crear Sala");
        gbc.gridy = 2; gbc.gridx = 0;
        add(btnCreateRoom, gbc);

        btnJoinRoom = new JButton("Unirse a Sala");
        gbc.gridx = 1;
        add(btnJoinRoom, gbc);

        initEvents();
    }

    public void updateUserInfo() {
        String name = ClientSession.getInstance().getName();
        if (name != null) {
            lblWelcome.setText("Bienvenido, " + name);
        }
    }

    private void initEvents() {
        btnCreateRoom.addActionListener(e -> {
            String roomId = txtRoomId.getText().trim();
            if (roomId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa un ID para la sala.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ControlHeader header = new ControlHeader("CREATE_ROOM");
            header.setRoomId(roomId);
            ClientService.getInstance().sendAction(header, null);
            JOptionPane.showMessageDialog(this, "Solicitud para crear la sala " + roomId + " enviada.");
        });

        btnJoinRoom.addActionListener(e -> {
            String roomId = txtRoomId.getText().trim();
            if (roomId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa el ID de la sala a unirte.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ControlHeader header = new ControlHeader("JOIN_ROOM");
            header.setRoomId(roomId);
            ClientService.getInstance().sendAction(header, null);
            JOptionPane.showMessageDialog(this, "Solicitud para unirse a la sala " + roomId + " enviada.");
        });
    }
}