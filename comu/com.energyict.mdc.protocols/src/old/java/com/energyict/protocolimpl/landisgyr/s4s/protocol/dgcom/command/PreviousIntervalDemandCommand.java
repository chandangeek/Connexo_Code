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


/**
 *
 * @author Koen
 */
public class PreviousIntervalDemandCommand extends AbstractCommand {

    private int demandInLastCompleteIntervalInPulses;
    private int demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses;
    private int powerFactorInLastCompleteInterval;
    private int averagePowerFactorSinceLastDemandReset;
    private int demandInLastCompleteIntervalForThirdMetricInPulses;

    /** Creates a new instance of TemplateCommand */
    public PreviousIntervalDemandCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousIntervalDemandCommand:\n");
        strBuff.append("   averagePowerFactorSinceLastDemandReset="+getAveragePowerFactorSinceLastDemandReset()+"\n");
        strBuff.append("   demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses="+getDemandInLastCompleteIntervalForAdditionalSelectedMetricInPulses()+"\n");
        strBuff.append("   demandInLastCompleteIntervalInPulses="+getDemandInLastCompleteIntervalInPulses()+"\n");
        strBuff.append("   powerFactorInLastCompleteInterval="+getPowerFactorInLastCompleteInterval()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC6,0,0,0,0,0,0,0,0};
        else
            return new byte[]{(byte)0x14,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setDemandInLastCompleteIntervalInPulses(ProtocolUtils.getIntLE(data, offset, 2));offset+=2;
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            setDemandInLastCompleteIntervalForAdditionalSelectedMetricInPulses(ProtocolUtils.getIntLE(data, offset, 2));offset+=2;
            setPowerFactorInLastCompleteInterval((int)ParseUtils.getBCD2LongLE(data, offset, 2));offset+=2;
            setAveragePowerFactorSinceLastDemandReset((int)ParseUtils.getBCD2LongLE(data, offset, 2));offset+=2;
        }
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            setDemandInLastCompleteIntervalForThirdMetricInPulses(ProtocolUtils.getIntLE(data, offset, 2));offset+=2;
    }

    public int getDemandInLastCompleteIntervalInPulses() {
        return demandInLastCompleteIntervalInPulses;
    }

    public void setDemandInLastCompleteIntervalInPulses(int demandInLastCompleteIntervalInPulses) {
        this.demandInLastCompleteIntervalInPulses = demandInLastCompleteIntervalInPulses;
    }

    public int getDemandInLastCompleteIntervalForAdditionalSelectedMetricInPulses() {
        return demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses;
    }

    public void setDemandInLastCompleteIntervalForAdditionalSelectedMetricInPulses(int demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses) {
        this.demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses = demandInLastCompleteIntervalForAdditionalSelectedMetricInPulses;
    }

    public int getPowerFactorInLastCompleteInterval() {
        return powerFactorInLastCompleteInterval;
    }

    public void setPowerFactorInLastCompleteInterval(int powerFactorInLastCompleteInterval) {
        this.powerFactorInLastCompleteInterval = powerFactorInLastCompleteInterval;
    }

    public int getAveragePowerFactorSinceLastDemandReset() {
        return averagePowerFactorSinceLastDemandReset;
    }

    public void setAveragePowerFactorSinceLastDemandReset(int averagePowerFactorSinceLastDemandReset) {
        this.averagePowerFactorSinceLastDemandReset = averagePowerFactorSinceLastDemandReset;
    }

    public int getDemandInLastCompleteIntervalForThirdMetricInPulses() {
        return demandInLastCompleteIntervalForThirdMetricInPulses;
    }

    public void setDemandInLastCompleteIntervalForThirdMetricInPulses(int demandInLastCompleteIntervalForThirdMetricInPulses) {
        this.demandInLastCompleteIntervalForThirdMetricInPulses = demandInLastCompleteIntervalForThirdMetricInPulses;
    }
}
