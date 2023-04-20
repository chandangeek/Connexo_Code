/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.protocol.inbound.mbus.Merlin.BUFFER_SIZE;

/**
 * Helper logging wrapper class
 */
public class MerlinLogger {

    public String logPrefix = "[Merlin] ";
    private Logger logger;

    public MerlinLogger(Logger logger) {
        this.logger = logger;
        this.logger.setLevel(Level.ALL);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    public void info(String message) {
        getLogger().info(logPrefix + message);
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
            getLogger().log(Level.SEVERE, logPrefix + "Error logging buffer " + ex.getMessage(), ex);
        }
    }

    public void warn(String message) {
        getLogger().warning(logPrefix + message);
    }

    public void error(String s) {
        getLogger().severe(logPrefix + s);
    }

    public void error(String message, Throwable e) {
        getLogger().log(Level.SEVERE, logPrefix + message + ": " + e.getMessage(), e);
    }

    public void debug(String message) {
        if (getLogger().isLoggable(Level.FINEST)) {
            getLogger().finest(logPrefix + message);
        }
    }

    public void setSerialNumber(String serialNumber) {
        this.logPrefix = "[MERLIN-" +serialNumber + "] ";
    }
}
