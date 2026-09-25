package com.zoomsockets.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:zoom_sockets.db";
    private static Connection connection = null;

    /**
     * Obtiene la instancia única de la conexión a SQLite (Patrón Singleton)
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Cargar el driver de SQLite
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("📦 Conexión a la base de datos SQLite establecida con éxito.");
                // Crear las tablas si no existen
                initDatabase(connection);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Inicializa el esquema de la base de datos creando las tablas esenciales
     */
    private static void initDatabase(Connection conn) {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT NOT NULL);";

        String sqlSalas = "CREATE TABLE IF NOT EXISTS salas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_id TEXT UNIQUE NOT NULL, " +
                "host_email TEXT NOT NULL);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlSalas);
            System.out.println("🛠️ Tablas de la base de datos verificadas/creadas correctamente.");
        } catch (SQLException e) {
            System.err.println("❌ Error al crear las tablas: " + e.getMessage());
        }
    }
}