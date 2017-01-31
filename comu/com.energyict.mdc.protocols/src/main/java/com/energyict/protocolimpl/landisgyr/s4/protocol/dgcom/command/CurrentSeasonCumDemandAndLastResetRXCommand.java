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
public class CurrentSeasonCumDemandAndLastResetRXCommand extends AbstractCommand {

    public final int NR_OF_RATES = 5;

    private int totalNrOfDemandResets; // Demand reset data->counters->reset counter
    private int numberOfOpticalDemandResets; // Demand reset data->counters->optical reset counter
    private Date timeStampOfLastDemandReset; // Demand reset data->last demand reset->time and date

    private long[] cumKWDemandInPulsesRates = new long[NR_OF_RATES]; // TOU Data -> cumulative Demand
    private long totalKWHInPulsesAtLastDemandReset; // Demand reset data->last demand reset->total kWh
    private int maxKWInPulsesAtLastDemandReset; // Demand reset data->last demand reset->max kW
    private Date timeStampOfMaxKWAtLastDemandReset; // Demand reset data->last demand reset->time & date of max kw

    private long[] cumKMDemandInPulsesRates = new long[NR_OF_RATES];
    private long totalKMHInPulsesAtLastDemandReset;
    private int maxKMInPulsesAtLastDemandReset; // Maximum demand?
    private Date timeStampOfMaxKMAtLastDemandReset; // Maximum demand?

    private int averagePowerFactorAtLastDemandReset;
    private Date timeStampOfWorstPowerFactorAtLastDemandReset;
    private int kWAtWorstPowerFactorInPulsesAtLastDemandReset;
    private int worstPowerFactorAtLastDemandReset;
    private Date timeStampOfWorstPowerFactorSinceLastDemandReset;
    private int kWAtWorstPowerFactorSinceLastDemandReset;
    private int worstPowerFactorSinceLastDemandReset;

    /** Creates a new instance of TemplateCommand */
    public CurrentSeasonCumDemandAndLastResetRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentSeasonCumDemandAndLastResetRXCommand:\n");
        strBuff.append("   totalNrOfDemandResets="+getTotalNrOfDemandResets()+"\n");
        strBuff.append("   numberOfOpticalDemandResets="+getNumberOfOpticalDemandResets()+"\n");
        strBuff.append("   KWAtWorstPowerFactorInPulsesAtLastDemandReset="+getKWAtWorstPowerFactorInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   KWAtWorstPowerFactorSinceLastDemandReset="+getKWAtWorstPowerFactorSinceLastDemandReset()+"\n");
        strBuff.append("   averagePowerFactorAtLastDemandReset="+getAveragePowerFactorAtLastDemandReset()+"\n");
        for (int i=0;i<NR_OF_RATES;i++) {
            strBuff.append("   cumKMDemandInPulsesRates["+i+"]="+getCumKMDemandInPulsesRates()[i]+"\n");
            strBuff.append("   cumKWDemandInPulsesRates["+i+"]="+getCumKWDemandInPulsesRates()[i]+"\n");
        }
        strBuff.append("   maxKMInPulsesAtLastDemandReset="+getMaxKMInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   maxKWInPulsesAtLastDemandReset="+getMaxKWInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   timeStampOfLastDemandReset="+getTimeStampOfLastDemandReset()+"\n");
        strBuff.append("   timeStampOfMaxKMAtLastDemandReset="+getTimeStampOfMaxKMAtLastDemandReset()+"\n");
        strBuff.append("   timeStampOfMaxKWAtLastDemandReset="+getTimeStampOfMaxKWAtLastDemandReset()+"\n");
        strBuff.append("   timeStampOfWorstPowerFactorAtLastDemandReset="+getTimeStampOfWorstPowerFactorAtLastDemandReset()+"\n");
        strBuff.append("   timeStampOfWorstPowerFactorSinceLastDemandReset="+getTimeStampOfWorstPowerFactorSinceLastDemandReset()+"\n");
        strBuff.append("   totalKMHInPulsesAtLastDemandReset="+getTotalKMHInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   totalKWHInPulsesAtLastDemandReset="+getTotalKWHInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   worstPowerFactorAtLastDemandReset="+getWorstPowerFactorAtLastDemandReset()+"\n");
        strBuff.append("   worstPowerFactorSinceLastDemandReset="+getWorstPowerFactorSinceLastDemandReset()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX())
            return new byte[]{(byte)0xAA,0,0,0,0,0,0,0,0};
        else
            throw new IOException("CurrentSeasonCumDemandAndLastResetCommand, only for RX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        for (int i=0;i<NR_OF_RATES;i++) {
            getCumKWDemandInPulsesRates()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setTotalNrOfDemandResets(ProtocolUtils.getIntLE(data,offset,2));offset+=2; //(int)ParseUtils.getBCD2LongLE(data,offset,2));offset+=2;
        setNumberOfOpticalDemandResets((int)ParseUtils.getBCD2LongLE(data,offset,2));offset+=2;
        setTimeStampOfLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setTotalKWHInPulsesAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;

        setTimeStampOfMaxKWAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setMaxKWInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data,offset,2));offset+=2;

        for (int i=0;i<NR_OF_RATES;i++) {
            getCumKMDemandInPulsesRates()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setTotalKMHInPulsesAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6));offset+=6;
        setTimeStampOfMaxKMAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setMaxKMInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data,offset,2));offset+=2;

        setAveragePowerFactorAtLastDemandReset((int)ParseUtils.getBCD2LongLE(data,offset,2));offset+=2;
        setTimeStampOfWorstPowerFactorAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setKWAtWorstPowerFactorInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
        setWorstPowerFactorAtLastDemandReset((int)ParseUtils.getBCD2LongLE(data,offset,2));offset+=2;
        setTimeStampOfWorstPowerFactorSinceLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));offset+=6;
        setKWAtWorstPowerFactorSinceLastDemandReset(ProtocolUtils.getIntLE(data,offset,2));offset+=2;
        setWorstPowerFactorSinceLastDemandReset((int)ParseUtils.getBCD2LongLE(data,offset,2));offset+=2;
    }

    public long[] getCumKWDemandInPulsesRates() {
        return cumKWDemandInPulsesRates;
    }

    public void setCumKWDemandInPulsesRates(long[] cumKWDemandInPulsesRates) {
        this.cumKWDemandInPulsesRates = cumKWDemandInPulsesRates;
    }

    public int getTotalNrOfDemandResets() {
        return totalNrOfDemandResets;
    }

    public void setTotalNrOfDemandResets(int totalNrOfDemandResets) {
        this.totalNrOfDemandResets = totalNrOfDemandResets;
    }

    public int getNumberOfOpticalDemandResets() {
        return numberOfOpticalDemandResets;
    }

    public void setNumberOfOpticalDemandResets(int numberOfOpticalDemandResets) {
        this.numberOfOpticalDemandResets = numberOfOpticalDemandResets;
    }

    public Date getTimeStampOfLastDemandReset() {
        return timeStampOfLastDemandReset;
    }

    public void setTimeStampOfLastDemandReset(Date timeStampOfLastDemandReset) {
        this.timeStampOfLastDemandReset = timeStampOfLastDemandReset;
    }

    public long getTotalKWHInPulsesAtLastDemandReset() {
        return totalKWHInPulsesAtLastDemandReset;
    }

    public void setTotalKWHInPulsesAtLastDemandReset(long totalKWHInPulsesAtLastDemandReset) {
        this.totalKWHInPulsesAtLastDemandReset = totalKWHInPulsesAtLastDemandReset;
    }

    public Date getTimeStampOfMaxKWAtLastDemandReset() {
        return timeStampOfMaxKWAtLastDemandReset;
    }

    public void setTimeStampOfMaxKWAtLastDemandReset(Date timeStampOfMaxKWAtLastDemandReset) {
        this.timeStampOfMaxKWAtLastDemandReset = timeStampOfMaxKWAtLastDemandReset;
    }

    public int getMaxKWInPulsesAtLastDemandReset() {
        return maxKWInPulsesAtLastDemandReset;
    }

    public void setMaxKWInPulsesAtLastDemandReset(int maxKWInPulsesAtLastDemandReset) {
        this.maxKWInPulsesAtLastDemandReset = maxKWInPulsesAtLastDemandReset;
    }

    public long[] getCumKMDemandInPulsesRates() {
        return cumKMDemandInPulsesRates;
    }

    public void setCumKMDemandInPulsesRates(long[] cumKMDemandInPulsesRates) {
        this.cumKMDemandInPulsesRates = cumKMDemandInPulsesRates;
    }

    public long getTotalKMHInPulsesAtLastDemandReset() {
        return totalKMHInPulsesAtLastDemandReset;
    }

    public void setTotalKMHInPulsesAtLastDemandReset(long totalKMHInPulsesAtLastDemandReset) {
        this.totalKMHInPulsesAtLastDemandReset = totalKMHInPulsesAtLastDemandReset;
    }

    public Date getTimeStampOfMaxKMAtLastDemandReset() {
        return timeStampOfMaxKMAtLastDemandReset;
    }

    public void setTimeStampOfMaxKMAtLastDemandReset(Date timeStampOfMaxKMAtLastDemandReset) {
        this.timeStampOfMaxKMAtLastDemandReset = timeStampOfMaxKMAtLastDemandReset;
    }

    public int getMaxKMInPulsesAtLastDemandReset() {
        return maxKMInPulsesAtLastDemandReset;
    }

    public void setMaxKMInPulsesAtLastDemandReset(int maxKMInPulsesAtLastDemandReset) {
        this.maxKMInPulsesAtLastDemandReset = maxKMInPulsesAtLastDemandReset;
    }

    public int getAveragePowerFactorAtLastDemandReset() {
        return averagePowerFactorAtLastDemandReset;
    }

    public void setAveragePowerFactorAtLastDemandReset(int averagePowerFactorAtLastDemandReset) {
        this.averagePowerFactorAtLastDemandReset = averagePowerFactorAtLastDemandReset;
    }

    public Date getTimeStampOfWorstPowerFactorAtLastDemandReset() {
        return timeStampOfWorstPowerFactorAtLastDemandReset;
    }

    public void setTimeStampOfWorstPowerFactorAtLastDemandReset(Date timeStampOfWorstPowerFactorAtLastDemandReset) {
        this.timeStampOfWorstPowerFactorAtLastDemandReset = timeStampOfWorstPowerFactorAtLastDemandReset;
    }

    public int getKWAtWorstPowerFactorInPulsesAtLastDemandReset() {
        return kWAtWorstPowerFactorInPulsesAtLastDemandReset;
    }

    public void setKWAtWorstPowerFactorInPulsesAtLastDemandReset(int kWAtWorstPowerFactorInPulsesAtLastDemandReset) {
        this.kWAtWorstPowerFactorInPulsesAtLastDemandReset = kWAtWorstPowerFactorInPulsesAtLastDemandReset;
    }

    public int getWorstPowerFactorAtLastDemandReset() {
        return worstPowerFactorAtLastDemandReset;
    }

    public void setWorstPowerFactorAtLastDemandReset(int worstPowerFactorAtLastDemandReset) {
        this.worstPowerFactorAtLastDemandReset = worstPowerFactorAtLastDemandReset;
    }

    public Date getTimeStampOfWorstPowerFactorSinceLastDemandReset() {
        return timeStampOfWorstPowerFactorSinceLastDemandReset;
    }

    public void setTimeStampOfWorstPowerFactorSinceLastDemandReset(Date timeStampOfWorstPowerFactorSinceLastDemandReset) {
        this.timeStampOfWorstPowerFactorSinceLastDemandReset = timeStampOfWorstPowerFactorSinceLastDemandReset;
    }

    public int getKWAtWorstPowerFactorSinceLastDemandReset() {
        return kWAtWorstPowerFactorSinceLastDemandReset;
    }

    public void setKWAtWorstPowerFactorSinceLastDemandReset(int kWAtWorstPowerFactorSinceLastDemandReset) {
        this.kWAtWorstPowerFactorSinceLastDemandReset = kWAtWorstPowerFactorSinceLastDemandReset;
    }

    public int getWorstPowerFactorSinceLastDemandReset() {
        return worstPowerFactorSinceLastDemandReset;
    }

    public void setWorstPowerFactorSinceLastDemandReset(int worstPowerFactorSinceLastDemandReset) {
        this.worstPowerFactorSinceLastDemandReset = worstPowerFactorSinceLastDemandReset;
    }
}
