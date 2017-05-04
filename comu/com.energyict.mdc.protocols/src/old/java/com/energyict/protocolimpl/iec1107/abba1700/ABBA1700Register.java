/*
 * ABBA1700Register.java
 *
 * Created on 24 april 2003, 17:29
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class ABBA1700Register extends ABBA1700RegisterData {

    static protected final boolean CACHED=true;
    static protected final boolean NOT_CACHED=false;

    static protected final boolean WRITEABLE=true;
    static protected final boolean NOT_WRITEABLE=false;

    private String dataId;
    private int type;
    private int offset;
    private int length;
    private Unit unit;

    private boolean writeable;
    private boolean cached;

    private ABBA1700RegisterFactory abba1700RegisterFactory=null;

    protected void setABBA1700RegisterFactory(ABBA1700RegisterFactory abba1700RegisterFactory) {
        this.abba1700RegisterFactory=abba1700RegisterFactory;
    }


    protected ABBA1700RegisterFactory getABBA1700RegisterFactory() {
        return abba1700RegisterFactory;
    }
    protected ABBA1700DataIdentityFactory getABBA1700DataIdentityFactory() {
        return getABBA1700RegisterFactory().getABBA1700DataIdentityFactory();
    }
    protected FlagIEC1107Connection getFlagIEC1107Connection() {
       return getABBA1700RegisterFactory().getProtocolLink().getFlagIEC1107Connection();
    }
    protected ProtocolLink getProtocolLink() {
       return getABBA1700RegisterFactory().getProtocolLink();
    }

    protected Unit getUnit() {
       return unit;
    }
    protected int getType() {
       return type;
    }
    protected int getOffset() {
       return offset;
    }
    protected int getLength() {
       return length;
    }
    protected ABBA1700MeterType getMeterType() {
        return getABBA1700RegisterFactory().getMeterType();
    }
    protected boolean isWriteable() {
       return writeable;
    }
    protected boolean isCached() {
       return cached;
    }

    protected String getDataID() {
       return dataId;
    }


    /** Creates a new instance of ABBA1700Register */
    protected ABBA1700Register(String dataId, int type, int offset,int length, Unit unit, boolean writeable, boolean cached) {
        this.dataId = dataId;
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.unit = unit;
        this.writeable = writeable;
        this.cached = cached;
    }


    protected void writeRegister(String value) throws FlagIEC1107ConnectionException,IOException {
        getABBA1700DataIdentityFactory().setDataIdentity(getDataID(),value);
    }

    protected void writeRegister(Object object) throws FlagIEC1107ConnectionException,IOException {
        getABBA1700DataIdentityFactory().setDataIdentity(getDataID(),buildData(object));
    }

    protected void invokeRegister() throws FlagIEC1107ConnectionException,IOException {
        getABBA1700DataIdentityFactory().invokeDataIdentity(getDataID());
    }

    // read register in iec1107 mode
    protected byte[] readRegister(boolean cached) throws FlagIEC1107ConnectionException,IOException {
         return readRegister(cached,-1,0);
    }
    protected byte[] readRegister(boolean cached, int set) throws FlagIEC1107ConnectionException,IOException {
         return readRegister(cached,-1,set);
    }
    protected byte[] readRegister(boolean cached,int dataLength,int set) throws FlagIEC1107ConnectionException,IOException {
         return(getABBA1700DataIdentityFactory().getDataIdentity(getDataID(),cached,dataLength,set));
    }

    // read register in streaming mode
    protected byte[] readRegisterStream(boolean cached,int nrOfBlocks) throws FlagIEC1107ConnectionException,IOException {
         return(getABBA1700DataIdentityFactory().getDataIdentityStream(getDataID(),cached,nrOfBlocks));
    }


}
