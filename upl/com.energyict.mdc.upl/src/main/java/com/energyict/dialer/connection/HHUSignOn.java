/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HHUSignOn.java
 *
 * Created on 18 september 2003, 11:21
 */

package com.energyict.dialer.connection;

import com.energyict.protocol.meteridentification.MeterType;

import java.io.IOException;

/**
 * @author Koen
 */
public interface HHUSignOn {

    int PROTOCOL_NORMAL = 0;
    int PROTOCOL_HDLC = 2;
    int MODE_READOUT = 0;
    int MODE_PROGRAMMING = 1;
    int MODE_BINARY_HDLC = 2;
    int MODE_MANUFACTURER_SPECIFIC_3 = 3;
    int MODE_MANUFACTURER_SPECIFIC_4 = 4;
    int MODE_MANUFACTURER_SPECIFIC_5 = 5;
    int MODE_MANUFACTURER_SPECIFIC_SEVCD = 6;

    void sendBreak() throws IOException, ConnectionException;

    MeterType signOn(String strIdent, String meterID) throws IOException;

    MeterType signOn(String strIdent, String meterID, int baudrate) throws IOException;

    MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate) throws IOException;

    void setProtocol(int protocol);

    void setMode(int mode);

    void enableDataReadout(boolean enabled);

    byte[] getDataReadout();

    String getReceivedIdent();

}