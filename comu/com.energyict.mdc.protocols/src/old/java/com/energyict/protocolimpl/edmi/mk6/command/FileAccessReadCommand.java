/*
 * FileAccessRead.java
 *
 * Created on 31 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author koen
 */
public class FileAccessReadCommand extends AbstractCommand {

    private int registerId;

    private long startRecord;
    private int numberOfRecords;
    private int recordOffset;
    private int recordSize;
    private byte[] data;


    /** Creates a new instance of FileAccessRead */
    public FileAccessReadCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected byte[] prepareBuild() {
        byte[] data = new byte[16];
        data[0] = 'F';
        data[1] = 'R';

        data[2] = (byte)((getRegisterId()>>24)&0xFF);
        data[3] = (byte)((getRegisterId()>>16)&0xFF);
        data[4] = (byte)((getRegisterId()>>8)&0xFF);
        data[5] = (byte)((getRegisterId())&0xFF);

        data[6] = (byte)((getStartRecord()>>24)&0xFF);
        data[7] = (byte)((getStartRecord()>>16)&0xFF);
        data[8] = (byte)((getStartRecord()>>8)&0xFF);
        data[9] = (byte)((getStartRecord())&0xFF);

        data[10] = (byte)((getNumberOfRecords()>>8)&0xFF);
        data[11] = (byte)((getNumberOfRecords())&0xFF);

        data[12] = (byte)((getRecordOffset()>>8)&0xFF);
        data[13] = (byte)((getRecordOffset())&0xFF);

        data[14] = (byte)((getRecordSize()>>8)&0xFF);
        data[15] = (byte)((getRecordSize())&0xFF);

        return data;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        return "FileAccessRead:\n" +
                "   registerId=0x" + Integer.toHexString(getRegisterId()) + "\n" +
                "   startRecord=" + getStartRecord() + "\n" +
                "   numberOfRecords=" + getNumberOfRecords() + "\n" +
                "   recordOffset=" + getRecordOffset() + "\n" +
                "   recordSize=" + getRecordSize() + "\n" +
                "   data=" + ProtocolUtils.outputHexString(getData()) + "\n" +
                "   data (string)=" + new String(getData()) + ", ";
    }
    protected void parse(byte[] data) throws IOException {
        int offset = 2;
        setRegisterId(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setNumberOfRecords(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRecordOffset(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRecordSize(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setData(ProtocolUtils.getSubArray(data,offset));
    }

    public int getRegisterId() {
        return registerId;
    }

    public void setRegisterId(int registerId) {
        this.registerId = registerId;
    }

    public long getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(long startRecord) {
        this.startRecord = startRecord;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public int getRecordOffset() {
        return recordOffset;
    }

    public void setRecordOffset(int recordOffset) {
        this.recordOffset = recordOffset;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }




}
