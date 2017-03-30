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

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class HighestMaximumDemandsCommand extends AbstractCommand {

    public final int NR_OF_MAX_DEMANDS=5;

    // Energy & Demand

    // DX, RX
    private Date[] maxDemandTimestamps = new Date[NR_OF_MAX_DEMANDS];
    private int[] maxDemands = new int[NR_OF_MAX_DEMANDS];

    // RX
    private int[] maxDemandCoincidents = new int[NR_OF_MAX_DEMANDS]; // coincident max demand 1..5
    private Date maxkWTimestamp;
    private int maxkWInPulses;
    private int powerFactorAtMaxkW;
    private Date maxkMTimestamp;
    private int maxkMInPulses;
    private int powerFactorAtMaxkM;

    // RX FW >=3.00
    private Date[] nonBillingMetricMaxDemandTimestamps = new Date[NR_OF_MAX_DEMANDS];
    private int[] nonBillingMetricMaxDemands = new int[NR_OF_MAX_DEMANDS];


    /** Creates a new instance of TemplateCommand */
    public HighestMaximumDemandsCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }


    private int getHighestIndex() {
        int index=0;
        for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
            if (maxDemandTimestamps[index].before(maxDemandTimestamps[i]))
                index=i;
        }
        return index;
    }

    public int getHighestMaxKW() {
        return getMaxDemands()[getHighestIndex()];
    }

    public Date getHighestMaxKWTimestamp() {
        return getMaxDemandTimestamps()[getHighestIndex()];
    }

    public int getHighestCoincident() {
        return getMaxDemandCoincidents()[getHighestIndex()];
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HighestMaximumDemandsCommand:\n");
        for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
            strBuff.append("   maxDemand["+i+"]="+getMaxDemands()[i]+"\n");
            strBuff.append("   maxDemandTimestamps["+i+"]="+getMaxDemandTimestamps()[i]+"\n");
        }
        for (int i=0;i<NR_OF_MAX_DEMANDS;i++)
            strBuff.append("   maxDemandCoincidents "+i+"="+getMaxDemandCoincidents()[i]+"\n");
        strBuff.append("   maxkMInPulses="+getMaxkMInPulses()+"\n");
        strBuff.append("   maxkMTimestamp="+getMaxkMTimestamp()+"\n");
        strBuff.append("   maxkWInPulses="+getMaxkWInPulses()+"\n");
        strBuff.append("   maxkWTimestamp="+getMaxkWTimestamp()+"\n");
        for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
            strBuff.append("   nonBillingMetricMaxDemands["+i+"]="+getNonBillingMetricMaxDemands()[i]+"\n");
            strBuff.append("   nonBillingMetricMaxDemandTimestamps["+i+"]="+getNonBillingMetricMaxDemandTimestamps()[i]+"\n");
        }
        strBuff.append("   powerFactorAtMaxkM="+getPowerFactorAtMaxkM()+"\n");
        strBuff.append("   powerFactorAtMaxkW="+getPowerFactorAtMaxkW()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC1,0,0,0,0,0,0,0,0};
        else
            return new byte[]{(byte)0x84,0,0,0,0,0,0,0,0};

    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        getMaxDemandTimestamps()[0] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone()); offset+=6;
        getMaxDemands()[0] = ProtocolUtils.getIntLE(data,offset, 2);offset+=2;
        for (int i=1;i<NR_OF_MAX_DEMANDS;i++) {
            getMaxDemandTimestamps()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone()); offset+=6;
            getMaxDemands()[i] = ProtocolUtils.getIntLE(data,offset, 2);offset+=2;
        }
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
                getMaxDemandCoincidents()[i] = ProtocolUtils.getIntLE(data,offset, 2);offset+=2;
            }
            setMaxkWTimestamp(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone())); offset+=6;
            setMaxkWInPulses(ProtocolUtils.getIntLE(data,offset, 2));offset+=2;
            setPowerFactorAtMaxkW((int)ParseUtils.getBCD2LongLE(data,offset, 2));offset+=2;
            setMaxkMTimestamp(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone())); offset+=6;
            setMaxkMInPulses(ProtocolUtils.getIntLE(data,offset, 2));offset+=2;
            setPowerFactorAtMaxkM((int)ParseUtils.getBCD2LongLE(data,offset, 2));offset+=2;
        }

        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)) {
            for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
                getNonBillingMetricMaxDemandTimestamps()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone()); offset+=6;
                getNonBillingMetricMaxDemands()[i] = ProtocolUtils.getIntLE(data,offset, 2);offset+=2;
            }
        }
    }


    public int[] getMaxDemandCoincidents() {
        return maxDemandCoincidents;
    }

    public void setMaxDemandCoincidents(int[] maxDemandCoincidents) {
        this.maxDemandCoincidents = maxDemandCoincidents;
    }

    public Date getMaxkWTimestamp() {
        return maxkWTimestamp;
    }

    public void setMaxkWTimestamp(Date maxkWTimestamp) {
        this.maxkWTimestamp = maxkWTimestamp;
    }

    public int getMaxkWInPulses() {
        return maxkWInPulses;
    }

    public void setMaxkWInPulses(int maxkWInPulses) {
        this.maxkWInPulses = maxkWInPulses;
    }

    public int getPowerFactorAtMaxkW() {
        return powerFactorAtMaxkW;
    }

    public void setPowerFactorAtMaxkW(int powerFactorAtMaxkW) {
        this.powerFactorAtMaxkW = powerFactorAtMaxkW;
    }

    public Date getMaxkMTimestamp() {
        return maxkMTimestamp;
    }

    public void setMaxkMTimestamp(Date maxkMTimestamp) {
        this.maxkMTimestamp = maxkMTimestamp;
    }

    public int getMaxkMInPulses() {
        return maxkMInPulses;
    }

    public void setMaxkMInPulses(int maxkMInPulses) {
        this.maxkMInPulses = maxkMInPulses;
    }

    public int getPowerFactorAtMaxkM() {
        return powerFactorAtMaxkM;
    }

    public void setPowerFactorAtMaxkM(int powerFactorAtMaxkM) {
        this.powerFactorAtMaxkM = powerFactorAtMaxkM;
    }

    public Date[] getMaxDemandTimestamps() {
        return maxDemandTimestamps;
    }

    public void setMaxDemandTimestamps(Date[] maxDemandTimestamps) {
        this.maxDemandTimestamps = maxDemandTimestamps;
    }

    public int[] getMaxDemands() {
        return maxDemands;
    }

    public void setMaxDemands(int[] maxDemands) {
        this.maxDemands = maxDemands;
    }

    public Date[] getNonBillingMetricMaxDemandTimestamps() {
        return nonBillingMetricMaxDemandTimestamps;
    }

    public void setNonBillingMetricMaxDemandTimestamps(Date[] nonBillingMetricMaxDemandTimestamps) {
        this.nonBillingMetricMaxDemandTimestamps = nonBillingMetricMaxDemandTimestamps;
    }

    public int[] getNonBillingMetricMaxDemands() {
        return nonBillingMetricMaxDemands;
    }

    public void setNonBillingMetricMaxDemands(int[] nonBillingMetricMaxDemands) {
        this.nonBillingMetricMaxDemands = nonBillingMetricMaxDemands;
    }

}
