/*
 * ABBA1700DataIdentityFactory.java
 *
 * Created on 17 juni 2003, 10:28
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author  Koen
 */
public class ABBA1700DataIdentityFactory {

    private Map<String, ABBA1700DataIdentity> rawRegisters = new HashMap<>();
    private ProtocolLink protocolLink=null;
    private MeterExceptionInfo meterExceptionInfo=null; // KV 19012004
    private ABBA1700MeterType meterType;

    /**
     * Creates a new instance of ABBA1700DataIdentityFactory
     */
    public ABBA1700DataIdentityFactory(ProtocolLink protocolLink,MeterExceptionInfo meterExceptionInfo,ABBA1700MeterType meterType) {
        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo; // KV 19012004
        this.meterType = meterType;
        initRegisters();
        initLocals();
    }

    private void initLocals() {
        for (ABBA1700DataIdentity di : rawRegisters.values()) {
            di.setABBA1700DataIdentityFactory(this);
        }
    }

    // Lengths are in hexadecimal!

    private void initRegisters() {
        rawRegisters.put("798", new ABBA1700DataIdentity(16, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("795", new ABBA1700DataIdentity(16, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("861", new ABBA1700DataIdentity(7, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("507", new ABBA1700DataIdentity(80, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("516", new ABBA1700DataIdentity(32, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("509", new ABBA1700DataIdentity(72, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("510", new ABBA1700DataIdentity(288, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // vt primary and secundary current
        rawRegisters.put("614", new ABBA1700DataIdentity(7, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // ct primary and secundary current
        rawRegisters.put("616", new ABBA1700DataIdentity(6, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Load profile configuration
        rawRegisters.put("777", new ABBA1700DataIdentity(2, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // demand, subinterval period
        rawRegisters.put("878", new ABBA1700DataIdentity(3, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Load profile configure data
        rawRegisters.put("551", new ABBA1700DataIdentity(4, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Load profile read data
        rawRegisters.put("550", new ABBA1700DataIdentity(0, ABBA1700DataIdentity.STREAMEABLE));
        // Historical events KV_TO_DO how to use???
        rawRegisters.put("544", new ABBA1700DataIdentity(66, 12, ABBA1700DataIdentity.STREAMEABLE));
        // Meter historical system status
        rawRegisters.put("691", new ABBA1700DataIdentity(4, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Meter current system status
        rawRegisters.put("724", new ABBA1700DataIdentity(4, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // (C)MD register sources  KV_TO_DO not used for the moment. We extract the registersource from the (C)MD register itself
        rawRegisters.put("668", new ABBA1700DataIdentity(8, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Customer defined register 1,2 & 3 configuration
        rawRegisters.put("601", new ABBA1700DataIdentity(meterType.hasExtendedCustomerRegisters() ? 15 : 6, ABBA1700DataIdentity.NOT_STREAMEABLE));

        // TOU register source
        rawRegisters.put("667", new ABBA1700DataIdentity(meterType.getNrOfTariffRegisters(), ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Historic display scalings (billing point TOU and (C)MD register sources. Protocoldescription seems to be wrong!
        rawRegisters.put("548", new ABBA1700DataIdentity(22 + meterType.getNrOfTariffRegisters() + 8 + meterType.getExtraOffsetHistoricDisplayScaling(), 12, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // TOU registers
        rawRegisters.put("508", new ABBA1700DataIdentity(8 * meterType.getNrOfTariffRegisters(), ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Historical values
        rawRegisters.put("543", new ABBA1700DataIdentity(10 * 8 + meterType.getNrOfTariffRegisters() * 8 + 4 * 8 + 8 * 9 + 24 * 12 + (meterType.hasExtendedCustomerRegisters() ? 5 * 24 : 0) + 15, 12, ABBA1700DataIdentity.STREAMEABLE));

        // Instantaneous values
        //rawRegisters.put("605"), new ABBA1700DataIdentity(6,ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("605", new ABBA1700DataIdentity(1, ABBA1700DataIdentity.NOT_STREAMEABLE));
        rawRegisters.put("606", new ABBA1700DataIdentity(7, ABBA1700DataIdentity.NOT_STREAMEABLE));

        // KV 30082006
        rawRegisters.put("655", new ABBA1700DataIdentity(0, ABBA1700DataIdentity.NOT_STREAMEABLE));

        // ProgrammingCounter
        rawRegisters.put("680", new ABBA1700DataIdentity(14, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // PhaseFailure counter
        rawRegisters.put("693", new ABBA1700DataIdentity(meterType.getType()==ABBA1700MeterType.METERTYPE_32_TOU_5_CDR?63:17, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // ReverseRun Counter
        rawRegisters.put("694", new ABBA1700DataIdentity(meterType.getType()==ABBA1700MeterType.METERTYPE_32_TOU_5_CDR?46:14, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // PowerDown counter
        rawRegisters.put("695", new ABBA1700DataIdentity(meterType.getType()==ABBA1700MeterType.METERTYPE_32_TOU_5_CDR?22:14, ABBA1700DataIdentity.NOT_STREAMEABLE));
        // Battery status
        rawRegisters.put("546", new ABBA1700DataIdentity(12, ABBA1700DataIdentity.NOT_STREAMEABLE));
    }

    protected ProtocolLink getProtocolLink() {
       return protocolLink;
    }

    protected Map getRawRegisters() {
       return rawRegisters;
    }

    public void setDataIdentity(String dataID,String value) throws IOException {

        try {
           ABBA1700DataIdentity rawRegister = findRawRegister(dataID);
           rawRegister.writeRawRegister(dataID,value);
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }

    public void invokeDataIdentity(String dataID) throws IOException {

        try {
           ABBA1700DataIdentity rawRegister = findRawRegister(dataID);
           rawRegister.writeRawRegister(dataID,"");
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700DataIdentityFactory, setDataIdentity, "+e.getMessage());
        }
    }

    // search the map for the register info

    private ABBA1700DataIdentity findRawRegister(String dataID) throws IOException {
       ABBA1700DataIdentity rawRegister = (ABBA1700DataIdentity)rawRegisters.get(dataID);
        if (rawRegister == null) {
            throw new IOException("ABBA1700DataIdentityFactory, findRawRegister, " + dataID + " does not exist!");
        } else {
            return rawRegister;
    }
    }

    public byte[] getDataIdentityStream(String dataID,boolean cached,int nrOfBlocks) throws IOException {
        try {
           ABBA1700DataIdentity rawRegister = findRawRegister(dataID);
            if (!rawRegister.isStreameable()) {
                throw new IOException("ABBA1700DataIdentity, getDataIdentityStream, data identity not streameable!");
            }
           return rawRegister.readRawRegisterStream(dataID,cached,nrOfBlocks);
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700DataIdentityFactory, getDataIdentityStream, "+e.getMessage());
        }
    }

    public byte[] getDataIdentity(String dataID,boolean cached,int dataLength, int set) throws IOException {

        try {
           ABBA1700DataIdentity rawRegister = findRawRegister(dataID);
           return rawRegister.readRawRegister(dataID,cached,(dataLength==-1?rawRegister.getLength():dataLength),set);
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700DataIdentityFactory, getDataIdentity, "+e.getMessage());
        }
    }

    /**
     * Getter for property meterExceptionInfo.
     *
     * @return Value of property meterExceptionInfo.
     */
    // KV 19012004
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

}