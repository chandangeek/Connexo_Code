/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class PreviousSeasonTOUDataDXCommand extends AbstractCommand {

    public final int NR_OF_RATES=4;

    private long[] cumulativeKWInPulses = new long[NR_OF_RATES];
    private int nrOfDemandResets;
    private Date timestampLastDemandReset;
    private Date[] timestampMaximumDemand = new Date[NR_OF_RATES];
    private int[] maximumDemandKW = new int[NR_OF_RATES];
    private long[] kWHInPulses = new long[NR_OF_RATES];
    private long totalKWHInPulses;
    private long totalNegativeKWHInPulses;

    /** Creates a new instance of TemplateCommand */
    public PreviousSeasonTOUDataDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousSeasonTOUDataDXCommand:\n");
        strBuff.append("   KWHInPulses="+getKWHInPulses()+"\n");
        strBuff.append("   cumulativeKWInPulses="+getCumulativeKWInPulses()+"\n");
        strBuff.append("   maximumDemandKW="+getMaximumDemandKW()+"\n");
        strBuff.append("   nrOfDemandResets="+getNrOfDemandResets()+"\n");
        strBuff.append("   timestampLastDemandReset="+getTimestampLastDemandReset()+"\n");
        strBuff.append("   timestampMaximumDemand="+getTimestampMaximumDemand()+"\n");
        strBuff.append("   totalKWHInPulses="+getTotalKWHInPulses()+"\n");
        strBuff.append("   totalNegativeKWHInPulses="+getTotalNegativeKWHInPulses()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x5C,0,0,0,0,0,0,0,0};
        else
            throw new IOException("PreviousSeasonTOUDataDXCommand, only for DX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        for(int i =0;i<NR_OF_RATES;i++) {
            getCumulativeKWInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }

        setNrOfDemandResets((int) ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;

        setTimestampLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone())); offset+=6;

        for(int i =0;i<NR_OF_RATES;i++) {
            getTimestampMaximumDemand()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone());
            offset+=6;
        }

        for(int i =0;i<NR_OF_RATES;i++) {
            getMaximumDemandKW()[i] = ProtocolUtils.getIntLE(data, offset, 2);
            offset+=2;
        }

        for(int i =0;i<NR_OF_RATES;i++) {
            getKWHInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }

        setTotalKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;

        setTotalNegativeKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
    }

    public long[] getCumulativeKWInPulses() {
        return cumulativeKWInPulses;
    }

    public void setCumulativeKWInPulses(long[] cumulativeKWInPulses) {
        this.cumulativeKWInPulses = cumulativeKWInPulses;
    }

    public int getNrOfDemandResets() {
        return nrOfDemandResets;
    }

    public void setNrOfDemandResets(int nrOfDemandResets) {
        this.nrOfDemandResets = nrOfDemandResets;
    }

    public Date getTimestampLastDemandReset() {
        return timestampLastDemandReset;
    }

    public void setTimestampLastDemandReset(Date timestampLastDemandReset) {
        this.timestampLastDemandReset = timestampLastDemandReset;
    }

    public Date[] getTimestampMaximumDemand() {
        return timestampMaximumDemand;
    }

    public void setTimestampMaximumDemand(Date[] timestampMaximumDemand) {
        this.timestampMaximumDemand = timestampMaximumDemand;
    }

    public int[] getMaximumDemandKW() {
        return maximumDemandKW;
    }

    public void setMaximumDemandKW(int[] maximumDemandKW) {
        this.maximumDemandKW = maximumDemandKW;
    }

    public long[] getKWHInPulses() {
        return kWHInPulses;
    }

    public void setKWHInPulses(long[] kWHInPulses) {
        this.kWHInPulses = kWHInPulses;
    }

    public long getTotalKWHInPulses() {
        return totalKWHInPulses;
    }

    public void setTotalKWHInPulses(long totalKWHInPulses) {
        this.totalKWHInPulses = totalKWHInPulses;
    }

    public long getTotalNegativeKWHInPulses() {
        return totalNegativeKWHInPulses;
    }

    public void setTotalNegativeKWHInPulses(long totalNegativeKWHInPulses) {
        this.totalNegativeKWHInPulses = totalNegativeKWHInPulses;
    }
}
