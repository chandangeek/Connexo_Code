/*
 * EICTTestProtocolConnection.java
 *
 * Created on 20 may 2010, 14:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.eicttest;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.protocol.meteridentification.*;
import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.util.logging.*;

/**
 *
 * @author fde
 */
public class EICTTestProtocolConnection extends Connection implements ProtocolConnection {
    
    InputStream inputStream;
    OutputStream outputStream;
    int iTimeout;
    int iMaxRetries;
    long lForceDelay;
    int iEchoCancelling;
    int iIEC1107Compatible;
    Encryptor encryptor;
    int iProtocolTimeout;
    boolean boolFlagIEC1107Connected;
    Logger logger;
    
    /** Creates a new instance of EICTTestProtocolConnection */
    public EICTTestProtocolConnection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,Logger logger) throws ConnectionException {
        super(inputStream, outputStream, lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor=encryptor;
        iProtocolTimeout=iTimeout;
        boolFlagIEC1107Connected=false;
        this.logger=logger;
    }
    
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        
    }
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        logger.info("call connection class disconnectMAC(...)");
    }
    public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException, ProtocolConnectionException {
        logger.info("call connection class connectMAC(...)");
        return null;
    }
    public byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;   
    }
    
}
