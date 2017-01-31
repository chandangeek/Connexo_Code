/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class CurrentSeasonLastResetValuesDXCommand extends AbstractCommand {

    private long totalKWHInPulsesAtLastDemandReset;
    private Date timestampMaxDemandAtLastDemandReset;
    private int maxKWInPulsesAtLastDemandReset;

    /** Creates a new instance of TemplateCommand */
    public CurrentSeasonLastResetValuesDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentSeasonLastResetValuesDXCommand:\n");
        strBuff.append("   maxKWInPulsesAtLastDemandReset="+getMaxKWInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   timestampMaxDemandAtLastDemandReset="+getTimestampMaxDemandAtLastDemandReset()+"\n");
        strBuff.append("   totalKWHInPulsesAtLastDemandReset="+getTotalKWHInPulsesAtLastDemandReset()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
         if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x85,0,0,0,0,0,0,0,0};
        else
            throw new IOException("CurrentSeasonLastResetValuesDXCommand, only for DX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setTotalKWHInPulsesAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;
        setTimestampMaxDemandAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setMaxKWInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2));offset+=2;
    }

    public long getTotalKWHInPulsesAtLastDemandReset() {
        return totalKWHInPulsesAtLastDemandReset;
    }

    public void setTotalKWHInPulsesAtLastDemandReset(long totalKWHInPulsesAtLastDemandReset) {
        this.totalKWHInPulsesAtLastDemandReset = totalKWHInPulsesAtLastDemandReset;
    }

    public Date getTimestampMaxDemandAtLastDemandReset() {
        return timestampMaxDemandAtLastDemandReset;
    }

    public void setTimestampMaxDemandAtLastDemandReset(Date timestampMaxDemandAtLastDemandReset) {
        this.timestampMaxDemandAtLastDemandReset = timestampMaxDemandAtLastDemandReset;
    }

    public int getMaxKWInPulsesAtLastDemandReset() {
        return maxKWInPulsesAtLastDemandReset;
    }

    public void setMaxKWInPulsesAtLastDemandReset(int maxKWInPulsesAtLastDemandReset) {
        this.maxKWInPulsesAtLastDemandReset = maxKWInPulsesAtLastDemandReset;
    }
}
