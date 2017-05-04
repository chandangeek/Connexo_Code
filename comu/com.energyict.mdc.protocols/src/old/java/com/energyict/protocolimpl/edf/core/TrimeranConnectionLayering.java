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

import com.energyict.dialer.connection.ConnectionV25;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 */
public class TrimeranConnectionLayering extends ConnectionV25  implements ProtocolConnection {

    int timeout;
    int maxRetries;
    long forcedDelay;
    String nodeId;
    int securityLevel;
    int halfDuplex;
    LayerManager layerManager=null;

    /** Creates a new instance of TrimeranConnection */
    public TrimeranConnectionLayering(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,int securityLevel,int halfDuplex) {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;
        this.halfDuplex=halfDuplex;
        layerManager = new LayerManager(this);

    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException {
        this.nodeId=nodeId;
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
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

}
