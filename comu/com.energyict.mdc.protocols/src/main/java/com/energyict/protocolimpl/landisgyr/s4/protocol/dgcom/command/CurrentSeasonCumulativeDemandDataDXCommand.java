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
public class CurrentSeasonCumulativeDemandDataDXCommand extends AbstractCommand {

    public final int NR_OF_RATES=4;

    private long[] cumulativeDemandRatesInPulses = new long[NR_OF_RATES];
    private int nrOfDemandResets;
    private Date lastDemandResetTimeStamp;

    /** Creates a new instance of TemplateCommand */
    public CurrentSeasonCumulativeDemandDataDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentSeasonCumulativeDemandDataDXCommand:\n");
        for (int i=0;i<NR_OF_RATES;i++)
        strBuff.append("   cumulativeDemandRatesInPulses["+i+"]="+getCumulativeDemandRatesInPulses()[i]+"\n");
        strBuff.append("   lastDemandResetTimeStamp="+getLastDemandResetTimeStamp()+"\n");
        strBuff.append("   nrOfDemandResets="+getNrOfDemandResets()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x5B,0,0,0,0,0,0,0,0};
        else
            throw new IOException("CurrentSeasonCumulativeDemandDataCommand, only for DX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        for (int i=0;i<NR_OF_RATES;i++) {
            getCumulativeDemandRatesInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setNrOfDemandResets(ProtocolUtils.getIntLE(data,offset,2));
        offset+=2;
        setLastDemandResetTimeStamp(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));
    }

    public long[] getCumulativeDemandRatesInPulses() {
        return cumulativeDemandRatesInPulses;
    }

    public void setCumulativeDemandRatesInPulses(long[] cumulativeDemandRatesInPulses) {
        this.cumulativeDemandRatesInPulses = cumulativeDemandRatesInPulses;
    }

    public int getNrOfDemandResets() {
        return nrOfDemandResets;
    }

    public void setNrOfDemandResets(int nrOfDemandResets) {
        this.nrOfDemandResets = nrOfDemandResets;
    }

    public Date getLastDemandResetTimeStamp() {
        return lastDemandResetTimeStamp;
    }

    public void setLastDemandResetTimeStamp(Date lastDemandResetTimeStamp) {
        this.lastDemandResetTimeStamp = lastDemandResetTimeStamp;
    }
}
