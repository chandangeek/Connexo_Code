package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.protocol.inbound.mbus.Merlin.BUFFER_SIZE;

public class MerlinLogger {

    private final Logger logger;

    public MerlinLogger(Logger logger) {
        this.logger = logger;
        this.logger.setLevel(Level.ALL);
    }

    public Logger getLogger() {
        return logger;
    }

    public void log(String message, byte[] payload) {
        log(message + " " + payload.length + ": " + ProtocolTools.bytesToHex(payload));
    }

    public void log(String message, byte[] buffer, int readBytes) {
        try {
            if (readBytes < BUFFER_SIZE) {
                byte[] payload = new byte[readBytes];
                System.arraycopy(buffer, 0, payload, 0, readBytes);
                log(message + " " + readBytes + ": " + ProtocolTools.bytesToHex(payload));
            } else {
                log(message + " " + readBytes + ": " + ProtocolTools.bytesToHex(buffer));
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "[Merlin] Error logging buffer " + ex.getMessage(), ex);
        }
    }


    public void log(String message) {
        getLogger().info("[Merlin] " + message);
    }

    public void logW(String message) {
        getLogger().warning("[Merlin] " + message);
    }

    public void logE(String message, Throwable e) {
        getLogger().log(Level.SEVERE, message + ": " + e.getMessage(), e);
    }
}
