package com.zoomsockets.common;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProtocolStreamer {
    private static final Gson gson = new Gson();

    // Límites de seguridad para evitar desbordamientos en memoria
    private static final int MAX_JSON_LENGTH = 10 * 1024;          // Máximo 10 KB para la cabecera
    private static final int MAX_PAYLOAD_LENGTH = 20 * 1024 * 1024; // Máximo 20 MB para archivos/binarios

    /**
     * Escribe una trama completa (NetworkFrame) a través del flujo de salida del Socket.
     */
    public static void writeFrame(DataOutputStream out, NetworkFrame frame) throws IOException {
        // 1. Convertir la cabecera JSON a bytes UTF-8
        byte[] jsonBytes = frame.getJsonHeader().getBytes(StandardCharsets.UTF_8);
        if (jsonBytes.length > MAX_JSON_LENGTH) {
            throw new IOException("La cabecera JSON excede el límite permitido.");
        }

        // 2. Obtener el payload binario (si es null, usar array vacío)
        byte[] payload = frame.getBinaryPayload() != null ? frame.getBinaryPayload() : new byte[0];
        if (payload.length > MAX_PAYLOAD_LENGTH) {
            throw new IOException("El payload binario excede el límite permitido.");
        }

        // 3. Escribir tamaño del JSON + bytes del JSON
        out.writeInt(jsonBytes.length);
        out.write(jsonBytes);

        // 4. Escribir tamaño del binario + bytes del binario
        out.writeInt(payload.length);
        out.write(payload);

        out.flush(); // Forzar el envío inmediato por la red
    }

    /**
     * Lee una trama desde el flujo de entrada del Socket respetando las longitudes exactas.
     */
    public static NetworkFrame readFrame(DataInputStream in) throws IOException {
        int jsonLength;
        try {
            jsonLength = in.readInt();
        } catch (Exception e) {
            return null; // Si la conexión se cerró, retorna null de forma segura
        }

        if (jsonLength <= 0 || jsonLength > MAX_JSON_LENGTH) {
            throw new IOException("Tamaño de JSON inválido o corrupto: " + jsonLength);
        }

        // Leer los bytes exactos de la cabecera JSON
        byte[] jsonBytes = new byte[jsonLength];
        in.readFully(jsonBytes);
        String jsonHeader = new String(jsonBytes, StandardCharsets.UTF_8);

        // Leer el tamaño del payload binario
        int payloadLength = in.readInt();
        if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_LENGTH) {
            throw new IOException("Tamaño de payload binario inválido o corrupto: " + payloadLength);
        }

        // Leer los bytes exactos del payload binario
        byte[] payload = new byte[payloadLength];
        if (payloadLength > 0) {
            in.readFully(payload);
        }

        return new NetworkFrame(jsonHeader, payload);
    }

    /**
     * Método auxiliar para empaquetar directamente un objeto ControlHeader y enviarlo.
     */
    public static void writeHeader(DataOutputStream out, ControlHeader header, byte[] payload) throws IOException {
        String json = gson.toJson(header);
        writeFrame(out, new NetworkFrame(json, payload));
    }

    /**
     * Método auxiliar para extraer el objeto ControlHeader desde una trama recibida.
     */
    public static ControlHeader readHeader(NetworkFrame frame) {
        if (frame == null || frame.getJsonHeader() == null) return null;
        return gson.fromJson(frame.getJsonHeader(), ControlHeader.class);
    }
}