/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.energyict.protocolimpl.iec1107;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.math.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import java.util.logging.*;
/**
 *
 * @author  Koen
 */
public interface ProtocolLink {
    
    public FlagIEC1107Connection getFlagIEC1107Connection();
    public TimeZone getTimeZone();
    public boolean isIEC1107Compatible();
    public int getNumberOfChannels() throws UnsupportedException, IOException;
    public String getPassword();
    public byte[] getDataReadout();
    public int getProfileInterval() throws UnsupportedException, IOException;
    
    /**
     * @deprecated use getProtocolChannelMap()
     */
    public ChannelMap getChannelMap();
    
    public ProtocolChannelMap getProtocolChannelMap();
    public Logger getLogger();
    public int getNrOfRetries();
    public boolean isRequestHeader();
    
}
