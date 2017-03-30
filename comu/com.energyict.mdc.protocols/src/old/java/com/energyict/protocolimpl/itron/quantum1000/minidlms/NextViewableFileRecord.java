/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * NextViewableFileRecord.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class NextViewableFileRecord extends AbstractDataDefinition {

    abstract protected void parseData(byte[] data) throws IOException;

    private long currentRecordNumber; // unsigned32,
    private long numberRecords; // unsigned32,
    private byte[] data; // OctetString (as defined in view object config)

    /**
     * Creates a new instance of NextViewableFileRecord
     */
    public NextViewableFileRecord(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("NextViewableFileRecord:\n");
        strBuff.append("   currentRecordNumber="+getCurrentRecordNumber()+"\n");
        for (int i=0;i<getData().length;i++) {
            strBuff.append("       data["+i+"]="+getData()[i]+"\n");
        }
        strBuff.append("   numberRecords="+getNumberRecords()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x003F; // 63 READ DLMS_NEXT_VIEWABLE_FILE_RECORD
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setCurrentRecordNumber(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setNumberRecords(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setData(ProtocolUtils.getSubArray(data,offset));
        parseData(getData());
    }

    public long getCurrentRecordNumber() {
        return currentRecordNumber;
    }

    public void setCurrentRecordNumber(long currentRecordNumber) {
        this.currentRecordNumber = currentRecordNumber;
    }

    public long getNumberRecords() {
        return numberRecords;
    }

    public void setNumberRecords(long numberRecords) {
        this.numberRecords = numberRecords;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
