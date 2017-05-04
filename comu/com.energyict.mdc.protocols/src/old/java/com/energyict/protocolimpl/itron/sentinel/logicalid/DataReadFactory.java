/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocolimpl.itron.sentinel.tables.ManufacturerTableFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DataReadFactory {

	private ManufacturerTableFactory manufacturerTableFactory;

	private ConstantsDataRead constantsDataRead = null;
	private CapabilitiesDataRead capabilitiesDataRead = null;
	private ClockRelatedDataRead clockRelatedDataRead = null;

	private QuantityIdentificationDataRead quantityIdentificationDataRead = null;

	private CurrentEnergyDataRead currentEnergyDataRead = null;
	private CurrentDemandDataRead currentDemandDataRead = null;
	private CurrentCumulativeDemandDataRead currentCumulativeDemandDataRead = null;
	private CurrentDemandTimeOfOccurenceDataRead currentDemandTimeOfOccurenceDataRead = null;

	private LastBillingPeriodStateDataRead lastBillingPeriodStateDataRead = null;
	private LastBillingPeriodEnergyDataRead lastBillingPeriodEnergyDataRead = null;
	private LastBillingPeriodDemandDataRead lastBillingPeriodDemandDataRead = null;
	private LastBillingPeriodCumulativeDemandDataRead lastBillingPeriodCumulativeDemandDataRead = null;
	private LastBillingPeriodDemandTimeOfOccurenceDataRead lastBillingPeriodDemandTimeOfOccurenceDataRead = null;

	private LastSeasonStateDataRead lastSeasonStateDataRead = null;
	private LastSeasonEnergyDataRead lastSeasonEnergyDataRead = null;
	private LastSeasonDemandDataRead lastSeasonDemandDataRead = null;
	private LastSeasonCumulativeDemandDataRead lastSeasonCumulativeDemandDataRead = null;
	private LastSeasonDemandTimeOfOccurenceDataRead lastSeasonDemandTimeOfOccurenceDataRead = null;

	private LastSelfReadStateDataRead lastSelfReadStateDataRead = null;
	private LastSelfReadEnergyDataRead lastSelfReadEnergyDataRead = null;
	private LastSelfReadDemandDataRead lastSelfReadDemandDataRead = null;
	private LastSelfReadCumulativeDemandDataRead lastSelfReadCumulativeDemandDataRead = null;
	private LastSelfReadDemandTimeOfOccurenceDataRead lastSelfReadDemandTimeOfOccurenceDataRead = null;

	private LoadProfileQuantitiesDataRead loadProfileQuantitiesDataRead = null;
	private LoadProfilePulseWeightsDataRead loadProfilePulseWeightsDataRead = null;
	private LoadProfilePreliminaryDataRead loadProfilePreliminaryDataRead = null;

	private MeterMultiplierDataRead meterMultiplierDataRead = null;

	/** Creates a new instance of ReadFactory */
	public DataReadFactory(ManufacturerTableFactory manufacturerTableFactory) {
		this.setManufacturerTableFactory(manufacturerTableFactory);
	}

	public LoadProfilePreliminaryDataRead getLoadProfilePreliminaryDataRead() throws IOException {
		return getLoadProfilePreliminaryDataRead(false);
	}

	public MeterMultiplierDataRead getMeterMultiplierDataRead() throws IOException {
		if (this.meterMultiplierDataRead == null) {
			this.meterMultiplierDataRead = new MeterMultiplierDataRead(this);
			this.meterMultiplierDataRead.invoke();
		}
		return this.meterMultiplierDataRead;
	}

	public LoadProfilePreliminaryDataRead getLoadProfilePreliminaryDataRead(boolean refresh) throws IOException {
		if ((this.loadProfilePreliminaryDataRead == null) || (refresh)) {
			this.loadProfilePreliminaryDataRead = new LoadProfilePreliminaryDataRead(this);
			this.loadProfilePreliminaryDataRead.invoke();
		}
		return this.loadProfilePreliminaryDataRead;
	}

	public LoadProfilePulseWeightsDataRead getLoadProfilePulseWeightsDataRead() throws IOException {
		if (this.loadProfilePulseWeightsDataRead == null) {
			this.loadProfilePulseWeightsDataRead = new LoadProfilePulseWeightsDataRead(this);
			this.loadProfilePulseWeightsDataRead.invoke();
		}
		return this.loadProfilePulseWeightsDataRead;
	}

	public LoadProfileQuantitiesDataRead getLoadProfileQuantitiesDataRead() throws IOException {
		if (this.loadProfileQuantitiesDataRead == null) {
			this.loadProfileQuantitiesDataRead = new LoadProfileQuantitiesDataRead(this);
			this.loadProfileQuantitiesDataRead.invoke();
		}
		return this.loadProfileQuantitiesDataRead;
	}

	public LastSelfReadDemandTimeOfOccurenceDataRead getLastSelfReadDemandTimeOfOccurenceDataRead() throws IOException {
		if (this.lastSelfReadDemandTimeOfOccurenceDataRead == null) {
			this.lastSelfReadDemandTimeOfOccurenceDataRead = new LastSelfReadDemandTimeOfOccurenceDataRead(this);
			this.lastSelfReadDemandTimeOfOccurenceDataRead.invoke();
		}
		return this.lastSelfReadDemandTimeOfOccurenceDataRead;
	}

	public LastSelfReadCumulativeDemandDataRead getLastSelfReadCumulativeDemandDataRead() throws IOException {
		if (this.lastSelfReadCumulativeDemandDataRead == null) {
			this.lastSelfReadCumulativeDemandDataRead = new LastSelfReadCumulativeDemandDataRead(this);
			this.lastSelfReadCumulativeDemandDataRead.invoke();
		}
		return this.lastSelfReadCumulativeDemandDataRead;
	}

	public LastSelfReadDemandDataRead getLastSelfReadDemandDataRead() throws IOException {
		if (this.lastSelfReadDemandDataRead == null) {
			this.lastSelfReadDemandDataRead = new LastSelfReadDemandDataRead(this);
			this.lastSelfReadDemandDataRead.invoke();
		}
		return this.lastSelfReadDemandDataRead;
	}

	public LastSelfReadEnergyDataRead getLastSelfReadEnergyDataRead() throws IOException {
		if (this.lastSelfReadEnergyDataRead == null) {
			this.lastSelfReadEnergyDataRead = new LastSelfReadEnergyDataRead(this);
			this.lastSelfReadEnergyDataRead.invoke();
		}
		return this.lastSelfReadEnergyDataRead;
	}

	public LastSelfReadStateDataRead getLastSelfReadStateDataRead() throws IOException {
		if (this.lastSelfReadStateDataRead == null) {
			this.lastSelfReadStateDataRead = new LastSelfReadStateDataRead(this);
			this.lastSelfReadStateDataRead.invoke();
		}
		return this.lastSelfReadStateDataRead;
	}

	public LastSeasonDemandTimeOfOccurenceDataRead getLastSeasonDemandTimeOfOccurenceDataRead() throws IOException {
		if (this.lastSeasonDemandTimeOfOccurenceDataRead == null) {
			this.lastSeasonDemandTimeOfOccurenceDataRead = new LastSeasonDemandTimeOfOccurenceDataRead(this);
			this.lastSeasonDemandTimeOfOccurenceDataRead.invoke();
		}
		return this.lastSeasonDemandTimeOfOccurenceDataRead;
	}

	public LastSeasonCumulativeDemandDataRead getLastSeasonCumulativeDemandDataRead() throws IOException {
		if (this.lastSeasonCumulativeDemandDataRead == null) {
			this.lastSeasonCumulativeDemandDataRead = new LastSeasonCumulativeDemandDataRead(this);
			this.lastSeasonCumulativeDemandDataRead.invoke();
		}
		return this.lastSeasonCumulativeDemandDataRead;
	}

	public LastSeasonDemandDataRead getLastSeasonDemandDataRead() throws IOException {
		if (this.lastSeasonDemandDataRead == null) {
			this.lastSeasonDemandDataRead = new LastSeasonDemandDataRead(this);
			this.lastSeasonDemandDataRead.invoke();
		}
		return this.lastSeasonDemandDataRead;
	}

	public LastSeasonEnergyDataRead getLastSeasonEnergyDataRead() throws IOException {
		if (this.lastSeasonEnergyDataRead == null) {
			this.lastSeasonEnergyDataRead = new LastSeasonEnergyDataRead(this);
			this.lastSeasonEnergyDataRead.invoke();
		}
		return this.lastSeasonEnergyDataRead;
	}

	public LastSeasonStateDataRead getLastSeasonStateDataRead() throws IOException {
		if (this.lastSeasonStateDataRead == null) {
			this.lastSeasonStateDataRead = new LastSeasonStateDataRead(this);
			this.lastSeasonStateDataRead.invoke();
		}
		return this.lastSeasonStateDataRead;
	}

	public LastBillingPeriodDemandTimeOfOccurenceDataRead getLastBillingPeriodDemandTimeOfOccurenceDataRead() throws IOException {
		if (this.lastBillingPeriodDemandTimeOfOccurenceDataRead == null) {
			this.lastBillingPeriodDemandTimeOfOccurenceDataRead = new LastBillingPeriodDemandTimeOfOccurenceDataRead(this);
			this.lastBillingPeriodDemandTimeOfOccurenceDataRead.invoke();
		}
		return this.lastBillingPeriodDemandTimeOfOccurenceDataRead;
	}

	public LastBillingPeriodCumulativeDemandDataRead getLastBillingPeriodCumulativeDemandDataRead() throws IOException {
		if (this.lastBillingPeriodCumulativeDemandDataRead == null) {
			this.lastBillingPeriodCumulativeDemandDataRead = new LastBillingPeriodCumulativeDemandDataRead(this);
			this.lastBillingPeriodCumulativeDemandDataRead.invoke();
		}
		return this.lastBillingPeriodCumulativeDemandDataRead;
	}

	public LastBillingPeriodDemandDataRead getLastBillingPeriodDemandDataRead() throws IOException {
		if (this.lastBillingPeriodDemandDataRead == null) {
			this.lastBillingPeriodDemandDataRead = new LastBillingPeriodDemandDataRead(this);
			this.lastBillingPeriodDemandDataRead.invoke();
		}
		return this.lastBillingPeriodDemandDataRead;
	}

	public LastBillingPeriodEnergyDataRead getLastBillingPeriodEnergyDataRead() throws IOException {
		if (this.lastBillingPeriodEnergyDataRead == null) {
			this.lastBillingPeriodEnergyDataRead = new LastBillingPeriodEnergyDataRead(this);
			this.lastBillingPeriodEnergyDataRead.invoke();
		}
		return this.lastBillingPeriodEnergyDataRead;
	}

	public LastBillingPeriodStateDataRead getLastBillingPeriodStateDataRead() throws IOException {
		if (this.lastBillingPeriodStateDataRead == null) {
			this.lastBillingPeriodStateDataRead = new LastBillingPeriodStateDataRead(this);
			this.lastBillingPeriodStateDataRead.invoke();
		}
		return this.lastBillingPeriodStateDataRead;
	}

	public CurrentDemandTimeOfOccurenceDataRead getCurrentDemandTimeOfOccurenceDataRead() throws IOException {
		if (this.currentDemandTimeOfOccurenceDataRead == null) {
			this.currentDemandTimeOfOccurenceDataRead = new CurrentDemandTimeOfOccurenceDataRead(this);
			this.currentDemandTimeOfOccurenceDataRead.invoke();
		}
		return this.currentDemandTimeOfOccurenceDataRead;
	}

	public CurrentCumulativeDemandDataRead getCurrentCumulativeDemandDataRead() throws IOException {
		if (this.currentCumulativeDemandDataRead == null) {
			this.currentCumulativeDemandDataRead = new CurrentCumulativeDemandDataRead(this);
			this.currentCumulativeDemandDataRead.invoke();
		}
		return this.currentCumulativeDemandDataRead;
	}

	public CurrentDemandDataRead getCurrentDemandDataRead() throws IOException {
		if (this.currentDemandDataRead == null) {
			this.currentDemandDataRead = new CurrentDemandDataRead(this);
			this.currentDemandDataRead.invoke();
		}
		return this.currentDemandDataRead;
	}

	public CurrentEnergyDataRead getCurrentEnergyDataRead() throws IOException {
		if (this.currentEnergyDataRead == null) {
			this.currentEnergyDataRead = new CurrentEnergyDataRead(this);
			this.currentEnergyDataRead.invoke();
		}
		return this.currentEnergyDataRead;
	}

	public QuantityIdentificationDataRead getQuantityIdentificationDataRead() throws IOException {
		if (this.quantityIdentificationDataRead == null) {
			this.quantityIdentificationDataRead = new QuantityIdentificationDataRead(this);
			this.quantityIdentificationDataRead.invoke();
		}
		return this.quantityIdentificationDataRead;
	}

	public ClockRelatedDataRead getClockRelatedDataRead() throws IOException {
		if (this.clockRelatedDataRead == null) {
			this.clockRelatedDataRead = new ClockRelatedDataRead(this);
			this.clockRelatedDataRead.invoke();
		}
		return this.clockRelatedDataRead;
	}

	public CapabilitiesDataRead getCapabilitiesDataRead() throws IOException {
		if (this.capabilitiesDataRead == null) {
			this.capabilitiesDataRead = new CapabilitiesDataRead(this);
			this.capabilitiesDataRead.invoke();
		}
		return this.capabilitiesDataRead;
	}

	public ConstantsDataRead getConstantsDataRead() throws IOException {
		if (this.constantsDataRead == null) {
			this.constantsDataRead = new ConstantsDataRead(this);
			this.constantsDataRead.invoke();
		}
		return this.constantsDataRead;
	}

	public ManufacturerTableFactory getManufacturerTableFactory() {
		return this.manufacturerTableFactory;
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
