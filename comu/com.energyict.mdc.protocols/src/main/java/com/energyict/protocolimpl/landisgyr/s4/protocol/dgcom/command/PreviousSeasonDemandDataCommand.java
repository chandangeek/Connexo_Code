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
public class PreviousSeasonDemandDataCommand extends AbstractCommand {


    private long previousSeasonCumulativeKWInPulses;
    private Date previousSeasonTimestampMaximumKW;
    private int previousSeasonMaximumKWInPulses;
    private long currentSeasonCumulativeKWInPulses;  // Energy & Demand --> cumulative demand

    private long previousSeasonCumulativeKMInPulses;
    private Date previousSeasonTimestampMaximumKM;
    private int previousSeasonMaximumKMInPulses;
    private long currentSeasonCumulativeKMInPulses;

    private int powerFactorAtPreviousSeasonMaxKW;
    private int powerFactorAtPreviousSeasonMaxKM;

    private int previousSeasonCoincidentDemandInPulses;

    private Date previousSeasonTimestampMaximumKM3;
    private int previousSeasonMaximumKM3InPulses;
    private int powerFactorAtPreviousSeasonMaxKM3;
    private int coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses;
    private long previousSeasonTotalKM3h;


    /** Creates a new instance of TemplateCommand */
    public PreviousSeasonDemandDataCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousSeasonDemandDataCommand:\n");
        strBuff.append("   coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses="+getCoincidentKM3AtPreviousSeasonMaxBillingDemandInPulses()+"\n");
        strBuff.append("   currentSeasonCumulativeKMInPulses="+getCurrentSeasonCumulativeKMInPulses()+"\n");
        strBuff.append("   currentSeasonCumulativeKWInPulses="+getCurrentSeasonCumulativeKWInPulses()+"\n");
        strBuff.append("   powerFactorAtPreviousSeasonMaxKM="+getPowerFactorAtPreviousSeasonMaxKM()+"\n");
        strBuff.append("   powerFactorAtPreviousSeasonMaxKM3="+getPowerFactorAtPreviousSeasonMaxKM3()+"\n");
        strBuff.append("   powerFactorAtPreviousSeasonMaxKW="+getPowerFactorAtPreviousSeasonMaxKW()+"\n");
        strBuff.append("   previousSeasonCoincidentDemandInPulses="+getPreviousSeasonCoincidentDemandInPulses()+"\n");
        strBuff.append("   previousSeasonCumulativeKMInPulses="+getPreviousSeasonCumulativeKMInPulses()+"\n");
        strBuff.append("   previousSeasonCumulativeKWInPulses="+getPreviousSeasonCumulativeKWInPulses()+"\n");
        strBuff.append("   previousSeasonMaximumKM3InPulses="+getPreviousSeasonMaximumKM3InPulses()+"\n");
        strBuff.append("   previousSeasonMaximumKMInPulses="+getPreviousSeasonMaximumKMInPulses()+"\n");
        strBuff.append("   previousSeasonMaximumKWInPulses="+getPreviousSeasonMaximumKWInPulses()+"\n");
        strBuff.append("   previousSeasonTimestampMaximumKM="+getPreviousSeasonTimestampMaximumKM()+"\n");
        strBuff.append("   previousSeasonTimestampMaximumKM3="+getPreviousSeasonTimestampMaximumKM3()+"\n");
        strBuff.append("   previousSeasonTimestampMaximumKW="+getPreviousSeasonTimestampMaximumKW()+"\n");
        strBuff.append("   previousSeasonTotalKM3h="+getPreviousSeasonTotalKM3h()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC8,0,0,0,0,0,0,0,0};
        else
            return new byte[]{(byte)0x4C,0,0,0,0,0,0,0,0};

    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        setPreviousSeasonCumulativeKWInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;
        setPreviousSeasonTimestampMaximumKW(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setPreviousSeasonMaximumKWInPulses(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
        setCurrentSeasonCumulativeKWInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;

        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            setPreviousSeasonCumulativeKMInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;
            setPreviousSeasonTimestampMaximumKM(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
            setPreviousSeasonMaximumKMInPulses(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
            setCurrentSeasonCumulativeKMInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;

            setPowerFactorAtPreviousSeasonMaxKW((int)ParseUtils.getBCD2LongLE(data, offset, 2));offset+=2;
            setPowerFactorAtPreviousSeasonMaxKM((int)ParseUtils.getBCD2LongLE(data, offset, 2));offset+=2;
        }

        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=2.10)) {
            setPreviousSeasonCoincidentDemandInPulses(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
        }

        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)) {
            setPreviousSeasonTimestampMaximumKM3(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
            setPreviousSeasonMaximumKM3InPulses(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
            setPowerFactorAtPreviousSeasonMaxKM3((int)ParseUtils.getBCD2LongLE(data, offset, 2));offset+=2;
            setCoincidentKM3AtPreviousSeasonMaxBillingDemandInPulses(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
            setPreviousSeasonTotalKM3h(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;
        }

    }

    public long getPreviousSeasonCumulativeKWInPulses() {
        return previousSeasonCumulativeKWInPulses;
    }

    public void setPreviousSeasonCumulativeKWInPulses(long previousSeasonCumulativeKWInPulses) {
        this.previousSeasonCumulativeKWInPulses = previousSeasonCumulativeKWInPulses;
    }

    public Date getPreviousSeasonTimestampMaximumKW() {
        return previousSeasonTimestampMaximumKW;
    }

    public void setPreviousSeasonTimestampMaximumKW(Date previousSeasonTimestampMaximumKW) {
        this.previousSeasonTimestampMaximumKW = previousSeasonTimestampMaximumKW;
    }

    public int getPreviousSeasonMaximumKWInPulses() {
        return previousSeasonMaximumKWInPulses;
    }

    public void setPreviousSeasonMaximumKWInPulses(int previousSeasonMaximumKWInPulses) {
        this.previousSeasonMaximumKWInPulses = previousSeasonMaximumKWInPulses;
    }

    public long getCurrentSeasonCumulativeKWInPulses() {
        return currentSeasonCumulativeKWInPulses;
    }

    public void setCurrentSeasonCumulativeKWInPulses(long currentSeasonCumulativeKWInPulses) {
        this.currentSeasonCumulativeKWInPulses = currentSeasonCumulativeKWInPulses;
    }

    public long getPreviousSeasonCumulativeKMInPulses() {
        return previousSeasonCumulativeKMInPulses;
    }

    public void setPreviousSeasonCumulativeKMInPulses(long previousSeasonCumulativeKMInPulses) {
        this.previousSeasonCumulativeKMInPulses = previousSeasonCumulativeKMInPulses;
    }

    public Date getPreviousSeasonTimestampMaximumKM() {
        return previousSeasonTimestampMaximumKM;
    }

    public void setPreviousSeasonTimestampMaximumKM(Date previousSeasonTimestampMaximumKM) {
        this.previousSeasonTimestampMaximumKM = previousSeasonTimestampMaximumKM;
    }

    public int getPreviousSeasonMaximumKMInPulses() {
        return previousSeasonMaximumKMInPulses;
    }

    public void setPreviousSeasonMaximumKMInPulses(int previousSeasonMaximumKMInPulses) {
        this.previousSeasonMaximumKMInPulses = previousSeasonMaximumKMInPulses;
    }

    public long getCurrentSeasonCumulativeKMInPulses() {
        return currentSeasonCumulativeKMInPulses;
    }

    public void setCurrentSeasonCumulativeKMInPulses(long currentSeasonCumulativeKMInPulses) {
        this.currentSeasonCumulativeKMInPulses = currentSeasonCumulativeKMInPulses;
    }

    public int getPowerFactorAtPreviousSeasonMaxKW() {
        return powerFactorAtPreviousSeasonMaxKW;
    }

    public void setPowerFactorAtPreviousSeasonMaxKW(int powerFactorAtPreviousSeasonMaxKW) {
        this.powerFactorAtPreviousSeasonMaxKW = powerFactorAtPreviousSeasonMaxKW;
    }

    public int getPowerFactorAtPreviousSeasonMaxKM() {
        return powerFactorAtPreviousSeasonMaxKM;
    }

    public void setPowerFactorAtPreviousSeasonMaxKM(int powerFactorAtPreviousSeasonMaxKM) {
        this.powerFactorAtPreviousSeasonMaxKM = powerFactorAtPreviousSeasonMaxKM;
    }

    public int getPreviousSeasonCoincidentDemandInPulses() {
        return previousSeasonCoincidentDemandInPulses;
    }

    public void setPreviousSeasonCoincidentDemandInPulses(int previousSeasonCoincidentDemandInPulses) {
        this.previousSeasonCoincidentDemandInPulses = previousSeasonCoincidentDemandInPulses;
    }

    public Date getPreviousSeasonTimestampMaximumKM3() {
        return previousSeasonTimestampMaximumKM3;
    }

    public void setPreviousSeasonTimestampMaximumKM3(Date previousSeasonTimestampMaximumKM3) {
        this.previousSeasonTimestampMaximumKM3 = previousSeasonTimestampMaximumKM3;
    }

    public int getPreviousSeasonMaximumKM3InPulses() {
        return previousSeasonMaximumKM3InPulses;
    }

    public void setPreviousSeasonMaximumKM3InPulses(int previousSeasonMaximumKM3InPulses) {
        this.previousSeasonMaximumKM3InPulses = previousSeasonMaximumKM3InPulses;
    }

    public int getPowerFactorAtPreviousSeasonMaxKM3() {
        return powerFactorAtPreviousSeasonMaxKM3;
    }

    public void setPowerFactorAtPreviousSeasonMaxKM3(int powerFactorAtPreviousSeasonMaxKM3) {
        this.powerFactorAtPreviousSeasonMaxKM3 = powerFactorAtPreviousSeasonMaxKM3;
    }

    public int getCoincidentKM3AtPreviousSeasonMaxBillingDemandInPulses() {
        return coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses;
    }

    public void setCoincidentKM3AtPreviousSeasonMaxBillingDemandInPulses(int coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses) {
        this.coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses = coincidentKM3AtPreviousSeasonMaxBillingDemandInPulses;
    }

    public long getPreviousSeasonTotalKM3h() {
        return previousSeasonTotalKM3h;
    }

    public void setPreviousSeasonTotalKM3h(long previousSeasonTotalKM3h) {
        this.previousSeasonTotalKM3h = previousSeasonTotalKM3h;
    }
}
