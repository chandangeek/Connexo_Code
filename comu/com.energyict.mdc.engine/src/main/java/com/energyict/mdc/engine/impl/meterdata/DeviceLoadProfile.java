package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import com.elster.jupiter.util.Ranges;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a LoadProfile, collected from a Device.
 * If no data could be collected, then a proper {@link #issueList issue} and {@link com.energyict.mdc.protocol.api.device.data.ResultType}
 * will be returned.
 *
 * @author gna
 * @since 29/03/12 - 14:37
 */
public class DeviceLoadProfile extends CollectedDeviceData implements CollectedLoadProfile {

    /**
     * The unique identifier of the LoadProfile for this collected data
     */
    private final LoadProfileIdentifier loadProfileIdentifier;

    /**
     * The collected intervals for the LoadProfile
     */
    private List<IntervalData> collectedIntervalData;
    private Range<Instant> collectedIntervalDataRange = Range.all();

    /**
     * The <code>ChannelInfo</code> corresponding with the {@link #collectedIntervalData}
     */
    private List<ChannelInfo> deviceChannelInfo;

    /**
     * Indication whether to store {@link IntervalData} before the lastReading of
     * a {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     * <ul>
     * <li>true: store values before the lastReading</li>
     * <li>false: don't store values before the lastReading</li>
     * </ul>
     */
    private boolean doStoreOlderValues = false;

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedLoadProfileDeviceCommand(this, meterDataStoreCommand, serviceProvider);
    }

    public DeviceLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        super();
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectLoadProfileData();
    }

    /**
     * @return the collected {@link IntervalData} since lastReading
     */
    @Override
    public List<IntervalData> getCollectedIntervalData() {
        if (this.collectedIntervalData == null) {
            return Collections.emptyList();
        }
        return collectedIntervalData;
    }

    public Range<Instant> getCollectedIntervalDataRange () {
        return collectedIntervalDataRange;
    }

    /**
     * @return the channel configuration of the collected {@link IntervalData}
     */
    @Override
    public List<ChannelInfo> getChannelInfo() {
        if (this.deviceChannelInfo == null) {
            return Collections.emptyList();
        }
        return deviceChannelInfo;
    }

    @Override
    public boolean doStoreOlderValues() {
        return doStoreOlderValues;
    }

    @Override
    public void setDoStoreOlderValues(boolean doStoreOlderValues) {
        this.doStoreOlderValues = doStoreOlderValues;
    }

    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.loadProfileIdentifier;
    }

    @Override
    public void setCollectedData(final List<IntervalData> collectedIntervalData, final List<ChannelInfo> deviceChannelInfo) {
        if (collectedIntervalData == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setCollectedData", "collectedIntervalData");
        } else if (deviceChannelInfo == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setCollectedData", "deviceChannelInfo");
        }
        this.collectedIntervalData = collectedIntervalData;
        this.collectedIntervalDataRange = this.calculateCollectedIntervalDataRange(collectedIntervalData);
        this.deviceChannelInfo = deviceChannelInfo;
    }

    private Range<Instant> calculateCollectedIntervalDataRange (List<IntervalData> collectedIntervalData) {
        Instant fromDate = null;
        Instant toDate = null;
        for (IntervalData intervalData : collectedIntervalData) {
            fromDate = this.min(fromDate, intervalData.getEndTime().toInstant());
            toDate = this.max(toDate, intervalData.getEndTime().toInstant());
        }
        return Ranges.openClosed(fromDate, toDate);
    }

    private Instant min (Instant currentMinimum, Instant candidate) {
        if (currentMinimum == null || candidate.isBefore(currentMinimum)) {
            return candidate;
        }
        else {
            return currentMinimum;
        }
    }

    private Instant max (Instant currentMaximum, Instant candidate) {
        if (currentMaximum == null || candidate.isAfter(currentMaximum)) {
            return candidate;
        }
        else {
            return currentMaximum;
        }
    }

}
