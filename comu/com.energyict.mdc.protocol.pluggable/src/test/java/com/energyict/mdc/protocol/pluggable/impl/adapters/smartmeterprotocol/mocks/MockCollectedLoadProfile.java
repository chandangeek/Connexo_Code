package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlElement;
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
    private boolean allowIncompleteLoadProfileData = false;

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
    public void setCollectedIntervalData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo) {
        this.collectedIntervalData = collectedIntervalData;
        this.deviceChannelInfo = deviceChannelInfo;
    }

    @Override
    public Range<Instant> getCollectedIntervalDataRange() {
        return null;
    }

    @Override
    public boolean isDoStoreOlderValues() {
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
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {
        this.setResultType(resultType);
        this.issues.addAll(issues);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
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