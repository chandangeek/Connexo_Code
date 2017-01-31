/*
 * SDKSampleProtocolConnection.java
 *
 * Created on 13 juni 2007, 11:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package test.com.energyict.protocolimpl.sdksample;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author kvds
 */
public class SDKSampleProtocolConnection extends Connection implements ProtocolConnection {

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

    /**
     * Creates a new instance of SDKSampleProtocolConnection
     */
    public SDKSampleProtocolConnection(InputStream inputStream,
                                       OutputStream outputStream,
                                       int iTimeout,
                                       int iMaxRetries,
                                       long lForceDelay,
                                       int iEchoCancelling,
                                       int iIEC1107Compatible,
                                       Encryptor encryptor, Logger logger) throws ConnectionException {
        super(inputStream, outputStream, lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor = encryptor;
        iProtocolTimeout = iTimeout;
        boolFlagIEC1107Connected = false;
        this.logger = logger;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {

    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        logger.info("call connection class disconnectMAC(...)");
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        logger.info("call connection class connectMAC(...)");
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void write(byte[] bytes) {
        if (outputStream != null) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void read() {
        if (inputStream != null) {
            try {
                while (inputStream.available() > 0) {
                    while (inputStream.available() > 0) {
                        inputStream.read();
                    }
                    ProtocolTools.delay(5);
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

}
