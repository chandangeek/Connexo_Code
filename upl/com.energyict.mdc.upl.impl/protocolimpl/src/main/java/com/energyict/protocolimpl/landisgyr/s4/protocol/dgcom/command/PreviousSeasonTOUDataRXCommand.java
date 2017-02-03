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

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class PreviousSeasonTOUDataRXCommand extends AbstractCommand {
    
    public final int NR_OF_RATES=5;
    public final int NR_OF_MAX_DEMANDS=5;
    
    private long[] cumulativeKWDemandInPulsesRates = new long[NR_OF_RATES];
    private int totalNrOfDemandResets;
    private int numberOfOpticalDemandResets;
    private Date timestampOfLastDemandReset;
    private long totalKWHInPulsesAtLastDemandReset;
    private Date timestampOfMaximumKWAtLastDemandReset;
    private int maximumKWInPulsesAtLastDemandReset;
    
    private long[] cumulativeKMDemandInPulsesRates = new long[NR_OF_RATES];
    private long totalKMHInPulsesAtLastDemandReset;
    private Date timestampOfMaximumKMAtLastDemandReset;
    private int maximumKMInPulsesAtLastDemandReset;
    
    private int averagePowerFactorAtLastDemandReset;
    private Date timestampOfWorstPowerFactorAtLastDemandReset;
    private int kWAtWorstPowerFactorInPulsesAtLastDemandReset;
    private int worstPowerFactorAtLastDemandReset;
    private Date timestampOfWorstPowerFactorSinceLastDemandReset;
    private int kWAtWorstPowerFactorSinceLastDemandReset;
    private int worstPowerFactorSinceLastDemandReset;
    private int averagePowerFactorSinceLastDemandReset;
    private Date[] timestampMaximumKWRate = new Date[NR_OF_RATES];
    private int[] maximumKWInPulsesRate = new int[NR_OF_RATES];
    private Date[] timestampMaximumKMRate = new Date[NR_OF_RATES];
    private int[] maximumKMInPulsesRate = new int[NR_OF_RATES];
    private int[] coincidentDemandInPulsesRate = new int[NR_OF_RATES];
    private int[] powerFactorAtMaximumDemandRate = new int[NR_OF_RATES];
    private long[] kWHInPulsesRate = new long[NR_OF_RATES];
    private long totalKWHInPulses;
    private long[] kMHInPulsesRate = new long[NR_OF_RATES];
    private long totalKMHInPulses;
    private long totalNegativeKWHInPulses;
    
    // >= firmware version 3.00
    private long leadingKVARHInPulses;
    
    // 5 highest max demands
    private Date[] timestampMaximumDemand = new Date[NR_OF_MAX_DEMANDS];
    private int[] maximumDemandInPulses = new int[NR_OF_MAX_DEMANDS];
    private int[] coincidentDemandInPulses = new int[NR_OF_MAX_DEMANDS];
    
    private Date timestampOfMaximumKW;
    private int maximumKWInPulses;
    private int powerFactorAtMaximumKW;
    
    private Date timestampOfMaximumKM;
    private int maximumKMInPulses;
    private int powerFactorAtMaximumKM;
    
    private Date[] timestampMaximumDemandNonBillingMetric = new Date[NR_OF_MAX_DEMANDS];
    private int[] maximumDemandInPulsesNonBillingMetric = new int[NR_OF_MAX_DEMANDS];
    
    // 84 reserved bytes
            
            
    /** Creates a new instance of TemplateCommand */
    public PreviousSeasonTOUDataRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    
    private int getHighestIndex() {
        int index=0;
        for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
            if (timestampMaximumDemand[index].before(timestampMaximumDemand[i]))
                index=i;
        }
        return index;
    }
    
    public int getHighestMaxKW() {
        return getMaximumDemandInPulses()[getHighestIndex()];
    }
    
    public Date getHighestMaxKWTimestamp() {
        return getTimestampMaximumDemand()[getHighestIndex()];
    }
    
    public int getHighestCoincident() {
        return getCoincidentDemandInPulses()[getHighestIndex()];
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousSeasonTOUDataRXCommand:\n");
        for (int i=0;i<getKMHInPulsesRate().length;i++) {
            strBuff.append("       KMHInPulsesRate["+i+"]="+getKMHInPulsesRate()[i]+"\n");
        }
        strBuff.append("   KWAtWorstPowerFactorInPulsesAtLastDemandReset="+getKWAtWorstPowerFactorInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   KWAtWorstPowerFactorSinceLastDemandReset="+getKWAtWorstPowerFactorSinceLastDemandReset()+"\n");
        for (int i=0;i<getKWHInPulsesRate().length;i++) {
            strBuff.append("       KWHInPulsesRate["+i+"]="+getKWHInPulsesRate()[i]+"\n");
        }
        strBuff.append("   averagePowerFactorAtLastDemandReset="+getAveragePowerFactorAtLastDemandReset()+"\n");
        strBuff.append("   averagePowerFactorSinceLastDemandReset="+getAveragePowerFactorSinceLastDemandReset()+"\n");
        for (int i=0;i<getCoincidentDemandInPulses().length;i++) {
            strBuff.append("       coincidentDemandInPulses["+i+"]="+getCoincidentDemandInPulses()[i]+"\n");
        }
        for (int i=0;i<getCoincidentDemandInPulsesRate().length;i++) {
            strBuff.append("       coincidentDemandInPulsesRate["+i+"]="+getCoincidentDemandInPulsesRate()[i]+"\n");
        }
        for (int i=0;i<getCumulativeKMDemandInPulsesRates().length;i++) {
            strBuff.append("       cumulativeKMDemandInPulsesRates["+i+"]="+getCumulativeKMDemandInPulsesRates()[i]+"\n");
        }
        for (int i=0;i<getCumulativeKWDemandInPulsesRates().length;i++) {
            strBuff.append("       cumulativeKWDemandInPulsesRates["+i+"]="+getCumulativeKWDemandInPulsesRates()[i]+"\n");
        }
        strBuff.append("   leadingKVARHInPulses="+getLeadingKVARHInPulses()+"\n");
        for (int i=0;i<getMaximumDemandInPulses().length;i++) {
            strBuff.append("       maximumDemandInPulses["+i+"]="+getMaximumDemandInPulses()[i]+"\n");
        }
        for (int i=0;i<getMaximumDemandInPulsesNonBillingMetric().length;i++) {
            strBuff.append("       maximumDemandInPulsesNonBillingMetric["+i+"]="+getMaximumDemandInPulsesNonBillingMetric()[i]+"\n");
        }
        strBuff.append("   maximumKMInPulses="+getMaximumKMInPulses()+"\n");
        strBuff.append("   maximumKMInPulsesAtLastDemandReset="+getMaximumKMInPulsesAtLastDemandReset()+"\n");
        for (int i=0;i<getMaximumKMInPulsesRate().length;i++) {
            strBuff.append("       maximumKMInPulsesRate["+i+"]="+getMaximumKMInPulsesRate()[i]+"\n");
        }
        strBuff.append("   maximumKWInPulses="+getMaximumKWInPulses()+"\n");
        strBuff.append("   maximumKWInPulsesAtLastDemandReset="+getMaximumKWInPulsesAtLastDemandReset()+"\n");
        for (int i=0;i<getMaximumKWInPulsesRate().length;i++) {
            strBuff.append("       maximumKWInPulsesRate["+i+"]="+getMaximumKWInPulsesRate()[i]+"\n");
        }
        strBuff.append("   numberOfOpticalDemandResets="+getNumberOfOpticalDemandResets()+"\n");
        for (int i=0;i<getPowerFactorAtMaximumDemandRate().length;i++) {
            strBuff.append("       powerFactorAtMaximumDemandRate["+i+"]="+getPowerFactorAtMaximumDemandRate()[i]+"\n");
        }
        strBuff.append("   powerFactorAtMaximumKM="+getPowerFactorAtMaximumKM()+"\n");
        strBuff.append("   powerFactorAtMaximumKW="+getPowerFactorAtMaximumKW()+"\n");
        for (int i=0;i<getTimestampMaximumDemand().length;i++) {
            strBuff.append("       timestampMaximumDemand["+i+"]="+getTimestampMaximumDemand()[i]+"\n");
        }
        for (int i=0;i<getTimestampMaximumDemandNonBillingMetric().length;i++) {
            strBuff.append("       timestampMaximumDemandNonBillingMetric["+i+"]="+getTimestampMaximumDemandNonBillingMetric()[i]+"\n");
        }
        for (int i=0;i<getTimestampMaximumKMRate().length;i++) {
            strBuff.append("       timestampMaximumKMRate["+i+"]="+getTimestampMaximumKMRate()[i]+"\n");
        }
        for (int i=0;i<getTimestampMaximumKWRate().length;i++) {
            strBuff.append("       timestampMaximumKWRate["+i+"]="+getTimestampMaximumKWRate()[i]+"\n");
        }
        strBuff.append("   timestampOfLastDemandReset="+getTimestampOfLastDemandReset()+"\n");
        strBuff.append("   timestampOfMaximumKM="+getTimestampOfMaximumKM()+"\n");
        strBuff.append("   timestampOfMaximumKMAtLastDemandReset="+getTimestampOfMaximumKMAtLastDemandReset()+"\n");
        strBuff.append("   timestampOfMaximumKW="+getTimestampOfMaximumKW()+"\n");
        strBuff.append("   timestampOfMaximumKWAtLastDemandReset="+getTimestampOfMaximumKWAtLastDemandReset()+"\n");
        strBuff.append("   timestampOfWorstPowerFactorAtLastDemandReset="+getTimestampOfWorstPowerFactorAtLastDemandReset()+"\n");
        strBuff.append("   timestampOfWorstPowerFactorSinceLastDemandReset="+getTimestampOfWorstPowerFactorSinceLastDemandReset()+"\n");
        strBuff.append("   totalKMHInPulses="+getTotalKMHInPulses()+"\n");
        strBuff.append("   totalKMHInPulsesAtLastDemandReset="+getTotalKMHInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   totalKWHInPulses="+getTotalKWHInPulses()+"\n");
        strBuff.append("   totalKWHInPulsesAtLastDemandReset="+getTotalKWHInPulsesAtLastDemandReset()+"\n");
        strBuff.append("   totalNegativeKWHInPulses="+getTotalNegativeKWHInPulses()+"\n");
        strBuff.append("   totalNrOfDemandResets="+getTotalNrOfDemandResets()+"\n");
        strBuff.append("   worstPowerFactorAtLastDemandReset="+getWorstPowerFactorAtLastDemandReset()+"\n");
        strBuff.append("   worstPowerFactorSinceLastDemandReset="+getWorstPowerFactorSinceLastDemandReset()+"\n");
        return strBuff.toString();
    }

    
    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()<3.00)
                setSize(300);
            else if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
                setSize(500);
            return new byte[]{(byte)0xAB,0,0,0,0,0,0,0,0};
        }
        else 
            throw new IOException("PreviousSeasonTOUDataRXCommand, only for RX meters!");
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;

        for (int i=0;i<NR_OF_RATES;i++) {
            getCumulativeKWDemandInPulsesRates()[i] = ParseUtils.getBCD2LongLE(data, offset, 6); 
            offset+=6;
        }
        
        setTotalNrOfDemandResets((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2; 
        setNumberOfOpticalDemandResets((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2; 
        setTimestampOfLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
        setTotalKWHInPulsesAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
        setTimestampOfMaximumKWAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
        setMaximumKWInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2; 

        for (int i=0;i<NR_OF_RATES;i++) {
            getCumulativeKMDemandInPulsesRates()[i] = ParseUtils.getBCD2LongLE(data, offset, 6); 
            offset+=6;
        }
        setTotalKMHInPulsesAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
        setTimestampOfMaximumKMAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
        setMaximumKMInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2;

        setAveragePowerFactorAtLastDemandReset((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2; 
        setTimestampOfWorstPowerFactorAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
        setKWAtWorstPowerFactorInPulsesAtLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2; 
        setWorstPowerFactorAtLastDemandReset((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;
        setTimestampOfWorstPowerFactorSinceLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
        setKWAtWorstPowerFactorSinceLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2; 
        setWorstPowerFactorSinceLastDemandReset((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;
        setAveragePowerFactorSinceLastDemandReset((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;
        for (int i=0;i<NR_OF_RATES;i++) {
            getTimestampMaximumKWRate()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone());
            offset+=6;
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getMaximumKWInPulsesRate()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
            offset+=2; 
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getTimestampMaximumKMRate()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone());
            offset+=6;
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getMaximumKMInPulsesRate()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
            offset+=2; 
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getCoincidentDemandInPulsesRate()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
            offset+=2; 
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getPowerFactorAtMaximumDemandRate()[i] = (int)ParseUtils.getBCD2LongLE(data, offset, 2);
            offset+=2; 
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getKWHInPulsesRate()[i] = ParseUtils.getBCD2LongLE(data, offset, 6); 
            offset+=6;
        }
        setTotalKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
        for (int i=0;i<NR_OF_RATES;i++) {
            getKMHInPulsesRate()[i] = ParseUtils.getBCD2LongLE(data, offset, 6); 
            offset+=6;
        }
        setTotalKMHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
        setTotalNegativeKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;

        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00) {
            // >= firmware version 3.00
            setLeadingKVARHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;

            // 5 highest max demands
            for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
                getTimestampMaximumDemand()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone());
                offset+=6;
                getMaximumDemandInPulses()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
                offset+=2; 
            }            
            for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
                getCoincidentDemandInPulses()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
                offset+=2; 
            }

            setTimestampOfMaximumKW(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone())); offset+=6;
            setMaximumKWInPulses(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2; 
            setPowerFactorAtMaximumKW((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;

            timestampOfMaximumKM = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()); offset+=6;
            int maximumKMInPulses = ProtocolUtils.getIntLE(data, offset, 2); offset+=2; 
            int powerFactorAtMaximumKM = (int)ParseUtils.getBCD2LongLE(data, offset, 2); offset+=2;
            
            for (int i=0;i<NR_OF_MAX_DEMANDS;i++) {
                getTimestampMaximumDemandNonBillingMetric()[i] = Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone());
                offset+=6;
                getMaximumDemandInPulsesNonBillingMetric()[i] = ProtocolUtils.getIntLE(data, offset, 2); 
                offset+=2; 
            }            
        }
    }

    public long[] getCumulativeKWDemandInPulsesRates() {
        return cumulativeKWDemandInPulsesRates;
    }

    public void setCumulativeKWDemandInPulsesRates(long[] cumulativeKWDemandInPulsesRates) {
        this.cumulativeKWDemandInPulsesRates = cumulativeKWDemandInPulsesRates;
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

    public Date getTimestampOfLastDemandReset() {
        return timestampOfLastDemandReset;
    }

    public void setTimestampOfLastDemandReset(Date timestampOfLastDemandReset) {
        this.timestampOfLastDemandReset = timestampOfLastDemandReset;
    }

    public long getTotalKWHInPulsesAtLastDemandReset() {
        return totalKWHInPulsesAtLastDemandReset;
    }

    public void setTotalKWHInPulsesAtLastDemandReset(long totalKWHInPulsesAtLastDemandReset) {
        this.totalKWHInPulsesAtLastDemandReset = totalKWHInPulsesAtLastDemandReset;
    }

    public Date getTimestampOfMaximumKWAtLastDemandReset() {
        return timestampOfMaximumKWAtLastDemandReset;
    }

    public void setTimestampOfMaximumKWAtLastDemandReset(Date timestampOfMaximumKWAtLastDemandReset) {
        this.timestampOfMaximumKWAtLastDemandReset = timestampOfMaximumKWAtLastDemandReset;
    }

    public int getMaximumKWInPulsesAtLastDemandReset() {
        return maximumKWInPulsesAtLastDemandReset;
    }

    public void setMaximumKWInPulsesAtLastDemandReset(int maximumKWInPulsesAtLastDemandReset) {
        this.maximumKWInPulsesAtLastDemandReset = maximumKWInPulsesAtLastDemandReset;
    }

    public long[] getCumulativeKMDemandInPulsesRates() {
        return cumulativeKMDemandInPulsesRates;
    }

    public void setCumulativeKMDemandInPulsesRates(long[] cumulativeKMDemandInPulsesRates) {
        this.cumulativeKMDemandInPulsesRates = cumulativeKMDemandInPulsesRates;
    }

    public long getTotalKMHInPulsesAtLastDemandReset() {
        return totalKMHInPulsesAtLastDemandReset;
    }

    public void setTotalKMHInPulsesAtLastDemandReset(long totalKMHInPulsesAtLastDemandReset) {
        this.totalKMHInPulsesAtLastDemandReset = totalKMHInPulsesAtLastDemandReset;
    }

    public Date getTimestampOfMaximumKMAtLastDemandReset() {
        return timestampOfMaximumKMAtLastDemandReset;
    }

    public void setTimestampOfMaximumKMAtLastDemandReset(Date timestampOfMaximumKMAtLastDemandReset) {
        this.timestampOfMaximumKMAtLastDemandReset = timestampOfMaximumKMAtLastDemandReset;
    }

    public int getMaximumKMInPulsesAtLastDemandReset() {
        return maximumKMInPulsesAtLastDemandReset;
    }

    public void setMaximumKMInPulsesAtLastDemandReset(int maximumKMInPulsesAtLastDemandReset) {
        this.maximumKMInPulsesAtLastDemandReset = maximumKMInPulsesAtLastDemandReset;
    }

    public int getAveragePowerFactorAtLastDemandReset() {
        return averagePowerFactorAtLastDemandReset;
    }

    public void setAveragePowerFactorAtLastDemandReset(int averagePowerFactorAtLastDemandReset) {
        this.averagePowerFactorAtLastDemandReset = averagePowerFactorAtLastDemandReset;
    }

    public Date getTimestampOfWorstPowerFactorAtLastDemandReset() {
        return timestampOfWorstPowerFactorAtLastDemandReset;
    }

    public void setTimestampOfWorstPowerFactorAtLastDemandReset(Date timestampOfWorstPowerFactorAtLastDemandReset) {
        this.timestampOfWorstPowerFactorAtLastDemandReset = timestampOfWorstPowerFactorAtLastDemandReset;
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

    public Date getTimestampOfWorstPowerFactorSinceLastDemandReset() {
        return timestampOfWorstPowerFactorSinceLastDemandReset;
    }

    public void setTimestampOfWorstPowerFactorSinceLastDemandReset(Date timestampOfWorstPowerFactorSinceLastDemandReset) {
        this.timestampOfWorstPowerFactorSinceLastDemandReset = timestampOfWorstPowerFactorSinceLastDemandReset;
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

    public int getAveragePowerFactorSinceLastDemandReset() {
        return averagePowerFactorSinceLastDemandReset;
    }

    public void setAveragePowerFactorSinceLastDemandReset(int averagePowerFactorSinceLastDemandReset) {
        this.averagePowerFactorSinceLastDemandReset = averagePowerFactorSinceLastDemandReset;
    }

    public Date[] getTimestampMaximumKWRate() {
        return timestampMaximumKWRate;
    }

    public void setTimestampMaximumKWRate(Date[] timestampMaximumKWRate) {
        this.timestampMaximumKWRate = timestampMaximumKWRate;
    }

    public int[] getMaximumKWInPulsesRate() {
        return maximumKWInPulsesRate;
    }

    public void setMaximumKWInPulsesRate(int[] maximumKWInPulsesRate) {
        this.maximumKWInPulsesRate = maximumKWInPulsesRate;
    }

    public Date[] getTimestampMaximumKMRate() {
        return timestampMaximumKMRate;
    }

    public void setTimestampMaximumKMRate(Date[] timestampMaximumKMRate) {
        this.timestampMaximumKMRate = timestampMaximumKMRate;
    }

    public int[] getMaximumKMInPulsesRate() {
        return maximumKMInPulsesRate;
    }

    public void setMaximumKMInPulsesRate(int[] maximumKMInPulsesRate) {
        this.maximumKMInPulsesRate = maximumKMInPulsesRate;
    }

    public int[] getCoincidentDemandInPulsesRate() {
        return coincidentDemandInPulsesRate;
    }

    public void setCoincidentDemandInPulsesRate(int[] coincidentDemandInPulsesRate) {
        this.coincidentDemandInPulsesRate = coincidentDemandInPulsesRate;
    }

    public int[] getPowerFactorAtMaximumDemandRate() {
        return powerFactorAtMaximumDemandRate;
    }

    public void setPowerFactorAtMaximumDemandRate(int[] powerFactorAtMaximumDemandRate) {
        this.powerFactorAtMaximumDemandRate = powerFactorAtMaximumDemandRate;
    }

    public long[] getKWHInPulsesRate() {
        return kWHInPulsesRate;
    }

    public void setKWHInPulsesRate(long[] kWHInPulsesRate) {
        this.kWHInPulsesRate = kWHInPulsesRate;
    }

    public long getTotalKWHInPulses() {
        return totalKWHInPulses;
    }

    public void setTotalKWHInPulses(long totalKWHInPulses) {
        this.totalKWHInPulses = totalKWHInPulses;
    }

    public long[] getKMHInPulsesRate() {
        return kMHInPulsesRate;
    }

    public void setKMHInPulsesRate(long[] kMHInPulsesRate) {
        this.kMHInPulsesRate = kMHInPulsesRate;
    }

    public long getTotalKMHInPulses() {
        return totalKMHInPulses;
    }

    public void setTotalKMHInPulses(long totalKMHInPulses) {
        this.totalKMHInPulses = totalKMHInPulses;
    }

    public long getTotalNegativeKWHInPulses() {
        return totalNegativeKWHInPulses;
    }

    public void setTotalNegativeKWHInPulses(long totalNegativeKWHInPulses) {
        this.totalNegativeKWHInPulses = totalNegativeKWHInPulses;
    }

    public long getLeadingKVARHInPulses() {
        return leadingKVARHInPulses;
    }

    public void setLeadingKVARHInPulses(long leadingKVARHInPulses) {
        this.leadingKVARHInPulses = leadingKVARHInPulses;
    }

    public Date[] getTimestampMaximumDemand() {
        return timestampMaximumDemand;
    }

    public void setTimestampMaximumDemand(Date[] timestampMaximumDemand) {
        this.timestampMaximumDemand = timestampMaximumDemand;
    }

    public int[] getMaximumDemandInPulses() {
        return maximumDemandInPulses;
    }

    public void setMaximumDemandInPulses(int[] maximumDemandInPulses) {
        this.maximumDemandInPulses = maximumDemandInPulses;
    }

    public int[] getCoincidentDemandInPulses() {
        return coincidentDemandInPulses;
    }

    public void setCoincidentDemandInPulses(int[] coincidentDemandInPulses) {
        this.coincidentDemandInPulses = coincidentDemandInPulses;
    }

    public Date getTimestampOfMaximumKW() {
        return timestampOfMaximumKW;
    }

    public void setTimestampOfMaximumKW(Date timestampOfMaximumKW) {
        this.timestampOfMaximumKW = timestampOfMaximumKW;
    }

    public int getMaximumKWInPulses() {
        return maximumKWInPulses;
    }

    public void setMaximumKWInPulses(int maximumKWInPulses) {
        this.maximumKWInPulses = maximumKWInPulses;
    }

    public int getPowerFactorAtMaximumKW() {
        return powerFactorAtMaximumKW;
    }

    public void setPowerFactorAtMaximumKW(int powerFactorAtMaximumKW) {
        this.powerFactorAtMaximumKW = powerFactorAtMaximumKW;
    }

    public Date getTimestampOfMaximumKM() {
        return timestampOfMaximumKM;
    }

    public void setTimestampOfMaximumKM(Date timestampOfMaximumKM) {
        this.timestampOfMaximumKM = timestampOfMaximumKM;
    }

    public int getMaximumKMInPulses() {
        return maximumKMInPulses;
    }

    public void setMaximumKMInPulses(int maximumKMInPulses) {
        this.maximumKMInPulses = maximumKMInPulses;
    }

    public int getPowerFactorAtMaximumKM() {
        return powerFactorAtMaximumKM;
    }

    public void setPowerFactorAtMaximumKM(int powerFactorAtMaximumKM) {
        this.powerFactorAtMaximumKM = powerFactorAtMaximumKM;
    }

    public Date[] getTimestampMaximumDemandNonBillingMetric() {
        return timestampMaximumDemandNonBillingMetric;
    }

    public void setTimestampMaximumDemandNonBillingMetric(Date[] timestampMaximumDemandNonBillingMetric) {
        this.timestampMaximumDemandNonBillingMetric = timestampMaximumDemandNonBillingMetric;
    }

    public int[] getMaximumDemandInPulsesNonBillingMetric() {
        return maximumDemandInPulsesNonBillingMetric;
    }

    public void setMaximumDemandInPulsesNonBillingMetric(int[] maximumDemandInPulsesNonBillingMetric) {
        this.maximumDemandInPulsesNonBillingMetric = maximumDemandInPulsesNonBillingMetric;
    }
}
