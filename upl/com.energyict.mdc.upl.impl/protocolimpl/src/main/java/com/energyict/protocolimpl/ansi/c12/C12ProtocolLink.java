/*
 * C12ProtocolLink.java
 *
 * Created on 16 oktober 2005, 17:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;
import com.energyict.protocolimpl.meteridentification.*;

/**
 *
 * @author Koen
 */
public interface C12ProtocolLink {
    
    public C12Layer2 getC12Layer2();
    public TimeZone getTimeZone();
    public int getNumberOfChannels() throws UnsupportedException, IOException;
    public ProtocolChannelMap getProtocolChannelMap();
    public int getProfileInterval() throws UnsupportedException, IOException;
    public Logger getLogger();
    public PSEMServiceFactory getPSEMServiceFactory();   
    public StandardTableFactory getStandardTableFactory();
    public int getInfoTypeRoundtripCorrection();
    public AbstractManufacturer getManufacturer();  
    
    /*
     *   In case of GE KV and KV2 meters, returns getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
     */
    public int getMeterConfig() throws IOException; // meter specific
}
