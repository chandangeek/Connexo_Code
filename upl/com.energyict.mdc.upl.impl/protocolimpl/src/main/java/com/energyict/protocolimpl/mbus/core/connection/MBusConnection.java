/*
 * MBusConnection.java
 *
 * Created on 2 oktober 2007, 10:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core.connection;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.meteridentification.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.mbus.core.*;
import com.energyict.protocolimpl.mbus.core.connection.iec870.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author kvds
 */
public class MBusConnection extends IEC870Connection implements ProtocolConnection { 
    
    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;
    
    int maxRetries;
    boolean loggedOn=false;
    
    int address;
    long forcedDelay;
    
    /** Creates a new instance of MBusConnection */
    public MBusConnection(InputStream inputStream, 
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            TimeZone timeZone,
            HalfDuplexController halfDuplexController) throws ConnectionException {
        super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, timeZone);
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
    } // ModbusConnection(...)
    
    
    /*******************************************************************************************
     * Implementation of the abstract Connection class
     ******************************************************************************************/
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        
    }
    public HHUSignOn getHhuSignOn() {
        return null;
    }
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        
    }
    public MeterType connectMAC(String strID,String strPassword,int securityLevel,String nodeId) throws IOException, ProtocolConnectionException {
        return null;
    }
    public byte[] dataReadout(String strID,String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
    }
    
    public void setRetries(int retries) {
        super.setRetries(retries);
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public IEC870Frame sendApplicationReset(int resetSubcode) throws IOException, MBusException {
        try {
            delay(forcedDelay);
            return sendFrame(IEC870Frame.FRAME_VARIABLE_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_USER_DATA, getRTUAddress(), new ApplicationData(0x50,new byte[]{(byte)resetSubcode}), false);
        }
        catch(IEC870ConnectionException e) {
            throw new MBusException(com.energyict.cbo.Utils.stack2string(e));
        }
    }    
    
    public void sendSND_NKE() throws IOException, MBusException {
        try {
            delay(forcedDelay);
            sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_RESET_REMOTE_LINK, getRTUAddress(), 2, null, false);
        }
        catch(IEC870ConnectionException e) {
            throw new MBusException(com.energyict.cbo.Utils.stack2string(e));
        }
    }    
    
    public IEC870Frame sendREQ_UD2() throws IOException, MBusException {
        try {
            delay(forcedDelay);
            return sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2, getRTUAddress(), 2, null, true);
        }
        catch(IEC870ConnectionException e) {
            throw new MBusException(com.energyict.cbo.Utils.stack2string(e));
        }
    }    
    
    public IEC870Frame sendREQ_UD1() throws IOException, MBusException {
        try {
            delay(forcedDelay);
            return sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS1, getRTUAddress(), 2, null, true);
        }
        catch(IEC870ConnectionException e) {
            throw new MBusException(com.energyict.cbo.Utils.stack2string(e));
        }
    }    
    
}
