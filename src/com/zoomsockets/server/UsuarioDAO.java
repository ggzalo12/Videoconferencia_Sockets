package com.zoomsockets.server;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Registra un nuevo usuario en la base de datos con contraseña cifrada (BCrypt).
     */
    public boolean registrarUsuario(String email, String password, String name) {
        String sql = "INSERT INTO usuarios (email, password, name) VALUES (?, ?, ?)";

        // Generar el hash seguro de la contraseña
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, name);

            pstmt.executeUpdate();
            System.out.println("✅ Usuario registrado exitosamente: " + email);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error al registrar usuario (posible correo duplicado): " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida las credenciales de un usuario comparando la contraseña con el hash de SQLite.
     */
    public boolean autenticarUsuario(String email, String password) {
        String sql = "SELECT password FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Verificar si la contraseña coincide con el hash almacenado
                return BCrypt.checkpw(password, storedHash);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al autenticar usuario: " + e.getMessage());
        }
        return false; // Retorna falso si no existe el correo o la contraseña falla
    }

    /**
     * Obtiene el nombre del usuario a partir de su correo.
     */
    public String obtenerNombrePorEmail(String email) {
        String sql = "SELECT name FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener el nombre: " + e.getMessage());
        }
        return "Usuario";
    }
}