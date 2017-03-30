/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DumpCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class DumpCommand extends AbstractCommand {

    private int nrOfBlocks=0xFFFF;
    private int channels=0xFF;
    private boolean dumpHistoryLog=true;
    private boolean dumpLoadControlMessageTable=false;

    private Date date;
    private Calendar lastEndingInterval;

    /** Creates a new instance of ForceStatusCommand */
    public DumpCommand(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DumpCommand:\n");
        strBuff.append("   channels="+getChannels()+"\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   dumpHistoryLog="+isDumpHistoryLog()+"\n");
        strBuff.append("   dumpLoadControlMessageTable="+isDumpLoadControlMessageTable()+"\n");
        strBuff.append("   nrOfBlocks="+getNrOfBlocks()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setLastEndingInterval(ProtocolUtils.getCleanCalendar(getCommandFactory().getS200().getTimeZone()));
        getLastEndingInterval().set(Calendar.MONTH,((int)ProtocolUtils.BCD2hex(data[offset++])&0xFF)-1);
        getLastEndingInterval().set(Calendar.DAY_OF_MONTH,(int)ProtocolUtils.BCD2hex(data[offset++])&0xFF);
        getLastEndingInterval().set(Calendar.DAY_OF_WEEK,(int)ProtocolUtils.BCD2hex(data[offset++])&0xFF);
        getLastEndingInterval().set(Calendar.HOUR_OF_DAY,(int)ProtocolUtils.BCD2hex(data[offset++])&0xFF);
        getLastEndingInterval().set(Calendar.MINUTE,(int)ProtocolUtils.BCD2hex(data[offset++])&0xFF);
        getLastEndingInterval().set(Calendar.SECOND,(int)ProtocolUtils.BCD2hex(data[offset++])&0xFF);

        // set the year
        Calendar calSystem = ProtocolUtils.getCalendar(getCommandFactory().getS200().getTimeZone());
        ParseUtils.roundDown2nearestInterval(calSystem, getCommandFactory().getBeginRecordTimeCommand().getProfileInterval()*60);
        getLastEndingInterval().set(Calendar.YEAR,calSystem.get(Calendar.YEAR));


        setDate(getLastEndingInterval().getTime());
    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('D');
    }

    protected byte[] prepareData() throws IOException {
        byte[] data = new byte[6];

        data[0] = (byte)(getNrOfBlocks() & 0xFF);
        data[1] = (byte)getChannels();
        data[2] = (byte)(isDumpHistoryLog()?0:'H');
        data[3] = (byte)(isDumpLoadControlMessageTable()?1:0);
        data[4] = (byte)((getNrOfBlocks()>>8) & 0xFF);
        data[5] = 0;

        return data;
    }

    public int getNrOfBlocks() {
        return nrOfBlocks;
    }

    public void setNrOfBlocks(int nrOfBlocks) {
        this.nrOfBlocks = nrOfBlocks;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public boolean isDumpHistoryLog() {
        return dumpHistoryLog;
    }

    public void setDumpHistoryLog(boolean dumpHistoryLog) {
        this.dumpHistoryLog = dumpHistoryLog;
    }

    public boolean isDumpLoadControlMessageTable() {
        return dumpLoadControlMessageTable;
    }

    public void setDumpLoadControlMessageTable(boolean dumpLoadControlMessageTable) {
        this.dumpLoadControlMessageTable = dumpLoadControlMessageTable;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Calendar getLastEndingInterval() {
        return lastEndingInterval;
    }

    private void setLastEndingInterval(Calendar lastEndingInterval) {
        this.lastEndingInterval = lastEndingInterval;
    }

}
