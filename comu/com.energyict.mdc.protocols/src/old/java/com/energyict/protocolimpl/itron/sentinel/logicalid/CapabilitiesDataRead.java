/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class CapabilitiesDataRead extends AbstractDataRead {
    
    
    private int numberOfTOURates; // (0 - 7)
    private boolean meterHasAClock; // (True/False)
    private boolean meterHasValidSelfReadData; // (True/False)
    private int numberOfLoadProfileChannels; // (0 - 8)
    private int numberOfEnergies; // (1 - 8)
    private int numberOfDemands; // (1 - 10)
    private int numberOfCumulativeDemands; // (1 - 8)
    private int pFAvgBillingPeriodAvailable; // (0 or 1)
    
    /** Creates a new instance of ConstantsDataRead */
    public CapabilitiesDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CapabilitiesDataRead:\n");
        strBuff.append("   PFAvgBillingPeriodAvailable="+getPFAvgBillingPeriodAvailable()+"\n");
        strBuff.append("   meterHasAClock="+isMeterHasAClock()+"\n");
        strBuff.append("   meterHasValidSelfReadData="+isMeterHasValidSelfReadData()+"\n");
        strBuff.append("   numberOfCumulativeDemands="+getNumberOfCumulativeDemands()+"\n");
        strBuff.append("   numberOfDemands="+getNumberOfDemands()+"\n");
        strBuff.append("   numberOfEnergies="+getNumberOfEnergies()+"\n");
        strBuff.append("   numberOfLoadProfileChannels="+getNumberOfLoadProfileChannels()+"\n");
        strBuff.append("   numberOfTOURates="+getNumberOfTOURates()+"\n");
        return strBuff.toString();
    }     
    
    protected void parse(byte[] data) throws IOException {
        
        int offset=0;
        
        setNumberOfTOURates(C12ParseUtils.getInt(data,offset++)); // (0 - 7)
        setMeterHasAClock(C12ParseUtils.getInt(data,offset++)==1); // (True/False)
        setMeterHasValidSelfReadData(C12ParseUtils.getInt(data,offset++)==1); // (True/False)
        setNumberOfLoadProfileChannels(C12ParseUtils.getInt(data,offset++)); // (0 - 8)
        setNumberOfEnergies(C12ParseUtils.getInt(data,offset++)); // (1 - 8)
        setNumberOfDemands(C12ParseUtils.getInt(data,offset++)); // (1 - 10)
        setNumberOfCumulativeDemands(C12ParseUtils.getInt(data,offset++)); // (1 - 8)
        setPFAvgBillingPeriodAvailable(C12ParseUtils.getInt(data,offset++)); // (0 or 1)        
        
        
        
    }
    
    protected void prepareBuild() throws IOException {
        
        long[] lids = new long[]{LogicalIDFactory.findLogicalId("NUM_TOU_RATES").getId(),
                                 LogicalIDFactory.findLogicalId("METER_HAS_CLOCK").getId(),
                                 LogicalIDFactory.findLogicalId("VALID_SELF_READ").getId(),
                                 LogicalIDFactory.findLogicalId("NUM_LP_CHANNELS").getId(),
                                 LogicalIDFactory.findLogicalId("NUMBER_OF_ENERGIES").getId(),
                                 LogicalIDFactory.findLogicalId("NUMBER_OF_DEMANDS").getId(),
                                 LogicalIDFactory.findLogicalId("NUMBER_OF_CUMS").getId(),
                                 LogicalIDFactory.findLogicalId("PF_AVG_BILL_AVAILABLE").getId()};
        
        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x08, lids));    
        
    } // protected void prepareBuild() throws IOException

    public int getNumberOfTOURates() {
        return numberOfTOURates;
    }

    public void setNumberOfTOURates(int numberOfTOURates) {
        this.numberOfTOURates = numberOfTOURates;
    }

    public boolean isMeterHasAClock() {
        return meterHasAClock;
    }

    public void setMeterHasAClock(boolean meterHasAClock) {
        this.meterHasAClock = meterHasAClock;
    }

    public boolean isMeterHasValidSelfReadData() {
        return meterHasValidSelfReadData;
    }

    public void setMeterHasValidSelfReadData(boolean meterHasValidSelfReadData) {
        this.meterHasValidSelfReadData = meterHasValidSelfReadData;
    }

    public int getNumberOfLoadProfileChannels() {
        return numberOfLoadProfileChannels;
    }

    public void setNumberOfLoadProfileChannels(int numberOfLoadProfileChannels) {
        this.numberOfLoadProfileChannels = numberOfLoadProfileChannels;
    }

    public int getNumberOfEnergies() {
        return numberOfEnergies;
    }

    public void setNumberOfEnergies(int numberOfEnergies) {
        this.numberOfEnergies = numberOfEnergies;
    }

    public int getNumberOfDemands() {
        return numberOfDemands;
    }

    public void setNumberOfDemands(int numberOfDemands) {
        this.numberOfDemands = numberOfDemands;
    }

    public int getNumberOfCumulativeDemands() {
        return numberOfCumulativeDemands;
    }

    public void setNumberOfCumulativeDemands(int numberOfCumulativeDemands) {
        this.numberOfCumulativeDemands = numberOfCumulativeDemands;
    }

    public int getPFAvgBillingPeriodAvailable() {
        return pFAvgBillingPeriodAvailable;
    }

    public void setPFAvgBillingPeriodAvailable(int pFAvgBillingPeriodAvailable) {
        this.pFAvgBillingPeriodAvailable = pFAvgBillingPeriodAvailable;
    }
    
} // public class ConstantsDataRead extends AbstractDataRead
