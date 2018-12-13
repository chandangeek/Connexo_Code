/*
 * MeteringDefinition.java
 *
 * Created on 7 juli 2004, 12:21
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class MeteringDefinition extends AbstractLogicalAddress {
    
    private static final int MAX_CHANNELS=12;
    
    // partially implemented
    
    Unit[] units={Unit.get("Wh"),
                 Unit.get("Wh"),
                 Unit.get("varh"),
                 Unit.get("varh"),
                 Unit.get("varh"),
                 Unit.get("varh"),
                 Unit.get("varh"),
                 Unit.get("varh"),
                 Unit.get("VAh"),
                 Unit.get(""),
                 Unit.get(""),
                 Unit.get(""),
                 Unit.get(""),
                 Unit.get(""),
                 Unit.get(""),
                 Unit.get("")};
                 
    int channelUnits; // bit 0: kWh import
                      // bit 1: kWh export
                      // bit 2: kvarh import
                      // bit 3: kvarh export
                      // bit 4: kvarh Q1
                      // bit 5: kvarh Q2
                      // bit 6: kvarh Q3
                      // bit 7: kvarh Q4
                      // bit 8: kVA
                      // bit 9: Auxiliary imput 1
                      // bit10: Auxiliary imput 2
                      // bit11: Status flags
                      // bit12: reserved
                      // bit13: reserved
                      // bit14: reserved
                      // bit15: reserved
    int apparentQuadrantDefinition; // bit 0..3: Q1..4
    int nrOfBillingPeriodsStored; 
    int nrOfIntervalRecordingChannels;
    int redThreshold;
    int redUnits;
    
    /** Creates a new instance of MeteringDefinition */
    public MeteringDefinition(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public String toString() {
        return "MeterDefinition: channelUnits=0x"+Integer.toHexString(getChannelUnits())+
               ", apparentQuadrantDefinition=0x"+Integer.toHexString(getApparentQuadrantDefinition())+
               ", nrOfBillingPeriodsStored="+getNrOfBillingPeriodsStored()+
               ", nrOfIntervalRecordingChannels="+getNrOfIntervalRecordingChannels()+
               ", redThreshold="+getRedThreshold()+
               ", redUnits="+getRedUnits();
               
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        // First 6 bytes absorbed
        setChannelUnits(ProtocolUtils.getInt(data,6,2));
        setApparentQuadrantDefinition(ProtocolUtils.getInt(data,8,1));
        setNrOfBillingPeriodsStored(ProtocolUtils.getInt(data,9,1));
        setNrOfIntervalRecordingChannels(ProtocolUtils.getInt(data,10,1));
        setRedThreshold(ProtocolUtils.getInt(data,11,1));
        setRedUnits(ProtocolUtils.getInt(data,12,1));
    }
    
    public boolean isChannelUnitsStatusFlagsChannel() {
        return (getChannelUnits() & 0x800) == 0x800;
    }
    
    public int getChannelId(int index) {
        int channelCount=0;
        for (int i=0;i<MAX_CHANNELS;i++) {
            if ((getChannelUnits()&(0x01<<i)) == (0x01<<i)) {
                if (channelCount == index) {
                    return i;
                }
                channelCount++;
            }
        }
        return -1;
    }
    
    public Unit getChannelUnit(int index) {
        return units[index];
    }
    /**
     * Getter for property channelUnits.
     * @return Value of property channelUnits.
     */
    public int getChannelUnits() {
        return channelUnits;
    }
    
    
    /**
     * Setter for property channelUnits.
     * @param channelUnits New value of property channelUnits.
     */
    public void setChannelUnits(int channelUnits) {
        this.channelUnits = channelUnits;
    }
    
    /**
     * Getter for property apparentQuadrantDefinition.
     * @return Value of property apparentQuadrantDefinition.
     */
    public int getApparentQuadrantDefinition() {
        return apparentQuadrantDefinition;
    }
    
    /**
     * Setter for property apparentQuadrantDefinition.
     * @param apparentQuadrantDefinition New value of property apparentQuadrantDefinition.
     */
    public void setApparentQuadrantDefinition(int apparentQuadrantDefinition) {
        this.apparentQuadrantDefinition = apparentQuadrantDefinition;
    }
    
    /**
     * Getter for property nrOfBillingPeriodsStored.
     * @return Value of property nrOfBillingPeriodsStored.
     */
    public int getNrOfBillingPeriodsStored() {
        return nrOfBillingPeriodsStored;
    }
    
    /**
     * Setter for property nrOfBillingPeriodsStored.
     * @param nrOfBillingPeriodsStored New value of property nrOfBillingPeriodsStored.
     */
    public void setNrOfBillingPeriodsStored(int nrOfBillingPeriodsStored) {
        this.nrOfBillingPeriodsStored = nrOfBillingPeriodsStored;
    }
    
    /**
     * Getter for property nrOfIntervalRecordingChannels.
     * @return Value of property nrOfIntervalRecordingChannels.
     */
    public int getNrOfIntervalRecordingChannels() {
        return nrOfIntervalRecordingChannels;
    }
    
    /**
     * Setter for property nrOfIntervalRecordingChannels.
     * @param nrOfIntervalRecordingChannels New value of property nrOfIntervalRecordingChannels.
     */
    public void setNrOfIntervalRecordingChannels(int nrOfIntervalRecordingChannels) {
        this.nrOfIntervalRecordingChannels = nrOfIntervalRecordingChannels;
    }
    
    /**
     * Getter for property redThreshold.
     * @return Value of property redThreshold.
     */
    public int getRedThreshold() {
        return redThreshold;
    }
    
    /**
     * Setter for property redThreshold.
     * @param redThreshold New value of property redThreshold.
     */
    public void setRedThreshold(int redThreshold) {
        this.redThreshold = redThreshold;
    }
    
    /**
     * Getter for property redUnits.
     * @return Value of property redUnits.
     */
    public int getRedUnits() {
        return redUnits;
    }
    
    /**
     * Setter for property redUnits.
     * @param redUnits New value of property redUnits.
     */
    public void setRedUnits(int redUnits) {
        this.redUnits = redUnits;
    }
    
}
