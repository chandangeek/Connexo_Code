package com.energyict.protocolimpl.iec1107.abba1140;

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

public class ABBA1140DataIdentityFactory {

    private final int dbg = 1;

    private Map rawRegisters = new TreeMap();
    private MeterExceptionInfo meterExceptionInfo=null;
    private ProtocolLink protocolLink;

    /** Create new DataIdentityFactory
     *
     * @param protocolLink
     * @param meterExceptionInfo
     */
    public ABBA1140DataIdentityFactory(ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo ) {
        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo;
        initRegisters();

        if( dbg > 0 ) {
            Logger l = protocolLink.getLogger();
            if( dbg > 1 && l.isLoggable( Level.INFO ) )
                l.log( Level.INFO, this.toString() );
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
            ABBA1140DataIdentity rawRegister = findRawRegister(dataID);
            return rawRegister.read(cached,(dataLength==-1?rawRegister.getLength():dataLength),set);
        } catch(FlagIEC1107ConnectionException e) {
        	String msg = "ABBA1140DataIdentityFactory, getDataIdentity, "
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
            ABBA1140DataIdentity rawRegister = findRawRegister(dataID);
            if (!rawRegister.isStreameable())
                throw new IOException("ABBA1140DataIdentity, getDataIdentityStream, data identity not streameable!");
            return rawRegister.readStream(cached,nrOfBlocks);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140DataIdentityFactory, getDataIdentityStream, "+e.getMessage());
        }
    }

    /** Write dataIdentity
     * @param dataID
     * @param value
     * @throws java.io.IOException
     */
    void setDataIdentity(String dataID,String value) throws IOException {
        try {
            ABBA1140DataIdentity rawRegister = findRawRegister(dataID);
            rawRegister.writeRawRegister(value);
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }


    private void initRegisters() {

        add("798", 16,ABBA1140DataIdentity.NOT_STREAMEABLE);
        add("795", 8,ABBA1140DataIdentity.NOT_STREAMEABLE);
        add("861", 7,ABBA1140DataIdentity.NOT_STREAMEABLE);
        add("507", 128,ABBA1140DataIdentity.NOT_STREAMEABLE);
        add("509", 36,ABBA1140DataIdentity.NOT_STREAMEABLE);
        add("510", 144,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // ct primary and secundary current
        add("616", 8,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // Load profile configuration
        add("777", 2,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // demand, subinterval period
        add("878", 3,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // Load profile configure data
        add("551", 4,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // Load profile read data
        add("550", 0,ABBA1140DataIdentity.STREAMEABLE);
        // Historical events
        add("544", 280,5,ABBA1140DataIdentity.STREAMEABLE);
        // Meter current system status
        add("724", 4,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // (C)MD register sources
        add("668", 8,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // Customer defined register 1,2 & 3 configuration
        add("600", 4,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // TOU register source
        add("667", 8,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // TOU registers
        add("508", 64,ABBA1140DataIdentity.NOT_STREAMEABLE);
        // Historical values
        add("543", 457, 24 ,ABBA1140DataIdentity.STREAMEABLE);
        // Daily historical values
        add("545", 457, 14,ABBA1140DataIdentity.STREAMEABLE);
        // Configure Load Profile Read By Date
        add("554", 8, ABBA1140DataIdentity.NOT_STREAMEABLE );

        // Load profile configure data
        add("655", 1,ABBA1140DataIdentity.NOT_STREAMEABLE);

        add("778", 1, ABBA1140DataIdentity.NOT_STREAMEABLE);

        // event logs
//        add("691", 83,ABBA1140DataIdentity.STREAMEABLE); // TerminalCoverEventLog
//        add("692", 83,ABBA1140DataIdentity.STREAMEABLE); // MainCoverEventLog
//        add("693", 83,ABBA1140DataIdentity.STREAMEABLE); // PhaseFailureEventLog
//        add("694", 43,ABBA1140DataIdentity.STREAMEABLE); // ReverserunEventLog
//        add("695", 83,ABBA1140DataIdentity.STREAMEABLE); // PowerFailEventLog
//        add("696", 43,ABBA1140DataIdentity.STREAMEABLE); // TransientEventLog
//        add("699", 53,ABBA1140DataIdentity.STREAMEABLE); // EndOfBillingEventLog
//        add("701", 53,ABBA1140DataIdentity.STREAMEABLE); // MeterErrorEventLog
        add("691", 14,ABBA1140DataIdentity.STREAMEABLE); // TerminalCoverEventLog
        add("692", 14,ABBA1140DataIdentity.STREAMEABLE); // MainCoverEventLog
        add("693", 17,ABBA1140DataIdentity.STREAMEABLE); // PhaseFailureEventLog
        add("694", 14,ABBA1140DataIdentity.STREAMEABLE); // ReverserunEventLog
        add("695", 14,ABBA1140DataIdentity.STREAMEABLE); // PowerFailEventLog
        add("696", 14,ABBA1140DataIdentity.STREAMEABLE); // TransientEventLog
        add("697", 14,ABBA1140DataIdentity.STREAMEABLE); // InternalBatteryEventLog
        add("699", 17,ABBA1140DataIdentity.STREAMEABLE); // EndOfBillingEventLog
        add("701", 14,ABBA1140DataIdentity.STREAMEABLE); // MeterErrorEventLog


        // Firmware version string and cuircuit board serial number
        add("998", 12, ABBA1140DataIdentity.NOT_STREAMEABLE);

    }

    private void add(String id, int length, boolean streamable ){
        add( id, length, 1, streamable );
    }

    private void add(String id, int length, int sets, boolean streamable ){
        ABBA1140DataIdentity di = new ABBA1140DataIdentity(id, length, sets, streamable, this );
        rawRegisters.put( id, di );
    }

    private ABBA1140DataIdentity findRawRegister(String dataID) throws IOException {
        ABBA1140DataIdentity rawRegister = (ABBA1140DataIdentity)rawRegisters.get(dataID);
        if (rawRegister == null)
            throw new IOException("ABBA1140DataIdentityFactory, findRawRegister, "+dataID+" does not exist!");
        return rawRegister;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "ABBA1140DataIdentityFactory [\n" );

        Iterator i = rawRegisters.values().iterator();
        while(i.hasNext()){
            ABBA1140DataIdentity dataIdentity = (ABBA1140DataIdentity)i.next();
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
