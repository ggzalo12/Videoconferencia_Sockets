package com.zoomsockets.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalaDAO {

    /**
     * Registra una nueva sala en la base de datos SQLite.
     */
    public boolean crearSala(String roomId, String hostEmail) {
        String sql = "INSERT INTO salas (room_id, host_email) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            pstmt.setString(2, hostEmail);
            pstmt.executeUpdate();
            System.out.println("🏠 Sala registrada en BD: " + roomId + " (Host: " + hostEmail + ")");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error al crear sala (posible ID duplicado): " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si una sala existe en la base de datos.
     */
    public boolean existeSala(String roomId) {
        String sql = "SELECT id FROM salas WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar la sala: " + e.getMessage());
        }
        return false;
    }
}