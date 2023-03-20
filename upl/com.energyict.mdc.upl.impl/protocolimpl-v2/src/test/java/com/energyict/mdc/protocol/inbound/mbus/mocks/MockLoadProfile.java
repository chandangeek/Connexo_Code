/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public class MockLoadProfile implements CollectedLoadProfile {
    private final LoadProfileIdentifier loadProfileIdentifier;
    private List<IntervalData> collectedIntervalData;

    public MockLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public List<IntervalData> getCollectedIntervalData() {
        return collectedIntervalData;
    }

    @Override
    public List<ChannelInfo> getChannelInfo() {
        return null;
    }

    @Override
    public boolean isDoStoreOlderValues() {
        return false;
    }

    @Override
    public void setDoStoreOlderValues(boolean doStoreOlderValues) {

    }

    @Override
    public boolean isAllowIncompleteLoadProfileData() {
        return false;
    }

    @Override
    public void setAllowIncompleteLoadProfileData(boolean allowIncompleteLoadProfileData) {

    }

    @Override
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return loadProfileIdentifier;
    }

    @Override
    public void setCollectedIntervalData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo) {
        this.collectedIntervalData = collectedIntervalData;
    }

    @Override
    public Range<Instant> getCollectedIntervalDataRange() {
        return null;
    }

    @Override
    public String getXmlType() {
        return null;
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public ResultType getResultType() {
        return null;
    }

    @Override
    public List<Issue> getIssues() {
        return null;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {

    }

    @Override
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {

    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return false;
    }
}
