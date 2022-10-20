package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.protocol.inbound.mbus.Merlin.BUFFER_SIZE;

/**
 * Helper logging wrapper class
 */
public class MerlinLogger {

    public static final String LOG_PREFIX = "[Merlin] ";
    private final Logger logger;

    public MerlinLogger(Logger logger) {
        this.logger = logger;
        this.logger.setLevel(Level.ALL);
    }

    public Logger getLogger() {
        return logger;
    }

    public void info(String message) {
        getLogger().info(LOG_PREFIX + message);
    }

    public void info(String message, byte[] payload) {
        info(message + " " + payload.length + ": " + ProtocolTools.bytesToHex(payload));
    }

    public void info(String message, byte[] buffer, int readBytes) {
        try {
            if (readBytes < BUFFER_SIZE) {
                byte[] payload = new byte[readBytes];
                System.arraycopy(buffer, 0, payload, 0, readBytes);
                info(message + " " + readBytes + ": " + ProtocolTools.bytesToHex(payload));
            } else {
                info(message + " " + readBytes + ": " + ProtocolTools.bytesToHex(buffer));
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, LOG_PREFIX + "Error logging buffer " + ex.getMessage(), ex);
        }
    }

    public void warn(String message) {
        getLogger().warning(LOG_PREFIX + message);
    }

    public void error(String s) {
        getLogger().severe(LOG_PREFIX + s);
    }

    public void error(String message, Throwable e) {
        getLogger().log(Level.SEVERE, LOG_PREFIX + message + ": " + e.getMessage(), e);
    }

    public void debug(String message) {
        if (getLogger().isLoggable(Level.FINEST)) {
            getLogger().finest(LOG_PREFIX + message);
            //System.out.println(message);
        }
    }

}
