/*
 * TrimeranConnection.java
 *
 * Created on 19 juni 2006, 16:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.edf.trimaran.core.*;
import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.base.*;

/**
 *
 * @author Koen
 */
public class TrimeranConnectionLayering extends ConnectionV25  implements ProtocolConnection {
    
    private static final int DEBUG=0;
    //private static final long SESSIONTIMEOUT_TSE=22000;
    
    int timeout;
    int maxRetries;
    long forcedDelay;
    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
    String nodeId;
    int securityLevel;
    private int nSEQ=0;
    private int nSEQRx=0;
    ResponseFrame lastResponseFrame;
    int halfDuplex;
    private int type;
    LayerManager layerManager=null;
    
    /** Creates a new instance of TrimeranConnection */
    public TrimeranConnectionLayering(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,int securityLevel,int halfDuplex) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        this.halfDuplex=halfDuplex;
        layerManager = new LayerManager(this);
        
    } // EZ7Connection(...)
    
    public com.energyict.protocol.meteridentification.MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException, ProtocolConnectionException {
        this.nodeId=nodeId;
        return null;
    }
    
    public byte[] dataReadout(String strID, String nodeId) throws com.energyict.cbo.NestedIOException, ProtocolConnectionException {
        return null;
    }
    
    public void disconnectMAC() throws com.energyict.cbo.NestedIOException, ProtocolConnectionException {
    }
    
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }
    
    
    private void sendFrame(byte[] data) throws ConnectionException {
        //if (getS
        sendOut(data);
    }
    
    public byte[] sendCommand(byte[] cmdData) throws IOException {
        return sendCommand(cmdData,0);
    }
    
    public byte[] sendCommand(byte[] cmdData, int len) throws IOException {

        return null;
    }
    

    

    
    
} // public class TrimeranConnection extends Connection  implements ProtocolConnection
