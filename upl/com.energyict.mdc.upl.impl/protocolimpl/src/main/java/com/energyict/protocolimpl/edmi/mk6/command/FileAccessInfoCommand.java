/*
 * FileAccessInfoCommand.java
 *
 * Created on 29 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 *
 * @author koen
 */
public class FileAccessInfoCommand extends AbstractCommand {

    private int registerId;

    private long startRecord;
    private long numberOfRecords;
    private int recordSize;
    private int fileType;
    private String fileName;



    /** Creates a new instance of FileAccessInfoCommand */
    public FileAccessInfoCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FileAccessInfoCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   startRecord="+getStartRecord()+"\n");
        strBuff.append("   numberOfRecords="+getNumberOfRecords()+"\n");
        strBuff.append("   recordSize="+getRecordSize()+"\n");
        strBuff.append("   fileType="+getFileType()+"\n");
        strBuff.append("   fileName="+getFileName()+"\n");
        return strBuff.toString();
    }

//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new FileAccessInfoCommand(null)));
//    }

    protected byte[] prepareBuild() {
        byte[] data = new byte[6];
        data[0] = 'F';
        data[1] = 'I';
        data[2] = (byte)((getRegisterId()>>24)&0xFF);
        data[3] = (byte)((getRegisterId()>>16)&0xFF);
        data[4] = (byte)((getRegisterId()>>8)&0xFF);
        data[5] = (byte)((getRegisterId())&0xFF);
        return data;
    }

    protected void parse(byte[] data) throws ProtocolException {
        int offset = 2;
        setRegisterId(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setNumberOfRecords(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setRecordSize(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setFileType(ProtocolUtils.getInt(data,offset++,1));
        setFileName(new String(ProtocolUtils.getSubArray(data,offset, data.length-2)));
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

    public long getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(long numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }



}
