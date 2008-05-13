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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.mbus.core.ApplicationData;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870Connection;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870Frame;

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
    
    public IEC870Frame selectSecondaryAddress(long identification) throws IOException, MBusException {
    	return selectSecondaryAddress(identification,0,0,0xffff,0,0xff,0,0xff);    	
    }
    public IEC870Frame selectSecondaryAddress(long identification,long identificationMask) throws IOException, MBusException {
    	return selectSecondaryAddress(identification,identificationMask,0,0xffff,0,0xff,0,0xff);    	
    }
    public IEC870Frame selectSecondaryAddress(long identification, int manufacturer, int version, int medium) throws IOException, MBusException {
    	return selectSecondaryAddress(identification,0,manufacturer,0,version,0,medium,0);
    }
    public IEC870Frame selectSecondaryAddress(long identification,long identificationMask, int manufacturer, int manufacturerMask, int version, int versionMask, int medium, int mediumMask) throws IOException, MBusException {
        try {
            delay(forcedDelay);
            byte[] data = new byte[8];
            System.arraycopy(ParseUtils.applyMask(ParseUtils.createBCDByteArrayLE(identification,8),identificationMask), 0, data, 0, 4);
            System.arraycopy(ParseUtils.applyMask(ParseUtils.createBCDByteArrayLE(manufacturer,4),manufacturerMask), 0, data, 4, 2);
            System.arraycopy(ParseUtils.applyMask(ParseUtils.createBCDByteArrayLE(version,2),versionMask), 0, data, 6, 1);
            System.arraycopy(ParseUtils.applyMask(ParseUtils.createBCDByteArrayLE(medium,2),mediumMask), 0, data, 7, 1);
            return sendFrame(IEC870Frame.FRAME_VARIABLE_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_USER_DATA, getRTUAddress(), new ApplicationData(0x52,data), false);
        }
        catch(IEC870ConnectionException e) {
            throw new MBusException(com.energyict.cbo.Utils.stack2string(e));
        }
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
