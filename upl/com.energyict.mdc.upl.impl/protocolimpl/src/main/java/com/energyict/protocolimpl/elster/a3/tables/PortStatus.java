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
public class PortStatus {

    private int status; // 1 byte b0-5: Not defined and not used by the meter
                // b6: Initialization Status 1 = Device on port has been successfully initialized
                // b7: Outage Modem Present 1 = outage modem is present 
    private int currentSessionSecurityLevel; // 1 byte Group permission bits (refer to ST-42) for the session per the C12.18 Security Service or the C12.21 Authenticate Service. For the shared remote port, this field will be used to show the optical session security level during optical sessions. 
    private long actualConnectBitRate; // 4 bytes UINT32. Bit rate of the connection. Only can be different form Actual_Init_Rate if the remote port is connected to a modem that doesn't support speed buffering (i.e. outage modem.)
    private long actualInitRate; // 4 bytes Actual meter to modem successful initialization bit rate.
    private int nextNumbertoDial; // 1 byte The index into XT-93 to select the next phone number to be dialed for this port. Valid range = 0-2.
    private int retryTimer; // 2 bytes There is one retry timer per port. The retry attempts are kept per phone number
    private CallScheduleRecord[] callScheduleRecords;
            
    /** Creates a new instance of SourceDefinitionEntry */
    public PortStatus(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setStatus(C12ParseUtils.getInt(data,offset++));
        setCurrentSessionSecurityLevel(C12ParseUtils.getInt(data,offset++));
        setActualConnectBitRate(C12ParseUtils.getLong(data,offset, 4, dataOrder));offset+=4;
        setActualInitRate(C12ParseUtils.getLong(data,offset, 4, dataOrder));offset+=4;
        setNextNumbertoDial(C12ParseUtils.getInt(data,offset++));
        setRetryTimer(C12ParseUtils.getInt(data,offset,2, dataOrder));offset+=2;
        setCallScheduleRecords(new CallScheduleRecord[3]);
        for (int i=0;i<getCallScheduleRecords().length;i++) {
            getCallScheduleRecords()[i] = new CallScheduleRecord(data, offset, tableFactory);
            offset+=CallScheduleRecord.getSize(tableFactory);
        }
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PortStatus:\n");
        strBuff.append("   actualConnectBitRate="+getActualConnectBitRate()+"\n");
        strBuff.append("   actualInitRate="+getActualInitRate()+"\n");
        strBuff.append("   currentSessionSecurityLevel="+getCurrentSessionSecurityLevel()+"\n");
        strBuff.append("   nextNumbertoDial="+getNextNumbertoDial()+"\n");
        strBuff.append("   retryTimer="+getRetryTimer()+"\n");
        strBuff.append("   status="+getStatus()+"\n");
        for (int i=0;i<getCallScheduleRecords().length;i++) {
            strBuff.append("   callScheduleRecords["+i+"]="+getCallScheduleRecords()[i]+"\n");
        }
        return strBuff.toString();
    }    
     
    static public int getSize(TableFactory tableFactory) throws IOException {
        return 22;
    }   

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCurrentSessionSecurityLevel() {
        return currentSessionSecurityLevel;
    }

    public void setCurrentSessionSecurityLevel(int currentSessionSecurityLevel) {
        this.currentSessionSecurityLevel = currentSessionSecurityLevel;
    }

    public long getActualConnectBitRate() {
        return actualConnectBitRate;
    }

    public void setActualConnectBitRate(long actualConnectBitRate) {
        this.actualConnectBitRate = actualConnectBitRate;
    }

    public long getActualInitRate() {
        return actualInitRate;
    }

    public void setActualInitRate(long actualInitRate) {
        this.actualInitRate = actualInitRate;
    }

    public int getNextNumbertoDial() {
        return nextNumbertoDial;
    }

    public void setNextNumbertoDial(int nextNumbertoDial) {
        this.nextNumbertoDial = nextNumbertoDial;
    }

    public int getRetryTimer() {
        return retryTimer;
    }

    public void setRetryTimer(int retryTimer) {
        this.retryTimer = retryTimer;
    }

    public CallScheduleRecord[] getCallScheduleRecords() {
        return callScheduleRecords;
    }

    public void setCallScheduleRecords(CallScheduleRecord[] callScheduleRecords) {
        this.callScheduleRecords = callScheduleRecords;
    }


}
