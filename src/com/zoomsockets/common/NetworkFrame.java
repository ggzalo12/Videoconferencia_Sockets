package com.zoomsockets.common;

import com.google.gson.Gson;
import java.io.Serializable;

public class NetworkFrame implements Serializable {
    private static final long serialVersionUID = 1L;

    private ControlHeader header;
    private byte[] payload;

    public NetworkFrame(ControlHeader header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }

    // Constructor para aceptar el JSON que envía ProtocolStreamer
    public NetworkFrame(String jsonHeader, byte[] payload) {
        this.payload = payload;
        if (jsonHeader != null && !jsonHeader.isEmpty()) {
            Gson gson = new Gson();
            this.header = gson.fromJson(jsonHeader, ControlHeader.class);
        }
    }

    public ControlHeader getHeader() {
        return header;
    }

    public void setHeader(ControlHeader header) {
        this.header = header;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getJsonHeader() {
        Gson gson = new Gson();
        return header != null ? gson.toJson(header) : "{}";
    }

    public byte[] getBinaryPayload() {
        return payload;
    }
}