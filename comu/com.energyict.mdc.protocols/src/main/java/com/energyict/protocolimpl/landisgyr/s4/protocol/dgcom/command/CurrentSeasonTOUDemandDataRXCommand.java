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
public class CurrentSeasonTOUDemandDataRXCommand extends AbstractCommand {

    public final int MAX_RATES=5;

    private Date[] maximumKWtimestampsRates = new Date[MAX_RATES];
    private int[] maximumKWsRates = new int[MAX_RATES];
    private Date[] maximumKMtimestampsRates = new Date[MAX_RATES];
    private int[] maximumKMsRates = new int[MAX_RATES];
    private int[] coincidentDemandsRates = new int[MAX_RATES];
    private int[] powerFactorAtMaxDemandRates = new int[MAX_RATES];

    /** Creates a new instance of TemplateCommand */
    public CurrentSeasonTOUDemandDataRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentSeasonTOUDemandDataRXCommand:\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   maximumKWsRates["+i+"]="+getMaximumKWsRates()[i]+"\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   maximumKWtimestampsRates["+i+"]="+getMaximumKWtimestampsRates()[i]+"\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   maximumKMsRates["+i+"]="+getMaximumKMsRates()[i]+"\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   maximumKMtimestampsRates["+i+"]="+getMaximumKMtimestampsRates()[i]+"\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   coincidentDemandsRates["+i+"]="+getCoincidentDemandsRates()[i]+"\n");
        for (int i=0;i<MAX_RATES;i++)
        strBuff.append("   powerFactorAtMaxDemandRates["+i+"]="+getPowerFactorAtMaxDemandRates()[i]+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX())
            return new byte[]{(byte)0xAC,0,0,0,0,0,0,0,0};
        else
            throw new IOException("CurrentSeasonTOUDemandDataRXCommand, only for RX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        for (int i=0;i<MAX_RATES;i++) {
            getMaximumKWtimestampsRates()[i] = Utils.getTimestampwwhhddYYDDMM(data,offset, getCommandFactory().getS4().getTimeZone());
            offset+=6;
        }
        for (int i=0;i<MAX_RATES;i++) {
            getMaximumKWsRates()[i] = ProtocolUtils.getIntLE(data,offset,2);
            offset+=2;
        }
        for (int i=0;i<MAX_RATES;i++) {
            getMaximumKMtimestampsRates()[i] = Utils.getTimestampwwhhddYYDDMM(data,offset, getCommandFactory().getS4().getTimeZone());
            offset+=6;
        }
        for (int i=0;i<MAX_RATES;i++) {
            getMaximumKMsRates()[i] = ProtocolUtils.getIntLE(data,offset,2);
            offset+=2;
        }
        for (int i=0;i<MAX_RATES;i++) {
            getCoincidentDemandsRates()[i] = ProtocolUtils.getIntLE(data,offset,2);
            offset+=2;
        }
        for (int i=0;i<MAX_RATES;i++) {
            getPowerFactorAtMaxDemandRates()[i] = (int)ParseUtils.getBCD2LongLE(data,offset,2);
            offset+=2;
        }
    }

    public Date[] getMaximumKWtimestampsRates() {
        return maximumKWtimestampsRates;
    }

    public void setMaximumKWtimestampsRates(Date[] maximumKWtimestampsRates) {
        this.maximumKWtimestampsRates = maximumKWtimestampsRates;
    }

    public int[] getMaximumKWsRates() {
        return maximumKWsRates;
    }

    public void setMaximumKWsRates(int[] maximumKWsRates) {
        this.maximumKWsRates = maximumKWsRates;
    }

    public Date[] getMaximumKMtimestampsRates() {
        return maximumKMtimestampsRates;
    }

    public void setMaximumKMtimestampsRates(Date[] maximumKMtimestampsRates) {
        this.maximumKMtimestampsRates = maximumKMtimestampsRates;
    }

    public int[] getMaximumKMsRates() {
        return maximumKMsRates;
    }

    public void setMaximumKMsRates(int[] maximumKMsRates) {
        this.maximumKMsRates = maximumKMsRates;
    }

    public int[] getCoincidentDemandsRates() {
        return coincidentDemandsRates;
    }

    public void setCoincidentDemandsRates(int[] coincidentDemandsRates) {
        this.coincidentDemandsRates = coincidentDemandsRates;
    }

    public int[] getPowerFactorAtMaxDemandRates() {
        return powerFactorAtMaxDemandRates;
    }

    public void setPowerFactorAtMaxDemandRates(int[] powerFactorAtMaxDemandRates) {
        this.powerFactorAtMaxDemandRates = powerFactorAtMaxDemandRates;
    }
}
