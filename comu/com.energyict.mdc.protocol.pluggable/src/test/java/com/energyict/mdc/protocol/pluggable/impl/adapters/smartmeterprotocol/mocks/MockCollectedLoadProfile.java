/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-27 (15:05)
 */
public class MockCollectedLoadProfile implements CollectedLoadProfile {

    private LoadProfileIdentifier loadProfileIdentifier;
    private boolean storeOlderValues;
    private ResultType resultType;
    private List<Issue> issues = new ArrayList<>();
    private List<IntervalData> collectedIntervalData = Collections.emptyList();
    private List<ChannelInfo> deviceChannelInfo = Collections.emptyList();

    public MockCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        super();
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public List<IntervalData> getCollectedIntervalData() {
        return this.collectedIntervalData;
    }

    @Override
    public List<ChannelInfo> getChannelInfo() {
        return this.deviceChannelInfo;
    }

    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.loadProfileIdentifier;
    }

    @Override
    public void setCollectedData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo) {
        this.collectedIntervalData = collectedIntervalData;
        this.deviceChannelInfo = deviceChannelInfo;
    }

    @Override
    public Range<Instant> getCollectedIntervalDataRange() {
        return null;
    }

    @Override
    public boolean doStoreOlderValues() {
        return this.storeOlderValues;
    }

    @Override
    public void setDoStoreOlderValues(boolean storeOlderValues) {
        this.storeOlderValues = storeOlderValues;
    }

    @Override
    public ResultType getResultType() {
        return this.resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    @Override
    public List<Issue> getIssues() {
        return this.issues;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {
        this.setResultType(resultType);
        this.issues.add(issue);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

}