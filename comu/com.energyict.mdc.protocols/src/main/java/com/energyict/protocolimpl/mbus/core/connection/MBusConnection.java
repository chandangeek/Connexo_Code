/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.mbus.core.ApplicationData;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870Connection;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870Frame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;

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

    public int getReasonTIMEOUT_ERROR() {
    	return TIMEOUT_ERROR;
    }

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

    public void flushInputStream() throws ConnectionException {
    	super.flushInputStream();
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

    public IEC870Frame selectSecondaryAddress(long identification, int manufacturer, int version, int medium,boolean discoverResponse) throws IOException {
        delay(forcedDelay);
        byte[] data = new byte[8];
        System.arraycopy(ParseUtils.createBCDByteArrayLEWithMask(identification,8), 0, data, 0, 4);
        System.arraycopy(ParseUtils.createBCDByteArrayLEWithMask(manufacturer,4), 0, data, 4, 2);
        System.arraycopy(ParseUtils.createBCDByteArrayLEWithMask(version,2), 0, data, 6, 1);
        System.arraycopy(ParseUtils.createBCDByteArrayLEWithMask(medium,2), 0, data, 7, 1);
        IEC870Frame o = sendFrame(IEC870Frame.FRAME_VARIABLE_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_USER_DATA, getRTUAddress(), -1,new ApplicationData(0x52,data), false,discoverResponse);
		try {
			Thread.sleep(300);
		}
		catch(InterruptedException e) {
			// absorb
		}
		return o;
    }

    public IEC870Frame sendApplicationReset(int resetSubcode) throws IOException {
        delay(forcedDelay);
        return sendFrame(IEC870Frame.FRAME_VARIABLE_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_USER_DATA, getRTUAddress(), new ApplicationData(0x50,new byte[]{(byte)resetSubcode}), false);
    }

    public void sendSND_NKE() throws IOException {
    	sendSND_NKE(getRTUAddress());
    }
    public void sendSND_NKE(int address) throws IOException {
        delay(forcedDelay);
        sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_SEND_CONFIRM_RESET_REMOTE_LINK, address, 2, null, false);
    }

    public IEC870Frame sendREQ_UD2() throws IOException {
    	return sendREQ_UD2(false);
    }
    public IEC870Frame sendREQ_UD2(boolean discoverResponse) throws IOException {
        delay(forcedDelay);
        return sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS2, getRTUAddress(), 2, null, true,discoverResponse);
    }

    public IEC870Frame sendREQ_UD1() throws IOException {
        delay(forcedDelay);
        return sendFrame(IEC870Frame.FRAME_FIXED_LENGTH, IEC870Frame.CONTROL_REQUEST_RESPOND_CLASS1, getRTUAddress(), 2, null, true);
    }

}
