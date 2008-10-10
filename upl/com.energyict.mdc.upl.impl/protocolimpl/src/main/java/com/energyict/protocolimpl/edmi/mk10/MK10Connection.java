
package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.protocol.meteridentification.*;
import com.energyict.protocolimpl.base.*;
import java.io.*;
import java.util.logging.*;

/**
 *
 * @author jme
 */
public class MK10Connection extends Connection implements ProtocolConnection {
    
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
    
    /** Creates a new instance of SDKSampleProtocolConnection */
    public MK10Connection(InputStream inputStream,
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
