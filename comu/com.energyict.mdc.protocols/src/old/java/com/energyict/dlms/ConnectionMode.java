/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

public enum ConnectionMode {

    HDLC(0, "HDLC"),
    TCPIP(1, "TCP/IP"),
    COSEM_APDU(2, "Cosem APDU"),
    LLC(3, "LLC"),
    IF2(4, "IF2"),
    HDLC_CONSERETH(5, "Consereth HDLC"),
    INVALID(-1, "Invalid");

    private final int mode;
    private final String description;

    private ConnectionMode(int mode, String description) {
        this.mode = mode;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getMode() {
        return mode;
    }

    public String getModeAsString() {
        return "" + getMode();
    }

    public boolean isInvalid() {
        return equals(INVALID);
    }

    public static ConnectionMode fromValue(int mode) {
        for (ConnectionMode connectionMode : values()) {
            if (connectionMode.getMode() == mode) {
                return connectionMode;
            }
        }
        return INVALID;
    }

}
