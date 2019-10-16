/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a LoadProfile, collected from a Device.
 * If no data could be collected, then a proper {@link #issueList issue} and {@link com.energyict.mdc.upl.meterdata.ResultType}
 * will be returned.
 *
 * @author gna
 * @since 29/03/12 - 14:37
 */
public class DeviceLoadProfile extends CollectedDeviceData implements CollectedLoadProfile {

    /**
     * The unique identifier of the LoadProfile for this collected data.
     */
    private LoadProfileIdentifier loadProfileIdentifier;

    /**
     * The collected intervals for the LoadProfile.
     */
    private List<IntervalData> collectedIntervalData;
    private Range<Instant> collectedIntervalDataRange = Range.all();

    /**
     * The <code>ChannelInfo</code> corresponding to the {@link #collectedIntervalData}.
     */
    private List<ChannelInfo> deviceChannelInfo;

    /**
     * Indication whether to store {@link IntervalData} before the lastReading of
     * a {@link com.energyict.mdc.upl.meterdata.LoadProfile}
     * <ul>
     * <li>true: store values before the lastReading</li>
     * <li>false: don't store values before the lastReading</li>
     * </ul>
     */
    private boolean doStoreOlderValues = false;

    /**
     * Indication whether the collected intervalData may be incomplete or not. <br/>
     * IntervalData is considered incomplete in case the intervalData contains only data for part of the channels
     * of the corresponding LoadProfile
     * <ul>
     * <li>false: the collected intervalData should be complete, thus there should be data present for <b>all channels</b> of the corresponding loadProfile</li>
     * <li>true: the collected intervalData may be incomplete; interval data can be missing for some of the channels of the corresponding loadProfile<br/>
     * E.g.: the loadProfile is defined in EIMaster with 32 channels, but the collected intervalData only contains data for 4 channels</li>
     * </ul>
     * <b>Remark:</b> By default this is set to <i>false</i>, <i>true</i> should only be used in specific cases (such as EIWeb)!
     */
    private boolean allowIncompleteLoadProfileData = false;

    public DeviceLoadProfile() {
        super();
    }

    public DeviceLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        super();
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedLoadProfileDeviceCommand(this, this.getComTaskExecution(), meterDataStoreCommand, serviceProvider);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectLoadProfileData();
    }

    @Override
    public List<IntervalData> getCollectedIntervalData() {
        if (this.collectedIntervalData == null) {
            return Collections.emptyList();
        }
        return collectedIntervalData;
    }

    public Range<Instant> getCollectedIntervalDataRange() {
        return collectedIntervalDataRange;
    }

    @Override
    public List<ChannelInfo> getChannelInfo() {
        if (this.deviceChannelInfo == null) {
            this.deviceChannelInfo = new ArrayList<ChannelInfo>();
        }
        return deviceChannelInfo;
    }

    @Override
    public boolean isDoStoreOlderValues() {
        return doStoreOlderValues;
    }

    @Override
    public void setDoStoreOlderValues(boolean doStoreOlderValues) {
        this.doStoreOlderValues = doStoreOlderValues;
    }

    @XmlElements( {
            @XmlElement(type = LoadProfileIdentifierById.class),
            @XmlElement(type = LoadProfileIdentifierByObisCodeAndDevice.class),
            @XmlElement(type = LoadProfileIdentifierFirstOnDevice.class),
    })
    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.loadProfileIdentifier;
    }

    @Override
    public void setCollectedIntervalData(final List<IntervalData> collectedIntervalData, final List<ChannelInfo> deviceChannelInfo) {
        if (collectedIntervalData == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setCollectedIntervalData", "collectedIntervalData", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        } else if (deviceChannelInfo == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setCollectedIntervalData", "deviceChannelInfo", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.collectedIntervalData = collectedIntervalData;
        this.collectedIntervalDataRange = this.calculateCollectedIntervalDataRange(collectedIntervalData);
        this.deviceChannelInfo = deviceChannelInfo;
    }

    private Range<Instant> calculateCollectedIntervalDataRange(List<IntervalData> collectedIntervalData) {
        Instant fromDate = null;
        Instant toDate = null;
        for (IntervalData intervalData : collectedIntervalData) {
            fromDate = this.min(fromDate, intervalData.getEndTime().toInstant());
            toDate = this.max(toDate, intervalData.getEndTime().toInstant());
        }
        return Ranges.openClosed(fromDate, toDate);
    }

    private Instant min(Instant currentMinimum, Instant candidate) {
        if (currentMinimum == null || candidate.isBefore(currentMinimum)) {
            return candidate;
        } else {
            return currentMinimum;
        }
    }

    private Instant max(Instant currentMaximum, Instant candidate) {
        if (currentMaximum == null || candidate.isAfter(currentMaximum)) {
            return candidate;
        } else {
            return currentMaximum;
        }
    }

    @Override
    public boolean isAllowIncompleteLoadProfileData() {
        return allowIncompleteLoadProfileData;
    }

    @Override
    public void setAllowIncompleteLoadProfileData(boolean allowIncompleteLoadProfileData) {
        this.allowIncompleteLoadProfileData = allowIncompleteLoadProfileData;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}