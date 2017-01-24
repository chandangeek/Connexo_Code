/*
 * FileAccessSearchCommand.java
 *
 * Created on 30 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.edmi.mk6.core.DateTimeBuilder;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author koen
 */
public class FileAccessSearchCommand extends AbstractCommand {

    private int registerId;

    private Date date;
    private long startRecord;
    private int result; // 0 search backwards, 1 search forward
    private int direction;



    /** Creates a new instance of FileAccessSearchCommand */
    public FileAccessSearchCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected byte[] prepareBuild() {
        byte[] data = new byte[17];
        data[0] = 'F';
        data[1] = 'S';

        data[2] = (byte)((getRegisterId()>>24)&0xFF);
        data[3] = (byte)((getRegisterId()>>16)&0xFF);
        data[4] = (byte)((getRegisterId()>>8)&0xFF);
        data[5] = (byte)((getRegisterId())&0xFF);

        data[6] = (byte)((getStartRecord()>>24)&0xFF);
        data[7] = (byte)((getStartRecord()>>16)&0xFF);
        data[8] = (byte)((getStartRecord()>>8)&0xFF);
        data[9] = (byte)((getStartRecord())&0xFF);

        byte[] dateData = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(getDate(), getCommandFactory().getMk6().getTimeZone());
        System.arraycopy(dateData,0,data,10,dateData.length);

        data[10+dateData.length]=(byte)getDirection();

        return data;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FileAccessSearchCommand:\n");
        strBuff.append("   registerId=0x"+Integer.toHexString(getRegisterId())+"\n");
        strBuff.append("   startRecord="+getStartRecord()+"\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   result="+getResult()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 2;
        setRegisterId(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setStartRecord(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setDate(DateTimeBuilder.getDateFromDDMMYYHHMMSS(getCommandFactory().getMk6().getTimeZone(),data,offset));
        offset+=DateTimeBuilder.getDateFromDDMMYYHHMMSSSize();
        setResultection(ProtocolUtils.getInt(data,offset++,1));
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

    public void setResultection(int result) {
        this.result = result;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }



}
