/*
 * DemandRegister.java
 *
 * Created on 13 september 2006, 15:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.protocol.Utils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class DemandRegister {
    
    private BigDecimal instantaneousDemand;
    private BigDecimal totalPreviousIntervalDemand;
    private BigDecimal totalMaximumDemand;
    private Date totalMaximumDemandDate;
    private BigDecimal[] maximumDemandRates;
    private Date[] maximumDemandRateDates;
    private BigDecimal[] highestPeaks;
    private Date[] highestPeakDates;
    private BigDecimal coincidentRegisterValue;
    private BigDecimal totalCumulativeDemand;
    private BigDecimal[] cumulativeDemandRates;
    private BigDecimal totalContinuousCumulativeDemand;
    private BigDecimal[] continuousCumulativeDemandRates;
    
    /** Creates a new instance of DemandRegister */
    public DemandRegister(byte[] data, int offset, TimeZone timeZone) throws IOException {
        
        
        setInstantaneousDemand(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setTotalPreviousIntervalDemand(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setTotalMaximumDemand(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setTotalMaximumDemandDate(Utils.buildDate(data,offset, timeZone));
        offset+=5;
        maximumDemandRates = new BigDecimal[RegisterFactory.MAX_NR_OF_RATES];
        setMaximumDemandRateDates(new Date[RegisterFactory.MAX_NR_OF_RATES]);
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            getMaximumDemandRates()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
            offset+=4;
            getMaximumDemandRateDates()[i] = Utils.buildDate(data,offset, timeZone);
            offset+=5;
        }        
        highestPeaks= new BigDecimal[RegisterFactory.MAX_NR_OF_PEAKS];
        setHighestPeakDates(new Date[RegisterFactory.MAX_NR_OF_PEAKS]);
        for (int i=0;i<RegisterFactory.MAX_NR_OF_PEAKS;i++) {
            getHighestPeaks()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
            offset+=4;
            getHighestPeakDates()[i] = Utils.buildDate(data,offset, timeZone);
            offset+=5;
        }        
        setCoincidentRegisterValue(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        setTotalCumulativeDemand(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        cumulativeDemandRates = new BigDecimal[RegisterFactory.MAX_NR_OF_RATES];
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            getCumulativeDemandRates()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
            offset+=4;
        }         
        setTotalContinuousCumulativeDemand(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
        continuousCumulativeDemandRates = new BigDecimal[RegisterFactory.MAX_NR_OF_RATES];
        for (int i=0;i<RegisterFactory.MAX_NR_OF_RATES;i++) {
            getContinuousCumulativeDemandRates()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
            offset+=4;
        }         
         
    }
    

    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegister:\n");
        strBuff.append("   instantaneousDemand="+getInstantaneousDemand()+"\n");
        strBuff.append("   totalPreviousIntervalDemand="+getTotalPreviousIntervalDemand()+"\n");
        strBuff.append("   totalMaximumDemand="+getTotalMaximumDemand()+", totalMaximumDemandDate="+getTotalMaximumDemandDate()+"\n");
        for (int i=0;i<getMaximumDemandRates().length;i++) {
            strBuff.append("       maximumDemandRates["+i+"]="+getMaximumDemandRates()[i]+", maximumDemandRateDates["+i+"]="+getMaximumDemandRateDates()[i]+"\n");
        }
        for (int i=0;i<getHighestPeaks().length;i++) {
            strBuff.append("       highestPeaks["+i+"]="+getHighestPeaks()[i]+", highestPeakDates["+i+"]="+getHighestPeakDates()[i]+"\n");
        }
        strBuff.append("   coincidentRegisterValue="+getCoincidentRegisterValue()+"\n");
        strBuff.append("   totalCumulativeDemand="+getTotalCumulativeDemand()+"\n");
        for (int i=0;i<getCumulativeDemandRates().length;i++) {
            strBuff.append("       cumulativeDemandRates["+i+"]="+getCumulativeDemandRates()[i]+"\n");
        }
        strBuff.append("   totalContinuousCumulativeDemand="+getTotalContinuousCumulativeDemand()+"\n");
        for (int i=0;i<getContinuousCumulativeDemandRates().length;i++) {
            strBuff.append("       continuousCumulativeDemandRates["+i+"]="+getContinuousCumulativeDemandRates()[i]+"\n");
        }
        return strBuff.toString();
    }     
    
    static public int size() {
        return 142;
    }    

    public BigDecimal getInstantaneousDemand() {
        return instantaneousDemand;
    }

    public void setInstantaneousDemand(BigDecimal instantaneousDemand) {
        this.instantaneousDemand = instantaneousDemand;
    }

    public BigDecimal getTotalPreviousIntervalDemand() {
        return totalPreviousIntervalDemand;
    }

    public void setTotalPreviousIntervalDemand(BigDecimal totalPreviousIntervalDemand) {
        this.totalPreviousIntervalDemand = totalPreviousIntervalDemand;
    }

    public BigDecimal getTotalMaximumDemand() {
        return totalMaximumDemand;
    }

    public void setTotalMaximumDemand(BigDecimal totalMaximumDemand) {
        this.totalMaximumDemand = totalMaximumDemand;
    }

    public BigDecimal[] getMaximumDemandRates() {
        return maximumDemandRates;
    }

    public void setMaximumDemandRates(BigDecimal[] maximumDemandRates) {
        this.maximumDemandRates = maximumDemandRates;
    }

    public BigDecimal[] getHighestPeaks() {
        return highestPeaks;
    }

    public void setHighestPeaks(BigDecimal[] highestPeaks) {
        this.highestPeaks = highestPeaks;
    }

    public BigDecimal getCoincidentRegisterValue() {
        return coincidentRegisterValue;
    }

    public void setCoincidentRegisterValue(BigDecimal coincidentRegisterValue) {
        this.coincidentRegisterValue = coincidentRegisterValue;
    }

    public BigDecimal getTotalCumulativeDemand() {
        return totalCumulativeDemand;
    }

    public void setTotalCumulativeDemand(BigDecimal totalCumulativeDemand) {
        this.totalCumulativeDemand = totalCumulativeDemand;
    }

    public BigDecimal[] getCumulativeDemandRates() {
        return cumulativeDemandRates;
    }

    public void setCumulativeDemandRates(BigDecimal[] cumulativeDemandRates) {
        this.cumulativeDemandRates = cumulativeDemandRates;
    }

    public BigDecimal getTotalContinuousCumulativeDemand() {
        return totalContinuousCumulativeDemand;
    }

    public void setTotalContinuousCumulativeDemand(BigDecimal totalContinuousCumulativeDemand) {
        this.totalContinuousCumulativeDemand = totalContinuousCumulativeDemand;
    }

    public BigDecimal[] getContinuousCumulativeDemandRates() {
        return continuousCumulativeDemandRates;
    }

    public void setContinuousCumulativeDemandRates(BigDecimal[] continuousCumulativeDemandRates) {
        this.continuousCumulativeDemandRates = continuousCumulativeDemandRates;
    }

    public Date getTotalMaximumDemandDate() {
        return totalMaximumDemandDate;
    }

    public void setTotalMaximumDemandDate(Date totalMaximumDemandDate) {
        this.totalMaximumDemandDate = totalMaximumDemandDate;
    }

    public Date[] getMaximumDemandRateDates() {
        return maximumDemandRateDates;
    }

    public void setMaximumDemandRateDates(Date[] maximumDemandRateDates) {
        this.maximumDemandRateDates = maximumDemandRateDates;
    }

    public Date[] getHighestPeakDates() {
        return highestPeakDates;
    }

    public void setHighestPeakDates(Date[] highestPeakDates) {
        this.highestPeakDates = highestPeakDates;
    }
}
