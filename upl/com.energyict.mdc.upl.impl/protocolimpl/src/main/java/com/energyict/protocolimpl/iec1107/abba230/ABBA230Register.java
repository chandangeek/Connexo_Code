package com.energyict.protocolimpl.iec1107.abba230;


import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/** @author Koen & fbo */

public class ABBA230Register extends ABBA230RegisterData {
    
    static protected final boolean CACHED = true;
    
    static protected final boolean NOT_CACHED = false;
    
    static protected final boolean WRITEABLE = true;
    
    static protected final boolean NOT_WRITEABLE = false;
    
    private String dataId;
    private String name;
    private int type;
    private int offset;
    private int length;
    private Unit unit;
    private boolean writeable;
    private boolean cached;
    private ABBA230RegisterFactory registerFactory = null;
    
    /** Creates a new instance of ABBA230Register */
    protected ABBA230Register(
        String dataId, String name, int type, int offset, int length,
        Unit unit, boolean writeable, boolean cached, 
        ABBA230RegisterFactory registerFactory) {
        
        this.dataId = dataId;
        this.name = name;
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.unit = unit;
        this.writeable = writeable;
        this.cached = cached;
        this.registerFactory = registerFactory;
    }
    
    protected String getDataID() {
        return dataId;
    }
    
    protected String getName(){
        return name;
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
    
    protected Unit getUnit() {
        return unit;
    }
    
    protected boolean isWriteable() {
        return writeable;
    }
    
    protected boolean isCached() {
        return cached;
    }
  
    protected ABBA230RegisterFactory getRegisterFactory() {
        return registerFactory;
    }
    
    protected ABBA230DataIdentityFactory getABBA230DataIdentityFactory() {
        return registerFactory.getABBA230DataIdentityFactory();
    }
    
    protected FlagIEC1107Connection getFlagIEC1107Connection() {
        return registerFactory.getProtocolLink().getFlagIEC1107Connection();
    }
    
    protected ProtocolLink getProtocolLink() {
        return registerFactory.getProtocolLink();
    }
    
    protected void writeRegister(String value)
    throws FlagIEC1107ConnectionException, IOException {
        getABBA230DataIdentityFactory().setDataIdentity(getDataID(), value);
    }
    
    protected void writeRegister(Object object)
    throws FlagIEC1107ConnectionException, IOException {
        getABBA230DataIdentityFactory().setDataIdentity(getDataID(), buildData(object));
    }
    
    protected byte[] readRegister(boolean cached)
            throws IOException {
        return readRegister(cached, -1, 0);
    }
    
    protected byte[] readRegister(boolean cached, int set)
            throws IOException {
        return readRegister(cached, -1, set);
    }
    
    protected byte[] readRegister(boolean cached, int dataLength, int set)
            throws IOException {
        return (getABBA230DataIdentityFactory().getDataIdentity(getDataID(), cached, dataLength, set));
    }
    
    protected byte[] readRegisterStream(boolean cached, int nrOfBlocks)
    throws FlagIEC1107ConnectionException, IOException {
        return (getABBA230DataIdentityFactory().getDataIdentityStream(getDataID(), cached, nrOfBlocks));
    }
    
    public String toString() {
        return new StringBuffer()
        .append("ABBA230Register[ ")
        .append("id=" + dataId + ", ")
        .append("name=" + name + ", " )
        .append("offset=" + offset + ", ")
        .append("length=" + length + ", ")
        .append( "" + ( (writeable) ? "" : "not " ) + "writeable, ")
        .append( "" + ( (cached) ? "" : "not " ) + "cached ")
        .append(" ]")
        .toString();
    }
    
}
