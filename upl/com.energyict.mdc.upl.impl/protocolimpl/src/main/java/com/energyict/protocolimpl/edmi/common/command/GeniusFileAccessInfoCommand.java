package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 *
 * @author koen
 */
public class GeniusFileAccessInfoCommand extends AbstractCommand {

    private static final char[] FILE_ACCESS_INFORMATION_COMMAND = new char[]{'F', 'I'};
    private int registerId;
    
    private long startRecord;
    private long numberOfRecords;
    private int recordSize;
    private int fileType;
    private String fileName;
    
    /** Creates a new instance of FileAccessInfoCommand */
    public GeniusFileAccessInfoCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("FileAccessInfoCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   startRecord="+getStartRecord()+"\n");
        strBuff.append("   numberOfRecords="+getNumberOfRecords()+"\n");
        strBuff.append("   recordSize="+getRecordSize()+"\n");
        strBuff.append("   fileType="+getFileType()+"\n");
        strBuff.append("   fileName="+getFileName()+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() {
        byte[] data = new byte[6];
        data[0] = (byte) FILE_ACCESS_INFORMATION_COMMAND[0];
        data[1] = (byte) FILE_ACCESS_INFORMATION_COMMAND[1];
        data[2] = (byte)((getRegisterId()>>24)&0xFF);
        data[3] = (byte)((getRegisterId()>>16)&0xFF);
        data[4] = (byte)((getRegisterId()>>8)&0xFF);
        data[5] = (byte)((getRegisterId())&0xFF);
        return data;
    }
    
    protected void parse(byte[] data) throws CommandResponseException {
        if (data.length < 15) {
            throw new CommandResponseException("Response for File access search command should have a minimum length of 15; actual size was " + data.length);
        }

        int offset = 2;
        setRegisterId(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setNumberOfRecords(ProtocolTools.getIntFromBytes(data,offset,2));   //TODO: isn't this 2 bytes?
        offset+=4;
        setRecordSize(ProtocolTools.getIntFromBytes(data,offset,2));
        offset+=2;
        setFileType(ProtocolTools.getIntFromBytes(data,offset++,1));
        setFileName(new String(ProtocolUtils.getSubArray(data,offset))); //TODO: why date.length - 2, CRC is already stripped of
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