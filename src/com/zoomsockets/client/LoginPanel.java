package com.zoomsockets.client;

import com.zoomsockets.common.ControlHeader;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtName; // Opcional para el registro
    private JButton btnLogin;
    private JButton btnRegister;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitle = new JLabel("Zoom Sockets - UNI", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // Campo Nombre (para registro)
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Nombre:"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1;
        add(txtName, gbc);

        // Campo Correo
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Correo:"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        add(txtEmail, gbc);

        // Campo Contraseña
        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        // Botón Login
        btnLogin = new JButton("Iniciar Sesión");
        gbc.gridy = 4; gbc.gridx = 0;
        add(btnLogin, gbc);

        // Botón Registrar
        btnRegister = new JButton("Registrarse");
        gbc.gridx = 1;
        add(btnRegister, gbc);

        // Configurar eventos de los botones
        initEvents();
    }

    private void initEvents() {
        // Evento para Iniciar Sesión
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor completa todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ControlHeader header = new ControlHeader("LOGIN");
            header.setEmail(email);
            header.setPassword(password);

            ClientService.getInstance().sendAction(header, null);
            JOptionPane.showMessageDialog(this, "Petición de inicio de sesión enviada al servidor.");
        });

        // Evento para Registrarse
        btnRegister.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Completa Nombre, Correo y Contraseña para registrarte.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ControlHeader header = new ControlHeader("REGISTER");
            header.setName(name);
            header.setEmail(email);
            header.setPassword(password);

            ClientService.getInstance().sendAction(header, null);
            JOptionPane.showMessageDialog(this, "Petición de registro enviada al servidor.");
        });
    }
}