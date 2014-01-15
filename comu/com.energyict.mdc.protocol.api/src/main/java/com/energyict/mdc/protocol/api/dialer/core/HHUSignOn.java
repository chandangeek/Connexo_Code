/*
 * HHUSignOn.java
 *
 * Created on 18 september 2003, 11:21
 */

package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.inbound.MeterType;

import java.io.IOException;


/**
 * @author Koen
 */
public interface HHUSignOn {

    public static final int PROTOCOL_NORMAL = 0;
    public static final int PROTOCOL_HDLC = 2;
    public static final int MODE_READOUT = 0;
    public static final int MODE_PROGRAMMING = 1;
    public static final int MODE_BINARY_HDLC = 2;
    public static final int MODE_MANUFACTURER_SPECIFIC_3 = 3;
    public static final int MODE_MANUFACTURER_SPECIFIC_4 = 4;
    public static final int MODE_MANUFACTURER_SPECIFIC_5 = 5;
    public static final int MODE_MANUFACTURER_SPECIFIC_SEVCD = 6;

    public void sendBreak() throws NestedIOException, ConnectionException;

    public MeterType signOn(String strIdent, String meterID) throws IOException, ConnectionException;

    public MeterType signOn(String strIdent, String meterID, int baudrate) throws IOException, ConnectionException;

    public MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate) throws IOException, ConnectionException;

    public void setProtocol(int protocol);

    public void setMode(int mode);

    public void enableDataReadout(boolean enabled);

    public byte[] getDataReadout();

    public String getReceivedIdent();
}
