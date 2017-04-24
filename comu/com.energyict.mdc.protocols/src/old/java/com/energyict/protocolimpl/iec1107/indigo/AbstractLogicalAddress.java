/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractLogicalAddress.java
 *
 * Created on 7 juli 2004, 11:03
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
abstract public class AbstractLogicalAddress {

    abstract public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException;

    //byte[] data;

    int size;
    int id;

    LogicalAddressFactory logicalAddressFactory;

    /** Creates a new instance of AbstractLogicalAddress */
    public AbstractLogicalAddress(int id,int size,LogicalAddressFactory logicalAddressFactory) {
        this.id=id;
        this.size=size;
        this.logicalAddressFactory=logicalAddressFactory;
    }

    public void retrieve() throws IOException {
        byte[] data = getLogicalAddress();
        parse(data,getLogicalAddressFactory().getProtocolLink().getTimeZone());
    }

    /*
     *  Must be overridden to implement the data builder...
     */
    protected byte[] buildData() {
        return null;
    }

    public void write() throws IOException {
        setLogicalAddress(buildData());
    }

    protected int getScaler() throws IOException {
        return getLogicalAddressFactory().getHistoricalData(getId()%0x100).getScaler();
    }
    protected Date getBillingTimestamp() throws IOException {
        return getLogicalAddressFactory().getHistoricalData(getId()%0x100).getBillingDate();
    }

    private void setLogicalAddress(byte[] data) throws FlagIEC1107ConnectionException,IOException {
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(Integer.toHexString(getId()).toUpperCase());
        strbuff.append('(');
        strbuff.append(new String(data));
        strbuff.append(')');
        String str = getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,strbuff.toString().getBytes());
        if (str != null)
            validateData(str);
    }

    private byte[] getLogicalAddress() throws FlagIEC1107ConnectionException,IOException {
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(Integer.toHexString(getId()).toUpperCase());
        strbuff.append('(');
        strbuff.append(buildLength(getSize(),4).toUpperCase());
        strbuff.append(')');
        getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ1,strbuff.toString().getBytes());
        byte[] ba = getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().receiveRawData();
        return ProtocolUtils.convert2ascii(validateData(ba));
    }

    protected String buildLength(int value,int length) {
        String str=Integer.toHexString(value);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }

    private void validateData(String str) throws FlagIEC1107ConnectionException {
        validateData(str.getBytes());
    }

    private byte[] validateData(byte[] data) throws FlagIEC1107ConnectionException {
        String str = new String(data);
        // We know about ERRDAT and ERRADD as returned error codes from the Indigo+ meter.
        // Probably there are more...
        if (str.indexOf("ERR") != -1) {
            throw new FlagIEC1107ConnectionException("AbstractLogicalAddress, validateData, "+getLogicalAddressFactory().getMeterExceptionInfo().getExceptionInfo(str));
        }
        return getLogicalAddressFactory().getProtocolLink().getFlagIEC1107Connection().parseDataBetweenBrackets(data);
    }

    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }

    /**
     * Setter for property logicalAddressFactory.
     * @param logicalAddressFactory New value of property logicalAddressFactory.
     */
    public void setLogicalAddressFactory(com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory logicalAddressFactory) {
        this.logicalAddressFactory = logicalAddressFactory;
    }

    /**
     * Getter for property size.
     * @return Value of property size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return id;
    }

}
