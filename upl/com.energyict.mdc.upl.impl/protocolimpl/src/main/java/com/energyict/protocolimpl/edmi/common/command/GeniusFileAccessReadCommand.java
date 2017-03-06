package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 *
 * @author koen
 */
public class GeniusFileAccessReadCommand extends AbstractCommand {

    private static final char[] FILE_ACCESS_READ_COMMAND = new char[]{'F', 'R'};

    private int registerId;
    
    private long startRecord;
    private int numberOfRecords;
    private int recordOffset;
    private int recordSize;
    private byte[] data;
 
    
    /** Creates a new instance of FileAccessRead */
    public GeniusFileAccessReadCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareBuild() {
        byte[] data = new byte[16];
        data[0] = (byte) FILE_ACCESS_READ_COMMAND[0];
        data[1] = (byte) FILE_ACCESS_READ_COMMAND[1];
        
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
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("FileAccessRead:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   startRecord="+getStartRecord()+"\n");
        strBuff.append("   numberOfRecords="+getNumberOfRecords()+"\n");
        strBuff.append("   recordOffset="+getRecordOffset()+"\n");
        strBuff.append("   recordSize="+getRecordSize()+"\n");
        strBuff.append("   data="+ProtocolUtils.outputHexString(getData())+"\n");
        strBuff.append("   data (string)="+new String(getData())+", ");
        return strBuff.toString();
    }
    
    protected void parse(byte[] data) throws CommandResponseException {
        if (data.length < 16) {
            throw new CommandResponseException("Response for File access read command should have a minimum length of 16; actual size was " + data.length);
        }

        int offset = 2;
        setRegisterId(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setNumberOfRecords(ProtocolTools.getIntFromBytes(data,offset,2));
        offset+=2;
        setRecordOffset(ProtocolTools.getIntFromBytes(data,offset,2));
        offset+=2;
        setRecordSize(ProtocolTools.getIntFromBytes(data,offset,2));
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