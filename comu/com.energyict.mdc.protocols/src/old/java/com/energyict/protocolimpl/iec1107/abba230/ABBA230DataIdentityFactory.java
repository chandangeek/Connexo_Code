/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author fbo */

public class ABBA230DataIdentityFactory {

    private final int dbg = 1;

    private Map rawRegisters = new TreeMap();
    private MeterExceptionInfo meterExceptionInfo=null;
    private ProtocolLink protocolLink;

    /** Create new DataIdentityFactory
     *
     * @param protocolLink
     * @param meterExceptionInfo
     */
    public ABBA230DataIdentityFactory(ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo ) {
        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo;
        initRegisters();

        if( dbg > 0 ) {
            Logger l = protocolLink.getLogger();
            if( dbg > 1 && l.isLoggable( Level.INFO ) ){
                l.log( Level.INFO, this.toString() );
            }
        }
    }

    /** @return protocolLink */
    ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /** @return meterExceptionInfo */
    MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

    /** Read DataIdentity the IEC1107 way
     * @param dataID
     * @param cached
     * @param dataLength
     * @param set from which to start reading
     * @throws java.io.IOException
     * @return results as raw byte array
     */
    byte[] getDataIdentity(String dataID,boolean cached,int dataLength, int set) throws IOException {
        try {
            ABBA230DataIdentity rawRegister = findRawRegister(dataID);
            return rawRegister.read(cached,(dataLength==-1?rawRegister.getLength():dataLength),set);
        } catch(FlagIEC1107ConnectionException e) {
            String msg = "ABBA230DataIdentityFactory, getDataIdentity, "
                    + "dataID=" + dataID + " " + e.getMessage();
            throw new IOException(msg);
        }
    }

    /** Read DataIdentity in streaming mode
     * @param dataID
     * @param cached
     * @param nrOfBlocks
     * @throws java.io.IOException
     * @return results as raw data stream
     */
    byte[] getDataIdentityStream(String dataID,boolean cached,int nrOfBlocks) throws IOException {
        System.out.println("getDataIdentityStream( " + dataID + " cached " + cached + " nrOfBlocks " + nrOfBlocks );
        try {
            ABBA230DataIdentity rawRegister = findRawRegister(dataID);
            if (!rawRegister.isStreameable()) {
				throw new IOException("ABBA230DataIdentity, getDataIdentityStream, data identity not streameable!");
			}
            return rawRegister.readStream(cached,nrOfBlocks);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA230DataIdentityFactory, getDataIdentityStream, "+e.getMessage());
        }
    }

    /** Write dataIdentity
     * @param dataID
     * @param value
     * @throws java.io.IOException
     */
    void setDataIdentity(String dataID,String value) throws IOException {
        try {
            ABBA230DataIdentity rawRegister = findRawRegister(dataID);
            rawRegister.writeRawRegister(value);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA230DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }

    void setDataIdentity(String dataID, int packet, String value) throws IOException {
        try {
            ABBA230DataIdentity rawRegister = new ABBA230DataIdentity(dataID,this);
            rawRegister.writeRawRegister(packet,value);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA230DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }
    void setDataIdentityHex(String dataID, int packet, String value) throws IOException {
        try {
            ABBA230DataIdentity rawRegister = new ABBA230DataIdentity(dataID,this);
            rawRegister.writeRawRegisterHex(packet,value);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA230DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }
    void setDataIdentityHex2(String dataID, int packet, String value) throws IOException {
        ABBA230DataIdentity rawRegister = new ABBA230DataIdentity(dataID,this);
        rawRegister.writeRawRegisterHex(packet,value);
    }


    private void initRegisters() {

        add("099", 1,ABBA230DataIdentity.NOT_STREAMEABLE);

        add("411", 1,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("412", 1,ABBA230DataIdentity.NOT_STREAMEABLE);

        add("798", 16,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("795", 8,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("861", 7,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("507", 128,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("509", 18,ABBA230DataIdentity.NOT_STREAMEABLE);
        add("510", 24,ABBA230DataIdentity.NOT_STREAMEABLE);
        // ct primary and secundary current
        add("616", 6,ABBA230DataIdentity.NOT_STREAMEABLE);

//        //Historical events
//        add("544", 5,  280,ABBA230DataIdentity.STREAMEABLE);
//        // Historical events
//        add("545", 280,5,ABBA230DataIdentity.STREAMEABLE);

        // Meter current system status
        add("724", 13,ABBA230DataIdentity.NOT_STREAMEABLE);

        // (C)MD register sources
        add("668", 8,ABBA230DataIdentity.NOT_STREAMEABLE);
        // Customer defined register 1,2 & 3 configuration
        add("600", 4,ABBA230DataIdentity.NOT_STREAMEABLE);
        // TOU register source
        add("667", 16,ABBA230DataIdentity.NOT_STREAMEABLE);
        // TOU registers
        add("508", 128,ABBA230DataIdentity.NOT_STREAMEABLE);
        // Historical values
        add("543", 302, 12,ABBA230DataIdentity.STREAMEABLE);
        // Daily historical values
        add("545", 302, 14,ABBA230DataIdentity.STREAMEABLE);

        // LOAD PROFILE CONFIG
        // Load profile read data
        add("550", 0,ABBA230DataIdentity.STREAMEABLE);
        // Load profile configure data (Read By Day)
        add("551", 4,ABBA230DataIdentity.NOT_STREAMEABLE);
        // Load profile configure data (Read By Date)
        add("554", 8, ABBA230DataIdentity.NOT_STREAMEABLE );
        // Load profile configuration
        add("777", 2,ABBA230DataIdentity.NOT_STREAMEABLE);
        // Demand period
        add("878", 3,ABBA230DataIdentity.NOT_STREAMEABLE);
        // Load Profile Daylight Savings Configuration
        add("778", 1, ABBA230DataIdentity.NOT_STREAMEABLE );

        // INSTRUMENTATION PROFILE CONFIG
        // Instrumentation profile read data
        add("555", 0, ABBA230DataIdentity.STREAMEABLE);
        // Instrumentation profile configure data (Read By Day)
        add("556", 4, ABBA230DataIdentity.NOT_STREAMEABLE);
        // Instrumentation profile configure data (Read By Date)
        add("558", 8, ABBA230DataIdentity.NOT_STREAMEABLE);
        // Instrumentation profile configuration
        add("775", 16, ABBA230DataIdentity.NOT_STREAMEABLE);
        // Instrumentation period
        add("879", 3, ABBA230DataIdentity.NOT_STREAMEABLE);
        // Instrumentation Profile Daylight Savings Configuration
        add("776", 1, ABBA230DataIdentity.NOT_STREAMEABLE );

        // event logs
        add("678", 83,ABBA230DataIdentity.STREAMEABLE); // OverVoltageEventLog
        add("679", 83,ABBA230DataIdentity.STREAMEABLE); // UnderVoltageEventLog
        add("680", 64+64+45,ABBA230DataIdentity.STREAMEABLE); // ProgrammingEventLog
        add("685", 83,ABBA230DataIdentity.STREAMEABLE); // LongPowerFailEventLog
        add("691", 83,ABBA230DataIdentity.STREAMEABLE); // TerminalCoverEventLog
        add("692", 83,ABBA230DataIdentity.STREAMEABLE); // MainCoverEventLog
        add("693", 83,ABBA230DataIdentity.STREAMEABLE); // MagneticTamperEventLog
        add("694", 43,ABBA230DataIdentity.STREAMEABLE); // ReverserunEventLog
        add("695", 83,ABBA230DataIdentity.STREAMEABLE); // PowerFailEventLog
        add("696", 43,ABBA230DataIdentity.STREAMEABLE); // TransientEventLog
        add("699", 53,ABBA230DataIdentity.STREAMEABLE); // EndOfBillingEventLog

        add("422", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorOpenOpticalLog
        add("423", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorOpenModuleLog
        add("424", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorOpenLoadMonitorLowEventLog
        add("425", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorOpenLoadMonitorHighEventLog
        add("426", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorOpenAutoDisconnectEventLog
        add("427", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorArmOpticalEventLog
        add("428", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorArmModuleEventLog
        add("429", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorArmLoadMonitorEventLog
        add("430", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorArmDisconnectEventLog
        add("431", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorCloseOpticalEventLog
        add("432", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorCloseModuleEventLog
        add("433", 53,ABBA230DataIdentity.STREAMEABLE); // ContactorCloseButtonEventLog
        add("655", 1,ABBA230DataIdentity.NOT_STREAMEABLE); // EndOfBillingPeriod


        add("701", 53,ABBA230DataIdentity.STREAMEABLE); // MeterErrorEventLog
        add("705", 43,ABBA230DataIdentity.STREAMEABLE); // BatteryVoltageLowEventLog
        add("998", 12,2,ABBA230DataIdentity.STREAMEABLE); // BatteryVoltageLowEventLog
    }

    private void add(String id, int length, boolean streamable ){
        add( id, length, 1, streamable );
    }

    private void add(String id, int length, int sets, boolean streamable ){
        ABBA230DataIdentity di = new ABBA230DataIdentity(id, length, sets, streamable, this );
        rawRegisters.put( id, di );
    }

    private ABBA230DataIdentity findRawRegister(String dataID) throws IOException {
        ABBA230DataIdentity rawRegister = (ABBA230DataIdentity)rawRegisters.get(dataID);
        if (rawRegister == null) {
			throw new IOException("ABBA230DataIdentityFactory, findRawRegister, "+dataID+" does not exist!");
		}
        return rawRegister;
    }

    private ABBA230DataIdentity setRawRegister(String dataID) throws IOException {
        ABBA230DataIdentity rawRegister = (ABBA230DataIdentity)rawRegisters.get(dataID);
        if (rawRegister == null) {
			throw new IOException("ABBA230DataIdentityFactory, findRawRegister, "+dataID+" does not exist!");
		}
        return rawRegister;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "ABBA230DataIdentityFactory [\n" );

        Iterator i = rawRegisters.values().iterator();
        while(i.hasNext()){
            ABBA230DataIdentity dataIdentity = (ABBA230DataIdentity)i.next();
            rslt.append( " " + dataIdentity.toString() + "\n" );

            if( dbg > 1 ) {
                try {
                    rslt.append( " " + DataType.toHexaString( dataIdentity.read(false, dataIdentity.getLength(), 0) ) );
                    rslt.append("\n");
                } catch (FlagIEC1107ConnectionException ex) {
                    rslt.append( ex.getMessage() );
                } catch (IOException ex) {
                    rslt.append( ex.getMessage() );
                }
            }
        }
        rslt.append( "]\n" );
        return rslt.toString();
    }
}
