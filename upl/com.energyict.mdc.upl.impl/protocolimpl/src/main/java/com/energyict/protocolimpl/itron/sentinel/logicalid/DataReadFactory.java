/*
 * ReadFactory.java
 *
 * Created on 2 november 2006, 16:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.itron.sentinel.*;
import com.energyict.protocolimpl.itron.sentinel.tables.*;
import java.io.*;

/**
 *
 * @author Koen
 */
public class DataReadFactory {
    
    
    private ManufacturerTableFactory manufacturerTableFactory;
    
    ConstantsDataRead constantsDataRead=null;
    CapabilitiesDataRead capabilitiesDataRead=null;
    ClockRelatedDataRead clockRelatedDataRead=null;
    
    QuantityIdentificationDataRead quantityIdentificationDataRead=null;
    
    CurrentEnergyDataRead currentEnergyDataRead=null;
    CurrentDemandDataRead currentDemandDataRead=null;
    CurrentCumulativeDemandDataRead currentCumulativeDemandDataRead=null;
    CurrentDemandTimeOfOccurenceDataRead currentDemandTimeOfOccurenceDataRead=null;
    
    LastBillingPeriodStateDataRead lastBillingPeriodStateDataRead=null;
    LastBillingPeriodEnergyDataRead lastBillingPeriodEnergyDataRead=null;
    LastBillingPeriodDemandDataRead lastBillingPeriodDemandDataRead=null;
    LastBillingPeriodCumulativeDemandDataRead lastBillingPeriodCumulativeDemandDataRead=null;
    LastBillingPeriodDemandTimeOfOccurenceDataRead lastBillingPeriodDemandTimeOfOccurenceDataRead=null;
    
    LastSeasonStateDataRead lastSeasonStateDataRead=null;
    LastSeasonEnergyDataRead lastSeasonEnergyDataRead=null;
    LastSeasonDemandDataRead lastSeasonDemandDataRead=null;
    LastSeasonCumulativeDemandDataRead lastSeasonCumulativeDemandDataRead=null;
    LastSeasonDemandTimeOfOccurenceDataRead lastSeasonDemandTimeOfOccurenceDataRead=null;
    
    LastSelfReadStateDataRead lastSelfReadStateDataRead=null;
    LastSelfReadEnergyDataRead lastSelfReadEnergyDataRead=null;
    LastSelfReadDemandDataRead lastSelfReadDemandDataRead=null;
    LastSelfReadCumulativeDemandDataRead lastSelfReadCumulativeDemandDataRead=null;
    LastSelfReadDemandTimeOfOccurenceDataRead lastSelfReadDemandTimeOfOccurenceDataRead=null;
    
    LoadProfileQuantitiesDataRead loadProfileQuantitiesDataRead=null;
    LoadProfilePulseWeightsDataRead loadProfilePulseWeightsDataRead=null;
    LoadProfilePreliminaryDataRead loadProfilePreliminaryDataRead=null;
    
    MeterMultiplierDataRead meterMultiplierDataRead=null;
    
    /** Creates a new instance of ReadFactory */
    public DataReadFactory(ManufacturerTableFactory manufacturerTableFactory) {
        this.setManufacturerTableFactory(manufacturerTableFactory);
    }

    public LoadProfilePreliminaryDataRead getLoadProfilePreliminaryDataRead() throws IOException {
        return getLoadProfilePreliminaryDataRead(false);
    }
    
    public MeterMultiplierDataRead getMeterMultiplierDataRead() throws IOException {
        if (meterMultiplierDataRead==null) {
            meterMultiplierDataRead = new MeterMultiplierDataRead(this);
            meterMultiplierDataRead.invoke();
        }
        return meterMultiplierDataRead;
    }    
    
    public LoadProfilePreliminaryDataRead getLoadProfilePreliminaryDataRead(boolean refresh) throws IOException {
        if ((loadProfilePreliminaryDataRead==null) || (refresh)) {
            loadProfilePreliminaryDataRead = new LoadProfilePreliminaryDataRead(this);
            loadProfilePreliminaryDataRead.invoke();
        }
        return loadProfilePreliminaryDataRead;
    }    

    public LoadProfilePulseWeightsDataRead getLoadProfilePulseWeightsDataRead() throws IOException {
        if (loadProfilePulseWeightsDataRead == null ) {
            loadProfilePulseWeightsDataRead = new LoadProfilePulseWeightsDataRead(this);
            loadProfilePulseWeightsDataRead.invoke();
        }
        return loadProfilePulseWeightsDataRead;
    }    
    
    public LoadProfileQuantitiesDataRead getLoadProfileQuantitiesDataRead() throws IOException {
        if (loadProfileQuantitiesDataRead == null ) {
            loadProfileQuantitiesDataRead = new LoadProfileQuantitiesDataRead(this);
            loadProfileQuantitiesDataRead.invoke();
        }
        return loadProfileQuantitiesDataRead;
    }    
    
    public LastSelfReadDemandTimeOfOccurenceDataRead getLastSelfReadDemandTimeOfOccurenceDataRead() throws IOException {
        if (lastSelfReadDemandTimeOfOccurenceDataRead == null ) {
            lastSelfReadDemandTimeOfOccurenceDataRead = new LastSelfReadDemandTimeOfOccurenceDataRead(this);
            lastSelfReadDemandTimeOfOccurenceDataRead.invoke();
        }
        return lastSelfReadDemandTimeOfOccurenceDataRead;
    }    
    
    public LastSelfReadCumulativeDemandDataRead getLastSelfReadCumulativeDemandDataRead() throws IOException {
        if (lastSelfReadCumulativeDemandDataRead == null ) {
            lastSelfReadCumulativeDemandDataRead = new LastSelfReadCumulativeDemandDataRead(this);
            lastSelfReadCumulativeDemandDataRead.invoke();
        }
        return lastSelfReadCumulativeDemandDataRead;
    }    
    
    public LastSelfReadDemandDataRead getLastSelfReadDemandDataRead() throws IOException {
        if (lastSelfReadDemandDataRead == null ) {
            lastSelfReadDemandDataRead = new LastSelfReadDemandDataRead(this);
            lastSelfReadDemandDataRead.invoke();
        }
        return lastSelfReadDemandDataRead;
    }    
    
    public LastSelfReadEnergyDataRead getLastSelfReadEnergyDataRead() throws IOException {
        if (lastSelfReadEnergyDataRead == null ) {
            lastSelfReadEnergyDataRead = new LastSelfReadEnergyDataRead(this);
            lastSelfReadEnergyDataRead.invoke();
        }
        return lastSelfReadEnergyDataRead;
    }    
    
    public LastSelfReadStateDataRead getLastSelfReadStateDataRead() throws IOException {
        if (lastSelfReadStateDataRead == null ) {
            lastSelfReadStateDataRead = new LastSelfReadStateDataRead(this);
            lastSelfReadStateDataRead.invoke();
        }
        return lastSelfReadStateDataRead;
    }    
    
    public LastSeasonDemandTimeOfOccurenceDataRead getLastSeasonDemandTimeOfOccurenceDataRead() throws IOException {
        if (lastSeasonDemandTimeOfOccurenceDataRead == null ) {
            lastSeasonDemandTimeOfOccurenceDataRead = new LastSeasonDemandTimeOfOccurenceDataRead(this);
            lastSeasonDemandTimeOfOccurenceDataRead.invoke();
        }
        return lastSeasonDemandTimeOfOccurenceDataRead;
    }    
    
    public LastSeasonCumulativeDemandDataRead getLastSeasonCumulativeDemandDataRead() throws IOException {
        if (lastSeasonCumulativeDemandDataRead == null ) {
            lastSeasonCumulativeDemandDataRead = new LastSeasonCumulativeDemandDataRead(this);
            lastSeasonCumulativeDemandDataRead.invoke();
        }
        return lastSeasonCumulativeDemandDataRead;
    }    
    
    public LastSeasonDemandDataRead getLastSeasonDemandDataRead() throws IOException {
        if (lastSeasonDemandDataRead == null ) {
            lastSeasonDemandDataRead = new LastSeasonDemandDataRead(this);
            lastSeasonDemandDataRead.invoke();
        }
        return lastSeasonDemandDataRead;
    }    
    
    public LastSeasonEnergyDataRead getLastSeasonEnergyDataRead() throws IOException {
        if (lastSeasonEnergyDataRead == null ) {
            lastSeasonEnergyDataRead = new LastSeasonEnergyDataRead(this);
            lastSeasonEnergyDataRead.invoke();
        }
        return lastSeasonEnergyDataRead;
    }    
    
    public LastSeasonStateDataRead getLastSeasonStateDataRead() throws IOException {
        if (lastSeasonStateDataRead == null ) {
            lastSeasonStateDataRead = new LastSeasonStateDataRead(this);
            lastSeasonStateDataRead.invoke();
        }
        return lastSeasonStateDataRead;
    }    
    
    public LastBillingPeriodDemandTimeOfOccurenceDataRead getLastBillingPeriodDemandTimeOfOccurenceDataRead() throws IOException {
        if (lastBillingPeriodDemandTimeOfOccurenceDataRead == null ) {
            lastBillingPeriodDemandTimeOfOccurenceDataRead = new LastBillingPeriodDemandTimeOfOccurenceDataRead(this);
            lastBillingPeriodDemandTimeOfOccurenceDataRead.invoke();
        }
        return lastBillingPeriodDemandTimeOfOccurenceDataRead;
    }    
    
    public LastBillingPeriodCumulativeDemandDataRead getLastBillingPeriodCumulativeDemandDataRead() throws IOException {
        if (lastBillingPeriodCumulativeDemandDataRead == null ) {
            lastBillingPeriodCumulativeDemandDataRead = new LastBillingPeriodCumulativeDemandDataRead(this);
            lastBillingPeriodCumulativeDemandDataRead.invoke();
        }
        return lastBillingPeriodCumulativeDemandDataRead;
    }    

    public LastBillingPeriodDemandDataRead getLastBillingPeriodDemandDataRead() throws IOException {
        if (lastBillingPeriodDemandDataRead == null ) {
            lastBillingPeriodDemandDataRead = new LastBillingPeriodDemandDataRead(this);
            lastBillingPeriodDemandDataRead.invoke();
        }
        return lastBillingPeriodDemandDataRead;
    }    
    
    public LastBillingPeriodEnergyDataRead getLastBillingPeriodEnergyDataRead() throws IOException {
        if (lastBillingPeriodEnergyDataRead == null ) {
            lastBillingPeriodEnergyDataRead = new LastBillingPeriodEnergyDataRead(this);
            lastBillingPeriodEnergyDataRead.invoke();
        }
        return lastBillingPeriodEnergyDataRead;
    }    
    
    public LastBillingPeriodStateDataRead getLastBillingPeriodStateDataRead() throws IOException {
        if (lastBillingPeriodStateDataRead == null ) {
            lastBillingPeriodStateDataRead = new LastBillingPeriodStateDataRead(this);
            lastBillingPeriodStateDataRead.invoke();
        }
        return lastBillingPeriodStateDataRead;
    }    
    
    public CurrentDemandTimeOfOccurenceDataRead getCurrentDemandTimeOfOccurenceDataRead() throws IOException {
        if (currentDemandTimeOfOccurenceDataRead == null ) {
            currentDemandTimeOfOccurenceDataRead = new CurrentDemandTimeOfOccurenceDataRead(this);
            currentDemandTimeOfOccurenceDataRead.invoke();
        }
        return currentDemandTimeOfOccurenceDataRead;
    }    
    
    public CurrentCumulativeDemandDataRead getCurrentCumulativeDemandDataRead() throws IOException {
        if (currentCumulativeDemandDataRead == null ) {
            currentCumulativeDemandDataRead = new CurrentCumulativeDemandDataRead(this);
            currentCumulativeDemandDataRead.invoke();
        }
        return currentCumulativeDemandDataRead;
    }    
    
    public CurrentDemandDataRead getCurrentDemandDataRead() throws IOException {
        if (currentDemandDataRead == null ) {
            currentDemandDataRead = new CurrentDemandDataRead(this);
            currentDemandDataRead.invoke();
        }
        return currentDemandDataRead;
    }    
    
    public CurrentEnergyDataRead getCurrentEnergyDataRead() throws IOException {
        if (currentEnergyDataRead == null ) {
            currentEnergyDataRead = new CurrentEnergyDataRead(this);
            currentEnergyDataRead.invoke();
        }
        return currentEnergyDataRead;
    }    
    
    public QuantityIdentificationDataRead getQuantityIdentificationDataRead() throws IOException {
        if (quantityIdentificationDataRead == null ) {
            quantityIdentificationDataRead = new QuantityIdentificationDataRead(this);
            quantityIdentificationDataRead.invoke();
        }
        return quantityIdentificationDataRead;
    }    
    
    public ClockRelatedDataRead getClockRelatedDataRead() throws IOException {
        if (clockRelatedDataRead == null ) {
            clockRelatedDataRead = new ClockRelatedDataRead(this);
            clockRelatedDataRead.invoke();
        }
        return clockRelatedDataRead;
    }    
    
    public CapabilitiesDataRead getCapabilitiesDataRead() throws IOException {
        if (capabilitiesDataRead == null ) {
            capabilitiesDataRead = new CapabilitiesDataRead(this);
            capabilitiesDataRead.invoke();
        }
        return capabilitiesDataRead;
    }
    
    public ConstantsDataRead getConstantsDataRead() throws IOException {
        if (constantsDataRead == null ) {
            constantsDataRead = new ConstantsDataRead(this);
            constantsDataRead.invoke();
        }
        return constantsDataRead;
    }

    public ManufacturerTableFactory getManufacturerTableFactory() {
        return manufacturerTableFactory;
    }

    private void setManufacturerTableFactory(ManufacturerTableFactory manufacturerTableFactory) {
        this.manufacturerTableFactory = manufacturerTableFactory;
    }
    
    
    public CurrentStateDataRead getCurrentStateDataRead() throws IOException {
        CurrentStateDataRead currentStateDataRead = new CurrentStateDataRead(this);
        currentStateDataRead.invoke();
        return currentStateDataRead;
    }
    
}
