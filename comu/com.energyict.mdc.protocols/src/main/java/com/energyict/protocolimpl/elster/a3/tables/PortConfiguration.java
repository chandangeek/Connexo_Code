/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PortConfiguration {


    private int controlParameters; // 2 bytes b0-3: Define how the remote port is used.
                           // b4: Read Only Port 0 = Read and write services are supported on this port.
                           // b5: Disable outgoing calls
                           // b6: Security Call Back 1 = When the meter answers a call it will only process the call back procedure. 0 = Normal communications
                           // b7-8: Initialization Frequency. When the port is configured for an outage modem it is recommended that the initialization frequency be set to hourly or daily. The outage modem keeps time in between initializations. 0 = only on power up and after a configuration table write. Not periodic. 1 = hourly 2 = midnight (every 24 hours) 3 = Not defined and not used by the meter
                           // b9-10 Shared Port Strategy 0 = Dedicated to remote port 1 = Dedicated until optical port activity 2 = Time multiplex remote and optical ports 3 = undefined When the shared port is enabled, the recommended shared port strategy is  Dedicated until optical port activity. 
                           // b11: Reserved for factory use.
                           // b12: Sportster Modem At Host 1 = always insert Sportster modem delays to work around possible problem of having US Robotics Sportster at host computer. 0 = Do not automatically insert delays. MP-28 can still be used at the start of a communication session to force the meter to insert delays for the current session.
                           // b13: Enable display of comm error codes. Note that optical port errors	are always displayed. 1 = communication errors for this port will be displayed for one display time									0 = communication errors are not displayed. 
                           // b14: Modem Supports Speed Buffering   The meter only uses this field if an internal modem is present. 1 = The modem supports speed buffering and the firmware will not change baud rate in conjunction with CONNECT messages. 0 = The firmware will change baud rates to communicate at the connected baud rate.
                           // b15: Large Response Timeouts   If TRUE, for C12.21 communication sessions the meter will use a response timeout of 15 seconds instead of the default timeout of 4 seconds. This option is primarily to facilitate CDPD modems.							    
    private long lowestBitRate; // 4 bytes UINT32. The lowest bit rate to attempt modem initialization.
    private int callInRandomization; // 1 byte Maximum initial modem call-in delay. Number of minutes the meter should randomize the 1st call-in after entering a call-in window. 
    private int inactivityTime; // 1 byte Prior to or after a session, if there is no activity within this amount of time the meter will hang up the modem. The time is in minute resolution. The meter enforces a minimum inactivity time of 1 minute. Valid range = 1-255 minutes. If Inactivity Time = 0, meter ignores and does not hang up the line. Recommended value = 5 minutes.
    private int minimumRetryDelay; // 1 byte The minimum time (in minutes) between retries. The actual retry time is this minimum + randomized(Retry_Delay). The meter enforces a minimum retry of a least 1 minute. 
    private int maximumRetryDelay; // 1 byte Maximum modem call in retry delay. Number of minutes over which the meter should randomize any call in retry attempts during the call in window. 
    private int phone1RetryAttempts; // 1 byte Number of times to retry calls for phone number 1. 0-254, 255 = try forever.
    private int phone2RetryAttempts; // 1 byte Number of times to retry calls for phone number 2. 0-254, 255 = try forever.
    private int Phone3RetryAttempts; // 1 byte Number of times to retry for phone number 3. 0-254, 255 = try forever.
    private int turnaroundDelay; // 1 byte For half duplex media there may be a delay required before the meter can successfully transmit a response. This specifies the number of 100 msec tics to wait before responding. Range = 0   25 seconds. Recommended default set at manufacturing = 0.
    private int escapeSequenceInterCharacterDelay; // 1 byte The number of system tics (8.33 msec) between escape sequence characters. Range = 0   2.1 seconds. Recommended default set at manufacturing = 0.


        
    
    /** Creates a new instance of SourceDefinitionEntry */
    public PortConfiguration(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setControlParameters(C12ParseUtils.getInt(data,offset,2, dataOrder)); offset+=2;
        setLowestBitRate(C12ParseUtils.getLong(data,offset,4, dataOrder)); offset+=4;
        setCallInRandomization(C12ParseUtils.getInt(data,offset++));
        setInactivityTime(C12ParseUtils.getInt(data,offset++));
        setMinimumRetryDelay(C12ParseUtils.getInt(data,offset++));
        setMaximumRetryDelay(C12ParseUtils.getInt(data,offset++));
        setPhone1RetryAttempts(C12ParseUtils.getInt(data,offset++));
        setPhone2RetryAttempts(C12ParseUtils.getInt(data,offset++));
        setPhone3RetryAttempts(C12ParseUtils.getInt(data,offset++));
        setTurnaroundDelay(C12ParseUtils.getInt(data,offset++));
        setEscapeSequenceInterCharacterDelay(C12ParseUtils.getInt(data,offset++));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PortConfiguration:\n");
        strBuff.append("   callInRandomization="+getCallInRandomization()+"\n");
        strBuff.append("   controlParameters="+getControlParameters()+"\n");
        strBuff.append("   escapeSequenceInterCharacterDelay="+getEscapeSequenceInterCharacterDelay()+"\n");
        strBuff.append("   inactivityTime="+getInactivityTime()+"\n");
        strBuff.append("   lowestBitRate="+getLowestBitRate()+"\n");
        strBuff.append("   maximumRetryDelay="+getMaximumRetryDelay()+"\n");
        strBuff.append("   minimumRetryDelay="+getMinimumRetryDelay()+"\n");
        strBuff.append("   phone1RetryAttempts="+getPhone1RetryAttempts()+"\n");
        strBuff.append("   phone2RetryAttempts="+getPhone2RetryAttempts()+"\n");
        strBuff.append("   phone3RetryAttempts="+getPhone3RetryAttempts()+"\n");
        strBuff.append("   turnaroundDelay="+getTurnaroundDelay()+"\n");
        return strBuff.toString();
    }
    
    static public int getSize(TableFactory tableFactory) throws IOException {
        return 15;
    }   

    public int getControlParameters() {
        return controlParameters;
    }

    public void setControlParameters(int controlParameters) {
        this.controlParameters = controlParameters;
    }

    public long getLowestBitRate() {
        return lowestBitRate;
    }

    public void setLowestBitRate(long lowestBitRate) {
        this.lowestBitRate = lowestBitRate;
    }

    public int getCallInRandomization() {
        return callInRandomization;
    }

    public void setCallInRandomization(int callInRandomization) {
        this.callInRandomization = callInRandomization;
    }

    public int getInactivityTime() {
        return inactivityTime;
    }

    public void setInactivityTime(int inactivityTime) {
        this.inactivityTime = inactivityTime;
    }

    public int getMinimumRetryDelay() {
        return minimumRetryDelay;
    }

    public void setMinimumRetryDelay(int minimumRetryDelay) {
        this.minimumRetryDelay = minimumRetryDelay;
    }

    public int getMaximumRetryDelay() {
        return maximumRetryDelay;
    }

    public void setMaximumRetryDelay(int maximumRetryDelay) {
        this.maximumRetryDelay = maximumRetryDelay;
    }

    public int getPhone1RetryAttempts() {
        return phone1RetryAttempts;
    }

    public void setPhone1RetryAttempts(int phone1RetryAttempts) {
        this.phone1RetryAttempts = phone1RetryAttempts;
    }

    public int getPhone2RetryAttempts() {
        return phone2RetryAttempts;
    }

    public void setPhone2RetryAttempts(int phone2RetryAttempts) {
        this.phone2RetryAttempts = phone2RetryAttempts;
    }

    public int getPhone3RetryAttempts() {
        return Phone3RetryAttempts;
    }

    public void setPhone3RetryAttempts(int Phone3RetryAttempts) {
        this.Phone3RetryAttempts = Phone3RetryAttempts;
    }

    public int getTurnaroundDelay() {
        return turnaroundDelay;
    }

    public void setTurnaroundDelay(int turnaroundDelay) {
        this.turnaroundDelay = turnaroundDelay;
    }

    public int getEscapeSequenceInterCharacterDelay() {
        return escapeSequenceInterCharacterDelay;
    }

    public void setEscapeSequenceInterCharacterDelay(int escapeSequenceInterCharacterDelay) {
        this.escapeSequenceInterCharacterDelay = escapeSequenceInterCharacterDelay;
    }

}
