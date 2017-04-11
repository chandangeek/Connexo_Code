package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Date;

/**
 *
 * @author koen
 */
public class GeniusFileAccessSearchCommand extends AbstractCommand {

    private static final char[] FILE_ACCESS_SEARCH_COMMAND = new char[]{'F', 'S'};
    
    private int registerId;
    
    private Date date;
    private long startRecord;
    private int result; // 0 search backwards, 1 search forward
    private int direction;

    
    /** Creates a new instance of FileAccessSearchCommand */
    public GeniusFileAccessSearchCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    protected byte[] prepareBuild() {
        byte[] data = new byte[17];
        data[0] = (byte) FILE_ACCESS_SEARCH_COMMAND[0];
        data[1] = (byte) FILE_ACCESS_SEARCH_COMMAND[1];
        
        data[2] = (byte)((getRegisterId()>>24)&0xFF);
        data[3] = (byte)((getRegisterId()>>16)&0xFF);
        data[4] = (byte)((getRegisterId()>>8)&0xFF);
        data[5] = (byte)((getRegisterId())&0xFF);
        
        data[6] = (byte)((getStartRecord()>>24)&0xFF);
        data[7] = (byte)((getStartRecord()>>16)&0xFF);
        data[8] = (byte)((getStartRecord()>>8)&0xFF);
        data[9] = (byte)((getStartRecord())&0xFF);
        
        byte[] dateData = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(getDate(), getCommandFactory().getProtocol().getTimeZone());
        System.arraycopy(dateData,0,data,10,dateData.length);
        
        data[10+dateData.length]=(byte)getDirection();
        
        return data;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("FileAccessSearchCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   startRecord="+getStartRecord()+"\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   result="+getResult()+"\n");
        return strBuff.toString();
    }    
    
    protected void parse(byte[] data) throws CommandResponseException {
        if (data.length < 17) {
            throw new CommandResponseException("Response for File access search command should have a length of 17; actual size was " + data.length);
        }

        int offset = 2;
        setRegisterId(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolTools.getIntFromBytes(data,offset,4));
        offset+=4;
        setDate(DateTimeBuilder.getDateFromDDMMYYHHMMSS(getCommandFactory().getProtocol().getTimeZone(),data,offset));
        offset+=DateTimeBuilder.getDateFromDDMMYYHHMMSSSize();
        setResult(ProtocolTools.getIntFromBytes(data,offset,1));
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}